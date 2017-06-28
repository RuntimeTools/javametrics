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
package com.ibm.javametrics.dataproviders;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

/**
 * Uses MXBeans to get CPU statistics
 *
 */
public class CPUDataProvider {

	/**
	 * Get the system CPU usage
	 */
	public static double getSystemCpuLoad() {
		return getCpuLoad(true);
	}

	/**
	 * Get the Java process CPU usage
	 */
	public static double getProcessCpuLoad() {
		return getCpuLoad(false);
	}

	private static double getCpuLoad(boolean system) {
		String cpuLoadMethod = system ? "getSystemCpuLoad" : "getProcessCpuLoad";
		Class<?> noparams[] = {};
		double result = -1;

		if (!(System.getProperty("java.vm.vendor").contains("IBM"))) { //$NON-NLS-1$ //$NON-NLS-2$
			Class<?> sunBeanClass;
			try {
				sunBeanClass = Class.forName("com.sun.management.OperatingSystemMXBean"); //$NON-NLS-1$

				Object sunBean = ManagementFactory.getOperatingSystemMXBean();

				Method sunMethod = sunBeanClass.getDeclaredMethod(cpuLoadMethod, noparams); // $NON-NLS-1$
				Double sunResult = (Double) sunMethod.invoke(sunBean, (Object[]) null);
				if (sunResult != null) {
					result = sunResult.doubleValue();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (System.getProperty("java.vm.vendor").contains("IBM")) { //$NON-NLS-1$ //$NON-NLS-2$
			try {
				Class<?> ibmBeanClass = Class.forName("com.ibm.lang.management.OperatingSystemMXBean"); //$NON-NLS-1$

				Object ibmBean = ManagementFactory.getOperatingSystemMXBean();

				Method ibmMethod = ibmBeanClass.getDeclaredMethod(cpuLoadMethod, noparams); // $NON-NLS-1$

				Double ibmResult = (Double) ibmMethod.invoke(ibmBean, (Object[]) null);
				if (ibmResult != null) {
					result = ibmResult.doubleValue();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}
