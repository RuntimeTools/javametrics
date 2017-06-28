PLATFORM=__MACH__
PORTDIR=osx
CC=g++
LINK=g++
GCC=gcc
LINK_OPT=
LD_OPT=-undefined dynamic_lookup -shared -fPIC -pthread
JAVA_PLAT_INCLUDE=${JAVA_SDK_INCLUDE}
OBJOPT=-o"$@"
ARCHIVE=ar -r 
ARCHIVE_MQTT=ar -r ${MQTT_LIB} 
ARC_EXT=a
CFLAGS=-O3 -Wall -pthread -c -fmessage-length=0 -fPIC 
LIB_EXT=dylib
EXE_EXT=
LIBFLAGS=-shared -fPIC -pthread -ldl 
LIB_OBJOPT=-o"$@"
LIBPATH=-L
EXEFLAGS=
LIB_PREFIX=lib
#ifdef NODE_SDK
NODE_GYP=PATH=${NODE_SDK}/bin:$$PATH ${NODE_SDK}/lib/node_modules/npm/bin/node-gyp-bin/node-gyp ${OPT_PYTHON}
#endif
