package org.wltea.analyzer.lucene.ext;

/**
 * 动态词库加载器接口；提供统一的加载动态字典的(@code loadDynamicDict)方法接口。
 *
 * @author micheal
 * @date 2018-8-24 14:13:29 <BR/>
 */
public interface DaynamicDictLoaderAware {

	void loadDaynamicDict();

}
