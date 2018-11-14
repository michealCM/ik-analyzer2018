对IK分词器2012_FF版本的修改；
1.继承TokenizerFactory新增加IKAnalyzerTokenizerFactory类实现智能分词的可配置；
2.实现ResourceLoaderAware接口，扩展实现动态更新自定义扩展词的功能。

schema.xml中的配置方式：

   <fieldType name="text_general" class="solr.TextField">
   		<analyzer type="index">
   			<tokenizer class="org.wltea.analyzer.lucene.ext.IKAnalyzerTokenizerFactory" useSmart="true"/>
   		</analyzer>
   
   		<analyzer type="query">
   			<tokenizer class="org.wltea.analyzer.lucene.ext.IKAnalyzerTokenizerFactory" useSmart="false" dynamicConf="dynamic.xml"/>
   		</analyzer>
   	</fieldType>
   	
   	1.是否是否智能化分词配置“useSmart”
   	  false：使用最细粒度分词；true：使用智能分词；
   	  
   	    二者的分词效果如下：输入"中国人"
   	    智能化分词："中国人"     
   	  最细粒度分词："中国人" "中国" "国人"         
   	  
   	  
   	 2.是否配置支持动态加载扩展词 "dynamicConf"
   	   dynamicConf="dynamic.xml"
   	   
   	   动态词典（dynamic.xml）加载配置实例：
   	       <?xml version="1.0" encoding="UTF-8"?>
           <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">  
           <properties>  
       	      <comment>IK Analyzer动态字典配置</comment>
       	
       	      <!--动态字典的版本号 -->
              <entry key="dynamic_version">1</entry> 
       	
           	  <!-- 用户自定义配置的动态扩展字典，及修改字典中的词 jvm会自动感知并加载 -->
       	      <entry key="dynamic_dict">dynamic.dic;</entry> 
           </properties>