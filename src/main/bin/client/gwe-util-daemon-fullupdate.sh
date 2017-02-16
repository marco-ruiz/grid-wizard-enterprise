#!/bin/sh
# Installs daemon in remote cluster head node 

$GWE_HOME/bin/gwe-base-script.sh org.gwe.app.client.admin.DaemonSetupApp install $@
