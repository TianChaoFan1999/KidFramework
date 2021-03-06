package com.tcf.kid.smart.framework.helper;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.tcf.kid.smart.framework.annotation.KidAction;
import com.tcf.kid.smart.framework.annotation.KidController;
import com.tcf.kid.smart.framework.models.Handle;
import com.tcf.kid.smart.framework.models.Request;

/***
 * TODO TCF 类加载器助手类
 * TODO TCF 初始化加载请求和处理之间的映射关系Map(Request -> Handle)
 * TODO TCF 根据请求路径和请求方式获取处理请求信息
 * @author 71485
 *
 */
public class ControllerHelper {

	//TODO TCF 全局请求和处理之间的映射关系Map(Request -> Handle)
	public static Map<Request,Handle> MAPPING_MAP=new HashMap<Request,Handle>();
	
	//TODO TCF 初始化加载请求和处理之间的映射关系Map(Request -> Handle)
	static
	{
		initRequestAndHandle();
	}
	
	//TODO TCF 加载请求和处理之间的映射关系Map(Request -> Handle)
	public static void initRequestAndHandle()
	{
		//TODO TCF 加载所有使用KidController注解标注的MVC控制器
		Set<Class<?>> controllerClassList=ClassHelper.loadClassByAnnotation(KidController.class);
		
		if(controllerClassList!=null && controllerClassList.size()>0)
		{
			for(Class<?> controllerClass:controllerClassList)
			{
				//TODO TCF 控制器中定义的所有处理请求方法
				Method[] methods=controllerClass.getDeclaredMethods();
			
			    if(methods!=null && methods.length>0)
			    {
			    	for(Method method:methods)
			    	{
			    		//TODO TCF 只加载使用KidAction注解标注的控制器处理请求方法
			    		if(method.isAnnotationPresent(KidAction.class))
			    		{
			    			KidAction kidAction=method.getAnnotation(KidAction.class);
			    			
			    			if(kidAction!=null)
			    			{
			    				//TODO TCF 拦截请求路径
			    				String requestUrl=kidAction.url();
			    				
			    				//TODO TCF 拦截请求方式
			    				String requestMethod=kidAction.method()!=null?kidAction.method().toLowerCase():"";
			    				
			    				//TODO TCF 建立请求和处理之间的映射Map
			    				Request request=new Request(requestUrl,requestMethod);
			    				
			    				Handle handle=new Handle(controllerClass,method);
			    				
			    				MAPPING_MAP.put(request,handle);
			    			}
			    		}
			    	}
			    }
			}
		}
	}
	
	//TODO TCF 根据请求路径和请求方式获取处理请求信息
	public static Handle getHandleByRequest(String requestUrl,String requestMethod)
	{
		Handle result=null;
		
		try
		{
			if(MAPPING_MAP!=null)
			{
				for(Map.Entry<Request,Handle> mappingEntry:MAPPING_MAP.entrySet())
				{
					//TODO TCF 请求
					Request request=mappingEntry.getKey();
					
					//TODO TCF 处理
					Handle handle=mappingEntry.getValue();
					
					if(request!=null)
					{
						if(StringUtils.isNotEmpty(request.getRequestUrl()) && StringUtils.isNotEmpty(request.getRequestMethod())
						&& request.getRequestUrl().equals(requestUrl) && request.getRequestMethod().equals(requestMethod))
						{
							result=handle;
							break;
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
}
