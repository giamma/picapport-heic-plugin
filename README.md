# picapport-heic-plugin
Experimental HEIC/HEIF plug-in for Picapport

This is an experimental [Piccapport](https://www.picapport.de/en/index.php) plug-in for supporting HEIC/HEIF images.

As Picapport only supports JPEG files, plug-ins like this are used to convert images in other file formats to JPEG. To do so, this plug-in relies on the linux package ImageMagick, which includes a command line utility called *convert*. To invoke such command line utility this plug-in relies on the library im4j which provides a simple Java API to invoke the *convert* utility from the command line.

In order to use this plug-in in Picapport you have to make sure that ImageMagick is installed and the executable is in the system PATH. Then follow Picapport installation instructions and drop the HEICImagePlugin.zip into the picapport plugin's folder.

# docker
For those running Piccapport via whatever4711 docker [image](https://hub.docker.com/r/whatever4711/picapport) (sources [here](https://github.com/whatever4711/picapport)), you can add imagemagick by building a derived image using a *Dockerfile* containing the following:

    FROM <IMAGE_TAG>
    RUN apk update && apk add imagemagick

# credits
This project uses:
- [ImageMagick](https://imagemagick.org/index.php) for converting HEIC/HEIF pictures to JPEG
- [im4j](https://im014java.sourceforge.net/) to invoke ImageMagick. This library did not see a release for many years, but it still works perfectly.

# license
This project is licensed under the Apache Public License 2.0.
