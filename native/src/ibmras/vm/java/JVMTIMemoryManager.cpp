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

#include "ibmras/vm/java/JVMTIMemoryManager.h"
#include "ibmras/common/logging.h"
#include <string.h>

namespace ibmras {
namespace vm {
namespace java {

IBMRAS_DEFINE_LOGGER("jvmtimemory")
;

JVMTIMemoryManager::JVMTIMemoryManager(jvmtiEnv* env) :
		jvmti(env) {
}

JVMTIMemoryManager::~JVMTIMemoryManager() {
}

unsigned char* JVMTIMemoryManager::allocate(uint32 size) {

	unsigned char* memory = NULL;

	jvmtiError rc = jvmti->Allocate(size, (unsigned char**) &memory);
	if (rc != JVMTI_ERROR_NONE) {
		IBMRAS_LOG_1(warning, "Failed to allocate memory of size %d", size);
		return NULL;
	}

	memset(memory, 0, size);

	IBMRAS_DEBUG_2(debug, "Allocated %d at %p", size, (void*)memory);

	return memory;
}

void JVMTIMemoryManager::deallocate(unsigned char** memoryPtr) {

	IBMRAS_DEBUG_1(debug, "Deallocate called for %p", (void*)memoryPtr);

	if (memoryPtr != NULL && *memoryPtr != NULL) {
		IBMRAS_DEBUG_1(debug, "Deallocating memory at %p", (void*)*memoryPtr);
		jvmtiError rc = jvmti->Deallocate(*memoryPtr);
		if (rc != JVMTI_ERROR_NONE) {
			IBMRAS_LOG_1(warning, "Failed to deAllocate memory at %p",(void*)*memoryPtr);
		}

		*memoryPtr = NULL;
	}
}

}
} /* namespace vm */
} /* namespace ibmras */

