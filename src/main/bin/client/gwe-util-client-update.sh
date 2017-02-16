#!/bin/sh
# GWE client updater from a previously installed version. Expects:
# 	- $GWE_HOME of the previously installed version as parameter
# 	- $GWE_HOME set to the location of the newly installed GWE version

cp $1/conf/*.* $GWE_HOME/conf
