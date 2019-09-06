package com.tcf.kid.smart.framework.aop;

/***
 * TODO TCF AOP������࣬ʵ�ִ���ӿ�
 * TODO TCF ����ÿ��AOP����ʵ������Ҫ���õĴ����������������ǿ���������̳и�����Զ�������������д
 * TODO TCF �ڴ���������Ŀ�귽��ִ��ǰ��̬֯����ǿ����
 * @author 71485
 *
 */
public class ProxyAspect implements Proxy{

	//TODO TCF ���������壬����ÿ������ʵ������Ҫ���õĴ��������ڸ÷�����֯����ǿ����
	@Override
	public Object doProxy(ProxyChain proxyChain) 
	{
		Object invokeResult=null;
		
		try
		{
			//TODO TCF ���㺺��ģʽ֯����ǿ����
			before();
			
			invokeResult=proxyChain.doProxyChain();
			
			afterReturning();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return invokeResult;
	}
	
	//TODO TCF ...�Զ��������ǿ������
	//TODO TCF ǰ����ǿ
	public void before()
	{
		System.out.println("====��������ǰ����ǿ������ִ��====");
	}
	
	//TODO TCF ������ǿ
	public void afterReturning()
	{
		System.out.println("====�������ĺ�����ǿ������ִ��====");
	}
}
