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

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

/**
 * A class file transformer registered by the javaagent
 *
 */
public class ClassTransformer implements ClassFileTransformer {

	/*
	 * Called during class loading.
	 * 
	 * Use ASM to modify class bytecode if necessary using the ClassAdaptor
	 */
	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		ClassReader cr = new ClassReader(classfileBuffer);
		ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
		ClassVisitor cv = new ClassAdapter(cw);
		cr.accept(cv, ClassReader.SKIP_FRAMES);
		return cw.toByteArray();
	}

}
