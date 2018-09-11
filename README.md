# BBE - Bugfix By Example

A team project for the Software Verification course.

The task is to fix bugs in a given code snippet by giving a syntaxically simalar working code example.

Developed by:
- Ivan Ristović
- Milana Kovačević
- Strahinja Stanojević

Using [GumTree API](https://github.com/GumTreeDiff/gumtree)


## Prerequisites

- Java 8 or higher runtime


## Usage

The paths of the files to compare are provided via command line arguments. We provide some basic examples [here](tests) which you can use.

```java bbe /path/to/proper/snippet /path/to/bugged/snippet```


## Importing the project to Eclipse

Simply use "import an existing project" option in the Eclipse and select the root folder of this (cloned) repository. Tested on Eclipse Photon.

The **bbe** package contains the source code of the program and helper classes.

The **experimental** package contains the experimental code which will be removed at some point.

Note: The project requires libraries in the [Dependencies](deps) folder to be added to the JRE runtime library. Eclipse should do it automatically but note this if you load the code in other way or in case Eclipse doesn't add the dependencies automatically.
