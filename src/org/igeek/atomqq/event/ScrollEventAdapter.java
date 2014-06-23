package org.igeek.atomqq.event;

import java.util.LinkedList;

/**
 * 事件适配器(非线程安全)
 * @author 作者 E-mail:hangxin1940@gmail.com
 * @version 创建时间：2011-8-21 下午09:28:21
 */
public class ScrollEventAdapter {
	LinkedList<OnScrollCompleteListener> listeners;
	
	public ScrollEventAdapter() {
		listeners=new LinkedList<OnScrollCompleteListener>();
	}
	
	public void notifyEvent(ScrollEvent e){
		for(OnScrollCompleteListener l:listeners){
			l.onScrollComplete(e);
		}
	}
	
	public void addListener(OnScrollCompleteListener l){
		listeners.add(l);
	}

}
