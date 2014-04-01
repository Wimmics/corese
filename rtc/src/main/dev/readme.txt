Firefox Development References:
https://developer.mozilla.org/en/docs/Building_an_Extension
https://developer.mozilla.org/en/docs/XUL

*****

In order to test the extention automatically and avoid to restart the browser every time some changes are made, you need to setup environment as follows:

1. Install firefox extension Auto-Installer
https://addons.mozilla.org/en-US/firefox/addon/autoinstaller/

2. Install wget(http://en.wikipedia.org/wiki/Wget), which is used to send the extension to Auto-Installer, the software can vary depending on different OS.

Take Mac OS X for example(http://osxdaily.com/2012/05/22/install-wget-mac-os-x/), 

----------------------------
[1] use curl to download the latest wget source:
    $ curl -O http://ftp.gnu.org/gnu/wget/wget-1.15.tar.gz

[2] use tar to uncompress the files you just downloaded:
    $ tar -xzf wget-1.13.4.tar.gz

[3] Use cd to change to the directory:
    $ cd wget-1.13.4

[4] Configure with the appropriate –with-ssl flag to prevent a “GNUTLS not available” error:
    $ ./configure --with-ssl=openssl

[5] Build the source:
    $ make

[6] Install wget, it ends up in /usr/local/bin/:
    $ sudo make install

[7] Confirm everything worked by running wget:
    $ wget --help

[8] Clean up by removing wget source files when finished:
    $ cd .. && rm -rf wget*
-------------------------------------

3. Use command 'wget --post-file={extension.xpi} http://localhost:8888/' to send the extension to Auto-Installer. {extension.xpi} is the path of the extension, in this project it will be {your project folder}/target/xip/{project name}-{version}.xpi

4. This plugin is packaged using maven-plugin-assembly, the generated xpi file will be saved in the folder target/xip/{project name}-{version}.xpi

********
contact: fuqi.song@inria.fr
