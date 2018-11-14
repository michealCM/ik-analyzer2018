package org.wltea.analyzer.lucene.ext;

import java.util.Vector;

/**
 * 由于实现的代码没有进行动态字典的刷新排重处理，导致schema.xml中配置了几个Analyzer就会加入几个动态
 * 字典的刷新实例；其实动态字典都是使用的同一套，所以修改之前的版本新增加{@code DaynamicDictMonitor} 类，具体的搭配使用详见{@code IKTokenizerFactory}
 * 
 * @author micheal
 * @date 2018-8-24 14:43:47 <BR/>
 */
@Deprecated
public class DaynamicDictUpdateMonitor implements Runnable {

	private static final DaynamicDictUpdateMonitor singleton = new DaynamicDictUpdateMonitor();
	
	//单位毫秒
	private int sleepTime = 1*30*1000;
	
	private Vector<DaynamicDictLoaderAware> daynamicDictLoaderAwares;
	 
	private Thread worker;

	/**
	 * 初始化动态字典的更新监听器。
	 * 
	 * @return
	 */
	public static DaynamicDictUpdateMonitor getInstance() {
		return singleton;
	}
	
	/**
	 * 构造函数初始化监听器线程。
	 */
	private DaynamicDictUpdateMonitor() {
        daynamicDictLoaderAwares = new Vector<DaynamicDictLoaderAware>();
        worker = new Thread(this);
        worker.setDaemon(true);
        worker.start();
	}
	
	/**
	 * 加入更新任务到更新任务队列中。
	 * 
	 * @param dictLoaderAware
	 */
	public void addDynamicDictResourceLoaderAware(DaynamicDictLoaderAware dictLoaderAware) {
		this.daynamicDictLoaderAwares.add(dictLoaderAware);
	}

	
	@Override
	public void run() {
		while(true){

			try {
				Thread.sleep(sleepTime);
				if(!daynamicDictLoaderAwares.isEmpty()){
					for(DaynamicDictLoaderAware dictLoaderAware: daynamicDictLoaderAwares){
                        dictLoaderAware.loadDaynamicDict();
	                }
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

}
