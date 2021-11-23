**BY DOWNLOADING OR USING THE LX SOFTWARE OR ANY PART THEREOF, YOU AGREE TO THE TERMS AND CONDITIONS OF THE [LX STUDIO SOFTWARE LICENSE AND DISTRIBUTION AGREEMENT](http://lx.studio/license).**

Please note that LX is not open-source software. The license grants permission to use this software freely in non-commercial applications. Commercial use is subject to a total annual revenue limit of $25K on any and all projects associated with the software. If this licensing is obstructive to your needs or you are unclear as to whether your desired use case is compliant, contact me to discuss proprietary licensing: mark@heronarts.com

---

### P3LX Overview ###

P3LX is a Processing 3 wrapper library for the [LX](https://github.com/heronarts/LX) lighting engine and the basis of the [LX Studio](http://lx.studio/) application. It allows you to simply embed LX inside a Processing sketch with a rich UI library that makes it easy and painless to render 3D simulations alongside versatile 2D controls.

Directly working with the P3LX library is only recommended for advanced users of LX. The easiest way to start an LX project is to clone or fork the [LXStudio](https://github.com/heronarts/LXStudio) repository and use that as the basis for your project.

### Development Environment ###

The recommended IDE for LX is Eclipse, using `mvn` for command-line build. Create a folder to work in and clone both the LX and P3LX repositories side-by-side.
```
$ mkdir workspace
$ cd workspace
$ git clone https://github.com/heronarts/LX.git
$ git clone https://github.com/heronarts/P3LX.git
```

To open the project in Eclipse:
```
File | Import...
General > Existing Projects Into Workspace
Select root directory...
```

Go through this process for both the LX and P3LX projects, selecting `workspace/LX` and `workspace/P3LX` as the root directories.

#### Maven Build Process ####

First: Install Maven for your platform. Google is your friend.

P3LX depends upon the Processing core library JAR file, which is not available in Maven central. First, you will need to install this to your local Maven repository using the `mvn validate` command. 
```
$ cd P3LX
# mvn validate
```

Once you have done this, you can build and install both the LX and P3LX packages
```
$ cd ../LX
$ mvn install
$ cd ../P3LX
$ mvn install
```
The above commands result in the following artifacts:
in `P3LX/target`:
1. fat jar with dependencies
1. thin jar for distribution via maven repository publishing
1. source jar for distribution via maven repository publishing
1. javadoc jar for distribution via maven repository publishing
1. javadoc html files for publishing to web: `apidocs`

The Maven build is IDE-agnostic, so any IDE that can import Maven projects (Eclipse, IntelliJ) should have no problem importing and building this repo.

#### Deployment ####

Deployment requires access to the central Sonatype repository and appropriate GPG keys.

```
$ cd ../P3LX
$ mvn deploy -Pdeploy
```

### Contact and Collaboration ###

Building a big cool project? I'm probably interested in hearing about it! Want to solicit some help, request new framework features, or just ask a random question? Drop me a line: mark@heronarts.com

---

HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR PURPOSE, WITH RESPECT TO THE SOFTWARE.

