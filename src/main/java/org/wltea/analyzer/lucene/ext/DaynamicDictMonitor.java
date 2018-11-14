package org.wltea.analyzer.lucene.ext;

/**
 * 单例模式，只启动一个线程加入一个动态词库更新器来对动态字典进行动态加载操作；
 * 由于之前的实现{@code DynamicDictUpdateMonitor}的代码没有进行动态字典的刷新排重处理，导致schema.xml中配置了几个Analyzer就会加入几个动态
 * 字典的刷新实例；其实动态字典都是使用的同一套，所以修改之前的版本新增加{@code DynamicDictMonitor} 类，具体的搭配使用详见{@code IKTokenizerFactory}
 * 
 * @author micheal
 * @date 2018-8-27 17:03:55 <BR/>
 */
public class DaynamicDictMonitor implements Runnable {

	private static DaynamicDictMonitor singleton = null;
	
	//单位毫秒
	private long inspectionTime = 1*30*1000;
	
	private DaynamicDictLoaderAware daynamicDictLoaderAware;
	 
	private Thread worker;
	
	/**
	 * 初始化动态字典的更新监听器。
	 * 
	 * @return
	 */
	public static DaynamicDictMonitor getInstance(DaynamicDictLoaderAware daynamicDictLoaderAware,long inspectionTime) {
		if(null == singleton) {
			
			synchronized(DaynamicDictMonitor.class){
				if(null == singleton){
					singleton = new DaynamicDictMonitor();
					singleton.daynamicDictLoaderAware = daynamicDictLoaderAware;
					singleton.inspectionTime = inspectionTime;
					return singleton;
				}
			}
			
		}
		
		return singleton;
	}

	/**
	 * 构造函数初始化监听器线程。
	 */
	private DaynamicDictMonitor() {
        worker = new Thread(this);
        worker.setDaemon(true);
        worker.start();
	}

	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(inspectionTime);
				this.daynamicDictLoaderAware.loadDaynamicDict();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
