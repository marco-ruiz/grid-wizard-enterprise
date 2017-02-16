#!/bin/sh
# Abstract script to run a main class from the gwe fatjar 

GWE_CP=$GWE_HOME/conf:$GWE_HOME/lib:$GWE_HOME/lib/gwe-${pom.version}-core.jar:$GWE_HOME/lib/gwe-${pom.version}-dep.jar
#if [ `cd $GWE_HOME/bin && java WindowsTester` == "1" ]; then GWE_CP=`cygpath -wp $GWE_CP`; fi;

#java -Xmx512m -agentlib:jdwp=transport=dt_socket,server=y,address=8888 -cp $GWE_CP $@
java -Xmx512m -cp $GWE_CP $@
