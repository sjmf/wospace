#!/bin/sh

export VAMP_PATH=native/vamp/amd64-linux;native/vamp/i686-linux

java -Djava.library.path=native/linux/ -jar wospace.jar
