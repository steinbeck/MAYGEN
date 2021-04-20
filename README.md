# MAYGEN

Copyright 2021 Mehmet Aziz Yirik

## Introduction

An open source structure generator based on the orderly graph generation method.

## Method

The project is an open source re-implementation of Dr. Grund's work [1]. The theoretical basis and the outlines of the functions can be found in [1,2].  

## Download jar File

The main class of the current jar file is MAYGEN class. The file can be downloaded from [here](https://github.com/MehmetAzizYirik/AlgorithmicGroupTheory/blob/master/MORGEN.jar)

## Download Source Code

It is assumed that users have git on their system and have initialised their local directory. For more information [set-up-git](https://help.github.com/articles/set-up-git/ )

To download MAYGEN source code:

```
$ git clone https://github.com/MehmetAzizYirik/MAYGEN.git
```
## Compiling

To compile MAYGEN, Apache Maven and Java 1.8 (or later) are required.
```
MAYGEN/$ mvn package
```
This command will create jar file named as "MAYGEN-jar-with-dependencies" under target folder.

## Usage

MAYGEN.jar can be run from command line with the specified arguments. An example command is given below.

The definitions of the arguments are given below:

```
usage: java -jar MAYGEN.jar -f <arg> [-v] -d <arg>

Generates molecular structures for a given molecular formula. The input is 
a molecular formula string. For example 'C2OH4'. Besides molecular formula, the
directory is needed to be specified for the output file.

 -f,--molecularFormula <arg>   Molecular formula as a string (required)
 
 -v,--verbose                  Print messages about structure generation
 
 -d,--filedir <arg>            Creates and store the output txt file 
                               in the directory 

Please report issues at https://github.com/MehmetAzizYirik/MAYGEN
```

```
java -jar MAYGEN.jar -f C2OH4 -v -d C:\Users\UserName\Desktop\
```

## License
This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/MehmetAzizYirik/MAYGEN/blob/master/LICENSE) file for details

## Authors

 - Mehmet Aziz Yirik - [MehmetAzizYirik](https://github.com/MehmetAzizYirik)
 
## Acknowledgements
![YourKit](https://camo.githubusercontent.com/97fa03cac759a772255b93c64ab1c9f76a103681/68747470733a2f2f7777772e796f75726b69742e636f6d2f696d616765732f796b6c6f676f2e706e67)

The developer uses YourKit to profile and optimise code.

YourKit supports open source projects with its full-featured Java Profiler. YourKit, LLC is the creator of YourKit Java Profiler and YourKit .NET Profiler, innovative and intelligent tools for profiling Java and .NET applications.

![cdk](https://github.com/MehmetAzizYirik/HMD/blob/master/cdk.png)

This project relies on the Chemistry Development Project (CDK), hosted under [CDK GitHub](http://cdk.github.io/). Please refer to these pages for updated information and the latest version of the CDK. CDK's API documentation is available though our [Github site](http://cdk.github.io/cdk/).

## References

1- Grund, R. and Müller, R., 1995. Konstruktion molekularer Graphen mit gegebenen Hybridisierungen und überlappungsfreien Fragmenten. Lehrstuhl II für Mathematik.

2- Kerber, A., Laue, R., Meringer, M., Rücker, C. and Schymanski, E., 2013. Mathematical chemistry and chemoinformatics: structure generation, elucidation and quantitative structure-property relationships. Walter de Gruyter.

