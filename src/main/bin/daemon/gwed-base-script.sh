#!/bin/sh
# Abstract script to run a main class from the gwe fatjar 
#
# $1 = If numeric then treated as a port for debugging the JVM - then shift parameters and resume with the following logic:
#
# $1 = class name of the class with the main
# $2 = gwe home
# $3... = application specific arguments. 

DEBUG=
if [ $(echo "$1" | grep -E "^[0-9]+$") ]; then
	DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=$1
	shift;
fi;

java -Xmx512m $DEBUG -cp $2/conf:$2/lib:$2/lib/gwe-${pom.version}-core.jar:$2/lib/gwe-${pom.version}-dep.jar: $@
