/*******************************************************************************
 * Copyright 2017 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.ibm.javametrics.instrument;

import java.util.HashSet;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * An instance of this ClassVisitor is created for each class being loaded.
 *
 */
public class ClassAdapter extends ClassVisitor implements Opcodes {

	String className;

	/*
	 * HTTP request instrumentation fields
	 */
	private static final String HTTP_REQUEST_METHOD_DESC = "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V";
	private static final String HTTP_SERVLET_CLASS = "javax/servlet/http/HttpServlet";
	private static final String HTTP_JSP_INTERFACE = "javax/servlet/jsp/HttpJspPage";
	private boolean httpInstrumentServlet = false;
	private boolean httpInstrumentJsp = false;

	static HashSet<String> jspImplementers = new HashSet<String>();
	static HashSet<String> servletExtenders = new HashSet<String>();

	static {
		servletExtenders.add(HTTP_SERVLET_CLASS);
	}

	/**
	 * @param cv
	 */
	public ClassAdapter(ClassVisitor cv) {
		super(ASM5, cv);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassVisitor#visit(int, int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[])
	 * 
	 * First visitor method called during class parsing.
	 * 
	 * Here we determine which classes/methods to instrument. This is currently
	 * hard-coded in this class but could be implemented via configuration
	 * files.
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

		className = name;

		visitHttp(version, access, name, signature, superName, interfaces);

		super.visit(version, access, name, signature, superName, interfaces);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String[]) Called for each
	 * method in the class.
	 * 
	 * We return a new instance of a class specific MethodVisitor for each
	 * method we need to instrument.
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

		mv = visitHttpMethod(mv, access, name, desc, signature, exceptions);

		return mv;
	}

	/**
	 * Check if HTTP request instrumentation is required
	 * 
	 * Servlets: instrument any class that is a subclass of HttpServlet
	 * 
	 * JSP pages: instrument any class that implements the HttpJspPage interface
	 * 
	 * NOTE: This assumes the superclasses are visited first which appears to be
	 * the case
	 * 
	 * @param version
	 * @param access
	 * @param name
	 * @param signature
	 * @param superName
	 * @param interfaces
	 */
	private void visitHttp(int version, int access, String name, String signature, String superName,
			String[] interfaces) {

		/*
		 * Instrument implementers of javax/servlet/jsp/HttpJspPage
		 */
		if (interfaces != null) {
			for (String iface : interfaces) {
				if (HTTP_JSP_INTERFACE.equals(iface)) {
					jspImplementers.add(name);
					httpInstrumentJsp = true;

					if (Agent.debug) {
						System.err.println("Javametrics: " + name + " implements " + HTTP_JSP_INTERFACE);
					}
				}
			}
		}

		/*
		 * Instrument classes that extend/override implementers of
		 * javax/servlet/jsp/HttpJspPage
		 */
		if (jspImplementers.contains(superName)) {
			jspImplementers.add(name);
			httpInstrumentJsp = true;

			if (Agent.debug) {
				System.err.println(
						"Javametrics: " + name + " extends " + superName + " that implements " + HTTP_JSP_INTERFACE);
			}
		}

		/*
		 * Instrument classes that extend javax/servlet/http/HttpServlet but not
		 * if it is already instrumented as a JSP
		 */
		if (!httpInstrumentJsp && servletExtenders.contains(superName)) {
			servletExtenders.add(name);
			httpInstrumentServlet = true;
			if (Agent.debug) {
				System.err.println("Javametrics: " + name + " extends " + superName);
			}
		}
	}

	/**
	 * Instrument HTTP request methods
	 * 
	 * @param mv
	 *            original MethodVisitor
	 * @param access
	 * @param name
	 * @param desc
	 * @param signature
	 * @param exceptions
	 * @return original MethodVisitor or new MethodVisitor chained to original
	 */
	private MethodVisitor visitHttpMethod(MethodVisitor mv, int access, String name, String desc, String signature,
			String[] exceptions) {

		MethodVisitor httpMv = mv;

		/*
		 * Instrument _jspService method for JSP. Instrument doGet, doPost and
		 * service methods for servlets.
		 */
		if ((httpInstrumentJsp && name.equals("_jspService")) || (httpInstrumentServlet
				&& (name.equals("doGet") || name.equals("doPost") || name.equals("service")))) {

			// Only instrument if method has the correct signature
			if (HTTP_REQUEST_METHOD_DESC.equals(desc)) {
				httpMv = new ServletCallBackAdapter(className, mv, access, name, desc);
			}
		}

		return httpMv;
	}

}
