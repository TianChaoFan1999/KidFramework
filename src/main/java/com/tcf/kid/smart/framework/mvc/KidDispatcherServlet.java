package com.tcf.kid.smart.framework.mvc;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.tcf.kid.smart.framework.common.Const;
import com.tcf.kid.smart.framework.helper.BeanHelper;
import com.tcf.kid.smart.framework.helper.ClassHelper;
import com.tcf.kid.smart.framework.helper.ControllerHelper;
import com.tcf.kid.smart.framework.models.Data;
import com.tcf.kid.smart.framework.models.Handle;
import com.tcf.kid.smart.framework.models.Param;
import com.tcf.kid.smart.framework.models.View;
import com.tcf.kid.smart.framework.util.CodecUtil;
import com.tcf.kid.smart.framework.util.JsonUtil;
import com.tcf.kid.smart.framework.util.PropertiesUtil;
import com.tcf.kid.smart.framework.util.ReflectUtil;
import com.tcf.kid.smart.framework.util.StreamUtil;

/***
 * TODO TCF MVC框架核心控制器，作用等效于SpringMVC的核心控制器DispatcherServlet
 * @author 71485
 *
 */
@WebServlet
public class KidDispatcherServlet extends HttpServlet{

	@Override
	public void init(ServletConfig servletConfig) throws ServletException
	{
		//TODO TCF 加载MVC框架启动核心类
		ClassHelper.loadCoreClass();
		
		//TODO TCF 注册Servlet资源服务
		ServletContext servletContext=servletConfig.getServletContext();
		
		//TODO TCF JSP视图资源
		ServletRegistration viewServletRegistration=servletContext.getServletRegistration(Const.SERVLET_REGISTRATION_TYPES.JSP);
		viewServletRegistration.addMapping(PropertiesUtil.loadPropertiesFile(Const.PROPERTIES_FILES.BASE_PROPERTIES).getProperty(Const.BASE_PROPERTIES_KEYS.VIEW_RESOURCE));
		
		//TODO TCF 其他默认静态资源
		ServletRegistration defaultServletRegistration=servletContext.getServletRegistration(Const.SERVLET_REGISTRATION_TYPES.DEFAULT);
		defaultServletRegistration.addMapping(PropertiesUtil.loadPropertiesFile(Const.PROPERTIES_FILES.BASE_PROPERTIES).getProperty(Const.BASE_PROPERTIES_KEYS.STATIC_RESOURCE));
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		//TODO TCF 请求路径
		String requestUrl=request.getServletPath();
		
		//TODO TCF 请求方式
		String requestMethod=request.getMethod()!=null?request.getMethod().toLowerCase():"";
		
		if(StringUtils.isNotEmpty(requestUrl) && StringUtils.isNotEmpty(requestMethod))
		{
			//TODO TCF 根据请求路径和请求方式加载处理请求信息
			Handle handle=ControllerHelper.getHandleByRequest(requestUrl,requestMethod);
			
			if(handle!=null)
			{
				//TODO TCF 处理请求控制器
				Class<?> handleController=handle.getHandleController();
				
				//TODO TCF 处理请求方法
				Method handleMethod=handle.getHandleMethod();
				
				if(handleController!=null && handleMethod!=null)
				{
					//TODO TCF 处理请求控制器实例
					Object handleControllerInstance=BeanHelper.getBeanInstance(handleController);
					
					//TODO TCF 所有请求参数名
					Enumeration<String> parameterNames=request.getParameterNames();
					
					if(parameterNames!=null)
					{
						//TODO TCF 封装请求参数(ParameterName:ParameterValue)
						Map<String,Object> paramMap=new HashMap<String,Object>();
						while(parameterNames.hasMoreElements())
						{
							//TODO TCF 请求参数名
							String parameterName=parameterNames.nextElement();
							
							//TODO TCF 请求参数值
							String parameterValue=request.getParameter(parameterName);
							
							paramMap.put(parameterName,parameterValue);
						}
						
						//TODO TCF 模型参数
						Param param=new Param(paramMap);
						
						//TODO TCF 请求作用域中其他请求参数(requestScope)
						String requestParameter=CodecUtil.encodeText(StreamUtil.readInputStream(request.getInputStream()));
						
						if(StringUtils.isNotEmpty(requestParameter))
						{
							String[] parameters=requestParameter.split("&");
							
							if(parameters!=null && parameters.length>0)
							{
								for(String parameter:parameters)
								{
									String[] paramPairs=parameter.split("=");
									
									if(paramPairs!=null && paramPairs.length==2)
									{
										//TODO TCF 参数名
										String paramName=paramPairs[0];
										
										//TODO TCF 参数值
										String paramValue=paramPairs[1];
										
										//TODO TCF 存入请求作用域
										request.setAttribute(paramName,paramValue);
									}
								}
							}
						}
						
						//TODO TCF 基于反射调用控制器处理方法执行
						Object invokeResult=ReflectUtil.invokeMethod(handleControllerInstance,handleMethod,param);
						
						if(invokeResult!=null)
						{
							if(invokeResult instanceof View)
							{
								//TODO TCF 返回视图解析器格式响应数据
								View view=(View)invokeResult;
								
								if(view!=null)
								{
									//TODO TCF 视图解析路径
									String viewPath=view.getViewPath();
									
									//TODO TCF 判断是转发还是重定向
									if(StringUtils.isNotEmpty(viewPath))
									{
										if(viewPath.startsWith("/"))
										{
											//TODO TCF 重定向
											response.sendRedirect(
													    PropertiesUtil.loadPropertiesFile(Const.PROPERTIES_FILES.BASE_PROPERTIES)
													    .getProperty(Const.BASE_PROPERTIES_KEYS.VIEW_RESOURCE)
													    +viewPath
												     );
										}
										else
										{
											//TODO TCF 转发
											//TODO TCF 绑定的模型参数
											Map<String,Object> modelParams=view.getModelParams();
											
											if(modelParams!=null)
											{
												for(Map.Entry<String,Object> modelParamEntry:modelParams.entrySet())
												{
													//TODO TCF 模型参数名
													String modelParamName=modelParamEntry.getKey();
													
													//TODO TCF 模型参数值
													Object modelParamValue=modelParamEntry.getValue();
													
													//TODO TCF 存入请求作用域
													request.setAttribute(modelParamName,modelParamValue);
												}
											}
											
											//TODO TCF 页面转发
											request.getRequestDispatcher(
													   PropertiesUtil.loadPropertiesFile(Const.PROPERTIES_FILES.BASE_PROPERTIES)
													   .getProperty(Const.BASE_PROPERTIES_KEYS.VIEW_RESOURCE)
													   +viewPath
													).forward(request,response);
										}
									}
								}
							}
							else if (invokeResult instanceof Data)
							{
								//TODO TCF 返回的是响应数据，JSON序列化之后直接写入输出流直接返回响应
								Data data=(Data)invokeResult;
								
								if(data!=null)
								{
									//TODO TCF 响应数据
									Object resposeData=data.getData();
									
									//TODO TCF JSON序列化
									String jsonString=JsonUtil.pojoToJson(resposeData);
									
									//TODO TCF 写入输出流后直接返回响应
									response.setContentType("application/json");
									response.setCharacterEncoding("UTF-8");
									PrintWriter writer=response.getWriter();
									
									writer.write(jsonString);
									
									//TODO TCF 清空缓冲区，关闭输出流
									writer.flush();
									writer.close();
								}
							}
						}
					}
				}
			}
		}
	}
}
