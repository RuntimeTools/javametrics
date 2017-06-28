#include makefile for AIX
PLATFORM=_AIX
PORTDIR=aix
CC=/usr/vacpp/bin/xlC_r
LINK=/usr/vacpp/bin/xlC_r
GCC=/usr/vacpp/bin/xlC_r
LINK_OPT=-G -brtl -q32 
LD_OPT=-Wl,-bexpall,-brtllib 
JAVA_PLAT_INCLUDE=${JAVA_SDK_INCLUDE}
OBJOPT=-o"$@"
ARCHIVE_MQTT=ar -r ${MQTT_LIB} 
ARCHIVE=ar -r 
ARC_EXT=a
#CFLAGS=-g3 -c -fmessage-length=0 -DAIX -D__BIG_ENDIAN -D_AIX -DAIXPPC -D_32BIT -D__NOLOCK_ON_INPUT -qasm=gcc
#CFLAGS=-D__BIG_ENDIAN -D_AIX -DAIX -DAIXPPC -G -qasm_as=/usr/bin/as -mcpu=powerpc -qasm=gcc -q32 -DREVERSED -D__NOLOCK_ON_INPUT -D__NOLOCK_ON_OUTPUT
CFLAGS=-D__BIG_ENDIAN -qarch=ppc -D_AIX -DAIX -DAIXPPC -G -qpic -c -q32 -DREVERSED -qasm_as=/usr/bin/as -qasm=gcc -Os -s
LIB_EXT=so
EXE_EXT=
LIBFLAGS=-G -q32 
LIB_OBJOPT=-o"$@"
LIBPATH=-L
EXEFLAGS=
LIB_PREFIX=lib
#ifdef NODE_SDK
NODE_GYP=PATH=${NODE_SDK}/bin:$$PATH ${NODE_SDK}/lib/node_modules/npm/bin/node-gyp-bin/node-gyp ${OPT_PYTHON}
#endif