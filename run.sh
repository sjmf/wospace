#!/bin/sh

ARCH=`arch`

export VAMP_PATH=./vamp/${ARCH}-linux
java -Djava.library.path=native/linux/ -jar wospace.jar
