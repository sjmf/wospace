RUNNING THE GAME
================

Launch the game using the command:

	java -Djava.library.path=native/linux/ -jar inv.jar

from the local directory. Replace "linux" with your host OS.

If you are running Windows, you can create a new shortcut and paste in the command.
Ensure that you use a fully qualified path to the executable and libraries if you
choose this option- something like:

	java -Djava.library.path='C:\Invaders\native\windows\' -jar 'C:\Invaders\inv.jar'

You may need to install and build dependencies for audio-response to work. The packages on debian-based systems can be installed with:

	sudo apt-get install libvamp-hostsdk3 vamp-plugin-sdk

CONTROLS
========

ESC	- Close the game
F11	- Fullscreen
WASD	- Movement
Arrows	- Movement
SPACE	- FIRE


Included CreativeCommons Licenced Music
http://freemusicarchive.org/music/Rushjet1/BACKUP11_-_Platine_festival_Compilation/06_Return_to_Control


