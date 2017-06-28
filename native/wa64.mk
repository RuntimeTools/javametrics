#include makefile for Windows
PLATFORM=_WINDOWS
OS=Windows
PORTDIR=windows
CC=cl.exe
GCC=cl.exe
LINK=link.exe
LINK_OPT=
LD_OPT=
HC_EXPORT=-DEXPORT
CFLAGS=/O1 -D_WINDOWS -DWINDOWS -D_64BIT -D__LITTLE_ENDIAN /EHsc -c /MD ${HC_EXPORT} -DWIN64 -DIBMRAS_DEBUG_LOGGING
OBJOPT=/Fo$(shell cygpath -am "$@")
ARCHIVE=lib -out:$(shell cygpath -am "${AGENT_LIB}")
ARCHIVE_MQTT=lib -out:$(shell cygpath -am "${MQTT_LIB}")
ARCHIVE_OSTREAM=lib -out:$(shell cygpath -am "${OSTREAM_LIB}")
JAVA_PLAT_INCLUDE=$(shell cygpath -am "${JAVA_SDK_INCLUDE}")
ARC_EXT=lib
LIB_EXT=dll
LIBFLAGS= -dll -machine:AMD64
LIB_OBJOPT=-out:$(shell cygpath -am "$@")
EXE_EXT=.exe
LIBPATH=/LIBPATH:
EXEFLAGS=
EXELIBS=Ws2_32.lib Pdh.lib
SCRIPT_NAME=launch.bat
OSTREAM_LIB_OPTIONS=${AGENT_LIB}
HC_LIB_USE=${HC_LIB}
MONAGENT=monagent
OPT_PYTHON=
ifdef PYTHON
OPT_PYTHON=--python "${PYTHON}"
endif
NODE_GYP=PATH=${shell cygpath -u ${NODE_SDK}}:$$PATH ${NODE_SDK}/node_modules/npm/bin/node-gyp-bin/node-gyp ${OPT_PYTHON}

	
install: all
	@echo "installing to ${INSTALL_DIR}"
	mkdir -p ${INSTALL_DIR}/plugins
	mkdir -p ${INSTALL_DIR}/libs
	cp ${TEST_OUT}/test${EXE_EXT} ${INSTALL_DIR}
	cp ${AGENT_OUT}/${LIB_PREFIX}monagent.${LIB_EXT} ${INSTALL_DIR}
	cp ${PLUGIN_OUT}/*.dll ${INSTALL_DIR}/plugins
	cp ${CONNECTOR_OUT}/*.dll ${INSTALL_DIR}/plugins
	@echo cd "${INSTALL_DIR}" >> ${INSTALL_DIR}/${SCRIPT_NAME}
	@echo test${EXE_EXT} "${INSTALL_DIR}/plugins" >> ${INSTALL_DIR}/${SCRIPT_NAME}
	@echo "-----------------------------------------------------------------------------------------------------------------------"
	@echo Launch script generated
	@echo "-----------------------------------------------------------------------------------------------------------------------"


nodeinstall: NODEEXELIBS=${NODE_SDK}/x64/node.lib 	
	
nodetest: nodeinstall
	cp ${SRC}/vm/node/test.js ${INSTALL_DIR}
	cd ${INSTALL_DIR} && PATH=libs:plugins:$$PATH ${NODE_SDK}/node test.js
	