#!/bin/sh
# Upgrades a Slicer installation to GWE Enabled 

cp -r $GWE_HOME $SLICER_HOME
export GWE_HOME=`ls -d $SLICER_HOME/gwe-*/ | head -1`
$GWE_HOME/bin/gwe-util-slicer-CLMPs-generator.sh
