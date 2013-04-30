@setlocal enableextensions enabledelayedexpansion
@echo off


rem Detect Windows architecture via hack:
set jversion=java -version

set ProgFiles86Root=%ProgramFiles(x86)%
IF NOT "%ProgFiles86Root%"=="" GOTO amd64

set VAMP_PATH=native\vamp\win32
GOTO run

:amd64
set VAMP_PATH=native\vamp\win64



:run
start javaw -Djava.library.path=native\windows\ -jar wospace.jar
