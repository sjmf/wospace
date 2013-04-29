RUNNING THE GAME
================

Launch the game using the command:

	java -Djava.library.path=native/linux/ -jar wospace.jar

from the local directory. Replace "linux" with your host OS.

If you are running Windows, you can create a new shortcut and paste in the command.
Ensure that you use a fully qualified path to the executable and libraries if you
choose this option- something like:

	java -Djava.library.path='C:\WOSGame\native\windows\' -jar 'C:\WOSGame\wospace.jar'

Dependencies should be included.

To use audio analysis, set the VAMP_PATH variable to the correct plugin path for your platform.
Plugins are included for x86/64 Windows and Linux, and Universal Mac OSX (untested). EG:

64-bit Windows:
	set VAMP_PATH=vamp/win64
32-bit Linux:
	export VAMP_PATH=vamp/i686-linux

Running the included .bat or .sh should do this for you.


CONTROLS
========

ESC	- Close the game
F11	- Fullscreen
WASD	- Movement
Mouse L - Fire
Mouse R - Bomb


