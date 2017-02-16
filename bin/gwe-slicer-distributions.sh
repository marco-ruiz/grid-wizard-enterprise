#!/bin/sh
# Slicer+GWE Bundler 
# Takes as parameter the root of the development environment where:
#	$1/dist          ---> contains a "gwe-${GWE_VERSION}-client.zip" distribution bundle
# 	$1/dist-slicer   ---> contains Slicer distribution bundles and/or Slicer distribution unbundled folders (to be merged)
# 	$1/dist/GSlicer3 ---> destination folder where this script will create the GSlicer merged bundles.

export GWE_DEV=$1
export SLICER_DIST=$GWE_DEV/dist-slicer
export GWE_DIST=$GWE_DEV/dist
export GSLICER_DIST=$GWE_DIST/GSlicer3

export GWE_BUNDLE=`ls -t $GWE_DIST/gwe-*-client.zip | head -1`

#=======================
# PREPARE OUTPUT FOLDER
#=======================
rm -fdr $GSLICER_DIST
mkdir $GSLICER_DIST

echo Unzipping $GWE_BUNDLE to $GSLICER_DIST ... 
unzip -q $GWE_BUNDLE -d $GSLICER_DIST 

#====================================
# EXTRACT BUNDLES INTO OUTPUT FOLDER
#====================================
for SLICER_BUNDLE in `ls $SLICER_DIST/Slicer3-*.tar.gz`;
do
	echo Untarring $SLICER_BUNDLE to $SLICER_DIST ... 
    tar -xzf $SLICER_BUNDLE -C $GSLICER_DIST
done

#rm $SLICER_DIST/Slicer3-*.tar.gz
#echo Copying $SLICER_DIST to $GSLICER_DIST ... 
#cp -r $SLICER_DIST/* $GSLICER_DIST

cd $GSLICER_DIST/
export GWE_DIR=`ls -d gwe-*/ | head -1`

for SLICER_DIR in `ls -d Slicer3-*/`;
do
	export SLICER_HOME=$GSLICER_DIST/$SLICER_DIR

	echo Copying $GWE_COPY to $SLICER_HOME ...
    cp -r ./gwe-* $SLICER_HOME
    
	export GWE_HOME=`ls -d $SLICER_HOME/gwe-*/ | head -1`

	echo Generating CLMPs for $SLICER_DIR ...
    $GWE_HOME/bin/gwe-util-slicer-CLMPs-generator.sh

	export GWE_VERSION=`ls -d gwe-* | grep -o "[-][^/]*"`
	export GSLICER_NAME=G`echo ${SLICER_DIR} | grep -o  "[^/]*"`${GWE_VERSION}
	
	mv ${SLICER_DIR} ${GSLICER_NAME}

	echo Rebundling ${GSLICER_NAME}.tar.gz ... 
    tar -czf ${GSLICER_NAME}.tar.gz ${GSLICER_NAME} 
    rm -fdr ${GSLICER_NAME}
done

echo Deleting GWE unbundled distribution $GWE_DIR ...
rm -fdr $GWE_DIR
