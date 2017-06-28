#default installation is relative to this
JAVAMETRICS_SRC=./src
OUTPUT=./output
INSTALL_DIR=${OUTPUT}/deploy
#-------------------------------------------------------------------------------------------
#Output directories i.e. where the files are going to be built
#-------------------------------------------------------------------------------------------

JAVA_OUT=${OUTPUT}/javametrics
OMR_AGENTCORE_AGENT_OBJS=./omr-agentcore/output/agent/*.o
OMR_AGENTCORE_COMMON_OBJS=./omr-agentcore/output/common/*.o
OMR_AGENTCORE_CONNECTOR_OBJS=./omr-agentcore/output/connectors/${LIB_PREFIX}apiplugin.lib
JAVA_OBJS = ${JAVA_OUT}/javametrics.o ${JAVA_OUT}/JVMTIMemoryManager.o
JAVAMETRICS_LIB=${LIB_PREFIX}javametrics.${ARC_EXT}
JAVAMETRICS_AGENT=${JAVA_OUT}/${LIB_PREFIX}javametrics.${LIB_EXT}	
#-------------------------------------------------------------------------------------------
#Compilation / build configuration parameters
#-------------------------------------------------------------------------------------------
INCS=-Isrc
HC_EXPORT=-DEXPORT
RC_COMPILE=


default: all
#do not change the position of this include
#include ${BUILD}.mk

${JAVA_OUT}/${LIB_PREFIX}javametrics.${LIB_EXT}	: ${JAVA_OBJS}




#-------------------------------------------------------------------------------------------
#Top level targets i.e. those that can be invoked from the command line
#-------------------------------------------------------------------------------------------

default: all
#do not change the position of this include
include ${BUILD}.mk

all: setup 

#-------------------------------------------------------------------------------------------
#Libraries
#-------------------------------------------------------------------------------------------

JAVAMETRICS_REVISON = $(shell git rev-parse --short HEAD)

${JAVAMETRICS_SRC}/version.h :
	@echo "#define JAVAMETRICS_REVISON \"$(JAVAMETRICS_REVISON)\"" > ${JAVAMETRICS_SRC}/version.h

${JAVA_OUT}/javametrics.o: ${JAVAMETRICS_SRC}/version.h
	${CC} ${INCS} -I./omr-agentcore/src -I${JAVA_PLAT_INCLUDE} ${CFLAGS} -D${PLATFORM} ${OBJOPT} ${JAVAMETRICS_SRC}/javametrics.cpp

${JAVA_OUT}/JVMTIMemoryManager.o: 
	${CC} ${INCS} -I./omr-agentcore/src -I${JAVA_PLAT_INCLUDE} ${CFLAGS} -D${PLATFORM} ${OBJOPT} ${JAVAMETRICS_SRC}/ibmras/vm/java/JVMTIMemoryManager.cpp


${JAVA_OUT}/${LIB_PREFIX}javametrics.${LIB_EXT}:  ${JAVA_OBJS}
	${LINK} ${LINK_OPT} ${LIBFLAGS} ${LIB_OBJOPT} ${JAVA_OBJS}  ${OMR_AGENTCORE_AGENT_OBJS} ${OMR_AGENTCORE_COMMON_OBJS} ${LD_OPT}  ${EXELIBS} ${EXEFLAGS}
	@echo "JAVAMETRICS lib built"
	

jm: setup  ${JAVAMETRICS_AGENT} 



#-------------------------------------------------------------------------------------------
#Various install destinations
#-------------------------------------------------------------------------------------------
setup: ${OUTPUT}

${OUTPUT}:
	@echo "Creating required build directories under ${OUTPUT}"
	mkdir -p ${OUTPUT}
	mkdir -p ${JAVA_OUT}
	mkdir -p ${INSTALL_DIR}
	cd ./omr-agentcore; make BUILD=${BUILD} coreinstall

clean:
	rm -fr ${OUTPUT}
	rm -f ${JAVAMETRICS_SRC}/version.h
	cd ./omr-agentcore; rm -fr ${OUTPUT}

javametrics: jm
	@echo "installing to  ${INSTALL_DIR}"
	cp ${JAVA_OUT}/${LIB_PREFIX}javametrics.${LIB_EXT} ${INSTALL_DIR}
	cp ./omr-agentcore/output/connectors/${LIB_PREFIX}apiplugin.${LIB_EXT} ${INSTALL_DIR}
	cp ./omr-agentcore/output/plugins/${LIB_PREFIX}envplugin.${LIB_EXT} ${INSTALL_DIR}
	@echo "-----------------------------------------------------------------------------------------------------------------------"
