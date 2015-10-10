#! /bin/bash

source ./report.conf

if [ ! -z "$packages" ]
then
	package=$(echo "--package $packages")
fi

if [ ! -z "$titles" ]
then
	title=$(echo "--title $titles")
fi


mvn clean compile exec:java -Dexec.mainClass="edu.cmu.jacoco.cli.JacocoComparisonTool" -Dexec.args="--source $source --report $reports --exec ${execfile[0]},${execfile[1]} $package $title"
