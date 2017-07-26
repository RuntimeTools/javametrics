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
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Java instrumentation agent
 * 
 * Invoked by the -javaagent:jarpath=[options] command line parameter Entry
 * point is defined in the jar manifest as: Premain-Class:
 * com.ibm.javametrics.instrument.Agent
 *
 */
public class Agent {

    private static final String CLASSTRANSFORMER_CLASS = "com.ibm.javametrics.instrument.ClassTransformer";

    private static final String JAR_URL = ".jar!/";
    private static final String JAVAMETRICS = "javametrics";

    private static final String ASM_VERSION = "5.0.4";
    private static final String ASM_JAR_URL = "asm/asm-" + ASM_VERSION + ".jar!/";
    private static final String ASM_COMMONS_JAR_URL = "asm/asm-commons-" + ASM_VERSION + ".jar!/";
    
    private static final String DEBUG_PROPERTY = "com.ibm.javametrics.javaagent.debug";
    private static final String OSGI_BOOTDELEGATION_PROPERTY = "org.osgi.framework.bootdelegation";
    private static final String BOOTDELEGATION_PACKAGES = "com.ibm.javametrics.instrument";

    public static final boolean debug = System.getProperty(DEBUG_PROPERTY, "false").equals("true");

    /**
     * Entry point for the agent via -javaagent command line parameter
     * 
     * @param agentArgs
     * @param inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        /*
         * To work in OSGI environment we need to expose our packages to the
         * boot ClassLoader
         */
        try {
            String bootDelegation = System.getProperty(OSGI_BOOTDELEGATION_PROPERTY, "");
            if (!bootDelegation.equals("")) {
                bootDelegation += ",";
            }
            bootDelegation += BOOTDELEGATION_PACKAGES;
            System.setProperty(OSGI_BOOTDELEGATION_PROPERTY, bootDelegation);
        } catch (Exception e) {
        }
        
        /*
         * We need to keep the ASM jars off the bootclasspath so we will load
         * our ClassTransformer with our own classloader so that subsequent load
         * of ASM classes are from our packaged jars
         */
        try {
            /*
             * Determine the url to our jar lib folder
             */
            String jarUrl = (Agent.class.getResource("Agent.class").toString());

            int jarIndex = jarUrl.indexOf(JAR_URL);
            if (jarIndex == -1) {
                System.err
                        .println("Javametrics: Unable to start javaagent: Agent class not loaded from jar: " + jarUrl);
                return;
            }

            /*
             * Determine root url and name of our jar
             */
            String libUrl = jarUrl.substring(0, jarIndex);
            int jmIndex = libUrl.lastIndexOf(JAVAMETRICS);
            libUrl = jarUrl.substring(0, jmIndex);
            String jarName = jarUrl.substring(jmIndex, jarIndex + JAR_URL.length());
            URL[] urls = { new URL(libUrl + jarName), new URL(libUrl + ASM_JAR_URL),
                    new URL(libUrl + ASM_COMMONS_JAR_URL) };
            URLClassLoader ucl = new URLClassLoader(urls) {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.ClassLoader#loadClass(java.lang.String)
                 * 
                 * Find class from our jars first before delegating to parent
                 */
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    try {
                        return findClass(name);
                    } catch (ClassNotFoundException cnf) {
                    }
                    return super.loadClass(name);
                }
            };
            Class<?> cl = ucl.loadClass(CLASSTRANSFORMER_CLASS);

            // Register our class transformer
            inst.addTransformer((ClassFileTransformer) cl.newInstance());

        } catch (NoClassDefFoundError ncdfe) {
            System.err.println("Javametrics: Unable to start javaagent: " + ncdfe);
            ncdfe.printStackTrace();
        } catch (Exception e) {
            System.err.println("Javametrics: Unable to start javaagent: " + e);
            e.printStackTrace();
        }
    }

    /**
     * @param agentArgs
     * @param inst
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    };
}
