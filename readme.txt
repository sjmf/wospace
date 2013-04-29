RUNNING THE GAME
================

Launch the game by running the provided .sh file for Linux, or .bat file for Windows. These
will set the state up for the application so you don't have to.

If you really want to know what's going on, or the .bat doesn't work, here's how to do it manually:


RUNNING MANUALLY
===============

To use audio analysis, set the VAMP_PATH variable to the correct plugin path for your platform.
Plugins are included for x86/64 Windows and Linux, and Universal Mac OSX (untested). EG:

64-bit Windows:
	set VAMP_PATH=vamp/win64
32-bit Linux:
	export VAMP_PATH=vamp/i686-linux

Running the included .bat or .sh should do this for you.

	java -Djava.library.path=native/linux/ -jar wospace.jar

from the local directory. Replace "linux" with your host OS.

If you are running Windows, you can create a new shortcut and paste in the command.
Ensure that you use a fully qualified path to the executable and libraries if you
choose this option- something like:

	java -Djava.library.path='C:\WOSGame\native\windows\' -jar 'C:\WOSGame\wospace.jar'


CONTROLS
========

ESC	- Close the game
F11	- Fullscreen
WASD	- Movement
Mouse L - Fire
Mouse R - Bomb


