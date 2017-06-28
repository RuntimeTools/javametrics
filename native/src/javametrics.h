/*******************************************************************************
 * Copyright 2017 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

#ifndef javametrics_h
#define javametrics_h

#include "jvmti.h"

namespace ibmras {
namespace monitoring {

int setEnv(JNIEnv** env, std::string name, JavaVM* jvm, bool asDaemon);

} /* namespace monitoring */
} /* namespace ibmras */


struct jvmFunctions {
	jvmtiEnv *pti;
	JavaVM *theVM;
};

#endif /* javametrics_h */
