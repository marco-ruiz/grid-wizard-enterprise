#!/bin/sh
# Script to deploy GWE related bundles
#	$1          ---> Version Id to deploy

GWE_VERSION=$1
DEST=/var/www/sites/www.gridwizardenterprise.org/maven/org/gwe/client/${GWE_VERSION}/
MK_DIR_CMD="mkdir -p /var/www/sites/www.gridwizardenterprise.org/maven/org/gwe/client/${GWE_VERSION}"
echo ${MK_DIR_CMD}
ssh mruiz@www.gridwizardenterprise.org ${MK_DIR_CMD}
scp ${GWE_DEV}/dist/gwe-${GWE_VERSION}-client.* mruiz@www.gridwizardenterprise.org:${DEST}
scp ${GWE_DEV}/dist/GSlicer3/* mruiz@www.gridwizardenterprise.org:${DEST}
