# Spring Reactive JWT Sample

This sample project combines the latest Spring WebFlux, Spring Security to implement JWT token based authentication in Spring Reactive stack. 

There is [spring-webmvc-jwt-sample](https://github.com/hantsy/spring-webmvc-jwt-sample) which shows how to implement in Spring Servlet stack.


## Build 

Clone the source codes into your local system.

```
git clone https://github.com/hantsy/spring-reactive-jwt-sample
```

Make sure you have installed the following software:

* Apache Maven 3.6
* JDK 11
* Docker for Desktop(for Windows users and MacOS users)

There is a *docker-compose.yml* file in the project root folder. 

Run the following command to start up a MongoDb service .

```
docker-compose up mongodb
```

> NOTE: You can install a local MongoDb instead of using Docker.

Then run the application by Spring boot maven plugin directly.

```
mvn spring-boot:run
```

## Contribute

File an issue on Github issue if you have any idea.