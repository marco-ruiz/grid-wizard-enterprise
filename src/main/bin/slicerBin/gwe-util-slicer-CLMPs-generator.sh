#!/bin/sh
# Generate CLMPs inside a specific Slicer3 installation 

#SLICER_HOME=$1
$GWE_HOME/bin/gwe-base-script.sh org.gwe.integration.slicer.GWEEnabledCLMCreatorApp
chmod a+x $SLICER_HOME/lib/Slicer3/Plugins/gweCLMP-*.sh
