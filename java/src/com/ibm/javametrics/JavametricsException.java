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
package com.ibm.javametrics;

/**
 * Exception thrown by Javametrics
 */
public class JavametricsException extends RuntimeException {

	private static final long serialVersionUID = -8487122983395576783L;

	public JavametricsException() {
	}

	public JavametricsException(String message) {
		super(message);
	}

	public JavametricsException(Throwable cause) {
		super(cause);
	}

	public JavametricsException(String message, Throwable cause) {
		super(message, cause);
	}

	public JavametricsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
