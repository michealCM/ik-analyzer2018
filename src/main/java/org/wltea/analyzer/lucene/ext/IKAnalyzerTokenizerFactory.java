package org.wltea.analyzer.lucene.ext;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeSource.AttributeFactory;
import org.wltea.analyzer.dic.Dictionary;
import org.wltea.analyzer.lucene.IKTokenizer;

/**
 * 自定义实现分词工厂。
 * 
 * @author micheal
 * @date 2018-8-23 13:56:52 <BR/>
 */
public class IKAnalyzerTokenizerFactory extends TokenizerFactory implements DaynamicDictLoaderAware,ResourceLoaderAware {

	//是否使用智能化分词
	private boolean useSmart;
	
	//动态字典的版本号
	private long dynamicVersion = -1;
	
	//动态词库的加载文件
	private String dynamicConf;
	
	//加载动态字典的间隔时间（单位：毫秒）
	private long inspectionTime = 30*10000;
	
	/**
	 * 分词工厂构造函数。
	 * @param args
	 */
    public IKAnalyzerTokenizerFactory(Map<String, String> args){
	    super(args);
	    
	    //初始化是否配置智能分词
	    if(args.containsKey("useSmart")) {
	    	setUseSmart(args.get("useSmart").equals("true"));
	    }else {
	    	setUseSmart(false);
	    }
	    
	    //初始化是否配置动态词库
	    if(args.containsKey("dynamicConf")) {
	    	setDynamicConf(args.get("dynamicConf"));
	    }
	    
	    //初始化动态加载字典的间隔时间
	    if(args.containsKey("inspectionTime")) {
	    	setInspectionTime(Long.valueOf(args.get("inspectionTime")));
	    }

    }
	  
    /**
     * 生产具体的IK分词器 Lucene Tokenizer适配器类。
     * 会创建多个Tokenizer对象，所以此处进行动态字典加载等动作是不合适的。
     */
	@Override
	public Tokenizer create(AttributeFactory factory, Reader reader) {
		Tokenizer _IKTokenizer = new IKTokenizer(reader, this.useSmart);
		System.out.println("生产具体的IK分词器 Lucene Tokenizer适配器类，生产Tokenizer！");
		//初始化动态字典监听器
//		DynamicDictUpdateMonitor.getInstance().addDynamicDictResourceLoaderAware(this);
	    return _IKTokenizer;
	}
	
	/**
	 * inform资源初始化加载配置资源文件的时候调用。（只会在项目启动的阶段加载，加载次数根据schema.xml中的tokenizer配置有关）。
	 * 例如：只有一个schema.xml同时配置了 index和query那么就会被加载两次，分别初始化了两个analyzer分别对应索引和查询两个过程。
	 * schema.xml配置文件如下：
	 * 
	 * <fieldType name="text_general" class="solr.TextField">
     *    <analyzer type="index">
     *      <tokenizer class="org.wltea.analyzer.lucene.ext.IKAnalyzerTokenizerFactory" useSmart="false" dynamicConf="dynamic.xml"/>
     *    </analyzer>
     *
     *    <analyzer type="query">
     *      <tokenizer class="org.wltea.analyzer.lucene.ext.IKAnalyzerTokenizerFactory" useSmart="false" dynamicConf="dynamic.xml"/>
     *    </analyzer>
     * </fieldType>
	 * 
	 */
	@Override
	public void inform(ResourceLoader resourceLoader) throws IOException {
		
		//判断动态配置字典的配件文件地址是否存在
		if(null != dynamicConf && dynamicConf.length() > 0) {
//			//初始化动态字典监听器（会加入多个更新实例）
//			DaynamicDictUpdateMonitor.getInstance().addDynamicDictResourceLoaderAware(this);
			
			//初始化动态字典监听器并只加入一个更新实例()
			DaynamicDictMonitor.getInstance(this,inspectionTime);
		}
	
	}
	
	@Override
	public void loadDaynamicDict() {
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(dynamicConf);
		Properties properties = new Properties();
		try {
			properties.loadFromXML(input);
			long tempVersion = Long.parseLong(properties.getProperty("dynamic_version","0"));
			String dynamicDictFile = properties.getProperty("dynamic_dict");
					
			if(tempVersion > dynamicVersion) {
				
				 List<String> dynamicDictFiles = new ArrayList<String>();
				 for (String file : dynamicDictFile.split("[;\\s]+")) {
					 if(null != file && file.length() > 0) {
						 dynamicDictFiles.add(file);
					 }
				 }

				//加载动态词到主词典中
				Dictionary.loadDaynamicDictToMainDict(dynamicDictFiles);
			}
			
		} catch (InvalidPropertiesFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//属性的get/set方法
    public void setUseSmart(boolean useSmart){
    	this.useSmart = useSmart;
    }

	public void setDynamicConf(String dynamicConf) {
		this.dynamicConf = dynamicConf;
	}

	public void setInspectionTime(long inspectionTime) {
		this.inspectionTime = inspectionTime;
	}
	
}
