package org.igeek.plugintest.main;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.igeek.atomqq.event.OnScrollCompleteListener;
import org.igeek.atomqq.event.ScrollEvent;


import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class AndoirdpluginActivity extends ActivityGroup implements OnClickListener ,OnScrollCompleteListener{
	private LinearLayout llMainLayout;
	private WorkSpace wkMain;
	private Button btnFindPlugins;
	private CheckBox chbAttachMain;
	
	private LocalActivityManager m_ActivityManager;
	
	private List<PluginBean> plugins;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        llMainLayout=(LinearLayout) findViewById(R.id.main_llMainLayout);
        wkMain=(WorkSpace) findViewById(R.id.main_wkMain);
        btnFindPlugins=(Button) findViewById(R.id.main_btnFindPlugins);
        chbAttachMain=(CheckBox) findViewById(R.id.main_chbAttachMain);
        
        m_ActivityManager = getLocalActivityManager();
        
        wkMain.setOnScrollCompleteLinstenner(this);
        btnFindPlugins.setOnClickListener(this);
        
        
        
        
    }


	@Override
	public void onClick(View v) {
		attachPlugin(findPlugins());
		btnFindPlugins.setVisibility(View.GONE);
	}
	
	/**
	 * 加载插件列表
	 * @param plugins
	 */
	private void attachPlugin(final List<PluginBean> plugins){
		Log.e("ydt", "   ");
		Log.e("ydt", "----- 列出插件");
		this.plugins=plugins;
		for(final PluginBean plugin:plugins){
			Button btn=new Button(this);
			btn.setTextColor(Color.RED);
			btn.setText(plugin.getLabel());
			
			llMainLayout.addView(btn);
			//添加事件
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					boolean isAttack=chbAttachMain.isChecked();
					
					
					
					
					Intent it=new Intent();
					it.setAction(plugin.getPakageName());
					
					//是否附加为view
					if(isAttack){
						//这里偷下懒，这是演示插件作为view附加到主程序中的
						for(PluginBean plugin:plugins){
							
							Intent itt=new Intent();
							itt.setAction(plugin.getPakageName());
							ViewGroup view=(ViewGroup) (m_ActivityManager.startActivity("", itt)).getDecorView();
							wkMain.addView(view);
							
							
							
						}
						//一次性附加完毕算了，然后把按钮都删了，看着清净，这几个不是重点
						llMainLayout.removeAllViews();
						chbAttachMain.setVisibility(View.GONE);
						wkMain.setToScreen(0);
					}else{
						//这里，不会把插件的窗体附加到主程序中，纯粹无用的演示
						startActivity(it);
					}
				}
			});
			
			
		}
	}
	
	/**
	 * 查找插件
	 * @return
	 */
	private List<PluginBean> findPlugins(){
		
		List<PluginBean> plugins=new ArrayList<PluginBean>();
		
		
		//遍历包名，来获取插件
		PackageManager pm=getPackageManager();
		
		
		List<PackageInfo> pkgs=pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for(PackageInfo pkg	:pkgs){
			//包名
			String packageName=pkg.packageName;
			String sharedUserId= pkg.sharedUserId;
			
			//sharedUserId是开发时约定好的，这样判断是否为自己人
			if(!"org.igeek.plugintest".equals(sharedUserId)||"org.igeek.plugintest.main".equals(packageName))
				continue;
			
			
			//进程名
			String prcessName=pkg.applicationInfo.processName;
			
			//label，也就是appName了
			String label=pm.getApplicationLabel(pkg.applicationInfo).toString();
			
			
			PluginBean plug=new PluginBean();
			plug.setLabel(label);
			plug.setPakageName(packageName);
			
			plugins.add(plug);
		}
		
		
		return plugins;
        
	}

 
	/**
	 * WorkSpace滚动到那个屏，会触发这个事件
	 * 而worksapce中每一屏又是一个插件
	 * 这个事件是用来列出当前屏幕插件所提供的应用，并且让用户调用
	 */
	@Override
	public void onScrollComplete(final ScrollEvent e) {
		try {
			final Context context = createPackageContext(plugins.get(e.curScreen).getPakageName(), Context.CONTEXT_INCLUDE_CODE|Context.CONTEXT_IGNORE_SECURITY);
			llMainLayout.removeAllViews();
			//这几行，通过反射获取了当前插件的描述信息，如同大部分框架的xml一样，这里算是模拟了一下IOC控制反转
			Class clazz=context.getClassLoader().loadClass(plugins.get(e.curScreen).getPakageName()+".PluginApplication");
			Object o=clazz.newInstance();
			Map<String,List<String>>  r=(Map<String, List<String>>) clazz.getMethod("getDesciption").invoke(o);
			List<String> classes=r.get("classes");
			List<String> methods=r.get("methods");
			
			
			//这里，根据获得的插件所提供的功能，来生成几个按钮显示，供我们调用
			for(final String clas:classes){
				for(final String method:methods){
					Button btn=new Button(this);
					
					btn.setText(clas+" -> "+method+" 执行");
					
					
					//点击后，就执行插件所提供的方法
					btn.setOnClickListener(new OnClickListener() {
						
						
						@Override
						public void onClick(View v) {
							try {
								Class c=context.getClassLoader().loadClass(plugins.get(e.curScreen).getPakageName()+"."+clas);
								Object o1=c.newInstance();
								
								//这里注意，context实际上就是句柄，这里如果涉及到窗体，plugin的句柄其实是不行的，因为它没有可以
								//依附的窗体
								
								//这个context是plugin的，通过测试，dialog这类行不通,Toast是可以的，因为
								//Toast是依附于屏幕主窗口的
								//c.getMethod(method,Context.class).invoke(o1,context); 
														
								//这里则传递的是主程序的句柄
								c.getMethod(method,Context.class).invoke(o1,AndoirdpluginActivity.this);
							
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
						}
					});
					llMainLayout.addView(btn);
				}
			}
			
			
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
	}
}