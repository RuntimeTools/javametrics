#include makefile for ARM
PLATFORM=_LINUX
OS=Linux
PORTDIR=linux
JAVA_PLAT_INCLUDE=${JAVA_SDK_INCLUDE}
CC=/buildtools/arm/xcompiler/tools-master/arm-bcm2708/arm-bcm2708hardfp-linux-gnueabi/bin/arm-bcm2708hardfp-linux-gnueabi-g++
GCC=/buildtools/arm/xcompiler/tools-master/arm-bcm2708/arm-bcm2708hardfp-linux-gnueabi/bin/arm-bcm2708hardfp-linux-gnueabi-gcc
LINK=/buildtools/arm/xcompiler/tools-master/arm-bcm2708/arm-bcm2708hardfp-linux-gnueabi/bin/arm-bcm2708hardfp-linux-gnueabi-g++
LINK_OPT=-Wl,--no-as-needed
LINK_PLUG=
LD_OPT=
OBJOPT=-o"$@"
ARCHIVE=ar -r
ARCHIVE_MQTT=ar -r ${MQTT_LIB} 
ARCHIVE_OSTREAM=ar -r ${OSTREAM_LIB} 
ARC_EXT=a
CFLAGS=-Os -Wall -s -c -fmessage-length=0 -fPIC -DLINUX -DIBMRAS_DEBUG_LOGGING -D_ARM
LIB_EXT=so
EXE_EXT=
LIBFLAGS=-shared -lpthread -ldl -Wl,--gc-sections
LIB_OBJOPT=-o"$@"
LIBPATH=-L
EXEFLAGS= -lrt 
EXELIBS=
SCRIPT_NAME=launch.sh
HC_LIB_USE=-lhealthcenter
LIB_PREFIX=lib
MONAGENT=-lmonagent
OPT_PYTHON=
ifdef PYTHON
OPT_PYTHON=--python "${PYTHON}"
endif
ifdef NODE_SRC
NODE_GYP=PATH=${NODE_SRC}/out/Release:$$PATH ${NODE_SRC}/deps/npm/bin/node-gyp-bin/node-gyp ${OPT_PYTHON}
endif
ifdef NODE_SDK
NODE_GYP=PATH=${NODE_SDK}/bin:$$PATH ${NODE_SDK}/lib/node_modules/npm/bin/node-gyp-bin/node-gyp ${OPT_PYTHON}
endif

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#Path to OMR include directories (omr-linux_x86-64)
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
OMR_CORE="${PYTHON_DIR}/source-python-3.4-omr-linux_x86-64-20140603_201904/bld_linux_x86-64/omr/include_core"
PYT_INCS=-I"${PYTHON_DIR}/source-python-3.4-omr-linux_x86-64-20140603_201904/bld_linux_x86-64/cpython/Include" -I"${PYTHON_DIR}/python-3.4-omr-linux_x86-64-20140603_201904/include/python3.4m"

	
install: all
	@echo "installing to  ${INSTALL_DIR}"
	mkdir -p ${INSTALL_DIR}/plugins
	mkdir -p ${INSTALL_DIR}/libs
	cp ${TEST_OUT}/test${EXE_EXT} ${INSTALL_DIR}
	cp ${AGENT_OUT}/${LIB_PREFIX}monagent.${LIB_EXT} ${INSTALL_DIR}
	cp ${PLUGIN_OUT}/*.so ${INSTALL_DIR}/plugins
	cp ${CONNECTOR_OUT}/*.so ${INSTALL_DIR}/plugins
	@echo "#!/bin/sh" > ${INSTALL_DIR}/${SCRIPT_NAME}
	@echo 'export LD_LIBRARY_PATH=.:${INSTALL_DIR}/plugins:${INSTALL_DIR}/libs' >> ${INSTALL_DIR}/${SCRIPT_NAME}
	@echo "cd ${INSTALL_DIR}" >> ${INSTALL_DIR}/${SCRIPT_NAME}
	@echo "./test${EXE_EXT} ${INSTALL_DIR}/plugins" >> ${INSTALL_DIR}/${SCRIPT_NAME}
	chmod 777 ${INSTALL_DIR}/${SCRIPT_NAME}
	@echo "-----------------------------------------------------------------------------------------------------------------------"
	

nodetest: nodeinstall
	cp ${SRC}/vm/node/test.js ${INSTALL_DIR}
	cd ${INSTALL_DIR}; LD_LIBRARY_PATH=.:libs:plugins ${NODE_SDK}/bin/node test.js
	
nodetestonly:
	cd ${INSTALL_DIR}; LD_LIBRARY_PATH=.:libs:plugins ${NODE_SDK}/bin/node test.js
	