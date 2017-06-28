#include makefile for AIX
PLATFORM=_AIX
PORTDIR=aix
CC=/usr/vacpp/bin/xlC_r
LINK=/usr/vacpp/bin/xlC_r
GCC=/usr/vacpp/bin/xlC_r
LINK_OPT=-G -q64 -brtl
LD_OPT=-Wl,-bexpall,-brtllib
JAVA_PLAT_INCLUDE=${JAVA_SDK_INCLUDE}
OBJOPT=-o"$@"
ARCHIVE=ar -r 
ARCHIVE_MQTT=ar -r ${MQTT_LIB} 
ARC_EXT=a
#CFLAGS=-g3 -c -fmessage-length=0 -DAIX -D__BIG_ENDIAN -D_AIX -DAIXPPC -D_64BIT -D__NOLOCK_ON_INPUT -qasm=gcc
#CFLAGS=-D__BIG_ENDIAN -D_AIX -DAIX -DAIXPPC -D_64BIT -G -qasm_as=/usr/bin/as -mppc64 -q64 -qarch=ppc -qasm=gcc
CFLAGS=-D__BIG_ENDIAN -D_AIX -DAIX -DAIXPPC -D_64BIT -G  -q64 -qpic  -qarch=ppc64 -DREVERSED -c -qasm=gcc -qasm_as="/usr/bin/as -a64" -Os -s
LIB_EXT=so
EXE_EXT=
LIBFLAGS=-G -q64 -G
LIB_OBJOPT=-o"$@"
LIBPATH=-L
EXEFLAGS=
LIB_PREFIX=lib
#ifdef NODE_SDK
NODE_GYP=PATH=${NODE_SDK}/bin:$$PATH ${NODE_SDK}/lib/node_modules/npm/bin/node-gyp-bin/node-gyp ${OPT_PYTHON}
#endif