@echo off


REM Detect Windows architecture via hack:

SET ProgFiles86Root=%ProgramFiles(x86)%
IF NOT "%ProgFiles86Root%"=="" GOTO amd64

set VAMP_PATH=vamp\win32
GOTO run

:amd64
set VAMP_PATH=vamp\win64



:run
start javaw -Djava.library.path=native\windows\ -jar wospace.jar
