#!/bin/sh

export VAMP_PATH=native/vamp/osx-universal

java -Djava.library.path=native/macosx/ -jar wospace.jar
