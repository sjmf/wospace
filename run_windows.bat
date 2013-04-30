@echo off

echo Loading...


set VAMP_PATH=native\vamp\win32;native\vamp\win64

start javaw -Djava.library.path=native\windows\ -jar wospace.jar
