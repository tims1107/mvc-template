package com.spectra.model;

import com.spectra.scorpion.framework.util.ProxyHandler;

import java.lang.reflect.Proxy;

public class HL7MessageFactory {
	public static HL7Message getHL7v251Message(){
		//return (HL7Message) Proxy.newProxyInstance(HL7Message.class.getClassLoader(), new Class[]{ HL7Message.class }, new ProxyHandler(new HL7v251MessageImpl()));
		return null;
	}
	
	public static HL7Message getHL7v23Message(){
		return (HL7Message) Proxy.newProxyInstance(HL7Message.class.getClassLoader(), new Class[]{ HL7Message.class }, new ProxyHandler(new HL7v23MessageImpl()));
	}
}
