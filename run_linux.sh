#!/bin/sh

ARCH=`uname -m`

if [[ $ARCH == *64* ]]
then
	export VAMP_PATH=./native/vamp/amd64-linux
else
	export VAMP_PATH=./native/vamp/i686-linux
fi

java -Djava.library.path=native/linux/ -jar wospace.jar
