# restfulresponsehandler.de
A small library for handling response objects from restful services.
You can specify what should happen on which http status code e.g. on a successfull status code an object of a specified class could be deserialized. If the status code represents a failure then you can specify a domain exception which will be thrown.

The library is based on my experencien while mediating between different restful webservices to achieve a consistent user experience.

## Roadmap
