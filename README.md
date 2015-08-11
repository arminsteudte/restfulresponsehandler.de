# restfulresponsehandler.de
A small library for handling response objects from restful services.

You can specify what should happen on which http status code e.g. on a successful status code an object of a specified class could be deserialized. If the status code represents a failure then you can specify a domain exception which will be thrown.

The library is based on my experience while mediating between different restful web services to achieve a consistent user experience.

## Roadmap
- First implementation only for Jersey Client response objects
- Further framework support e.g. Spring Resttemplate or REASTEasy
- Create a version depending on JDK 1.6

## Requirements
Requires JDK 1.8 or higher.

## Build status
![travis-ci build status](https://travis-ci.org/arminsteudte/restfulresponsehandler.de.svg?branch=master "Travis-CI 
build status")
