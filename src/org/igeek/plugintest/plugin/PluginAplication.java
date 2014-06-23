package org.igeek.plugintest.plugin;

import java.util.Map;

import android.app.Application;


/**
 * @author 作者 E-mail:hangxin1940@gmail.com
 * @version 创建时间：2011-12-14 上午11:18:13
 * 类说明
 */
 public abstract class  PluginAplication<K,V> extends Application {
	public abstract Map<K,V>  getDesciption();
}
