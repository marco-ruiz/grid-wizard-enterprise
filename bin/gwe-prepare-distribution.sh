#!/bin/sh
# GWE Client Installer 

export GWE_RUN=$GWE_DEV/run-gwe

rm -fdr $GWE_RUN
mkdir $GWE_RUN
cp $GWE_DEV/dist/gwe-*-client.zip $GWE_RUN
unzip -q `ls -d -t $GWE_RUN/gwe-*-client.zip | head -1` -d $GWE_RUN

export GWE_HOME=`ls -d $GWE_RUN/gwe-*/ | head -1`
export PATH=$GWE_HOME/bin:$PATH

cp $GWE_DEV/user/*.* $GWE_HOME/conf
