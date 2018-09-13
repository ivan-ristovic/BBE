# BBE - Bugfix By Example

A team project for the Software Verification course.

The task is to fix bugs in a given code snippet by giving a syntaxically simalar working code example. For now we provide only Java code analyzer due to usage of the JDT Core DOM API.

In-depth README can be found [here](SystemDescription.pdf).

Developed by:
- Ivan Ristović
- Milana Kovačević
- Strahinja Stanojević

Using [GumTree API](https://github.com/GumTreeDiff/gumtree) and [JDT Core DOM API](https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FASTVisitor.html).


## Prerequisites

- JRE 8 or higher


## Usage

The paths of the files to compare are provided via command line arguments. We provide some basic examples which you can use - you can find them [here](tests).

```java -jar bbe.jar /path/to/proper/snippet /path/to/bugged/snippet```


## Importing the project to Eclipse

Simply use "import an existing project" option in the Eclipse and select the root folder of this (cloned) repository. Tested on Eclipse Photon.

The **bbe** package contains the source code of the program and helper classes.

The **experimental** package contains the experimental code which will be removed at some point.

Note: The project requires libraries in the [Dependencies](deps) folder to be added to the JRE runtime library. Eclipse should do it automatically but note this if you load the code in other way or in case Eclipse doesn't add the dependencies automatically.
