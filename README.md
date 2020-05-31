# Spring Reactive JWT Sample

As an alternative of  [spring-webmvc-jwt-sample](https://github.com/hantsy/spring-webmvc-jwt-sample) which is implemented in Spring Servlet stack,  this sample project combines the latest Spring WebFlux, Spring Security to implement JWT token based authentication in Spring Reactive stack. 

[>> Protect REST APIs with Spring Security and JWT, the Reactive Way](./docs/GUIDE.md)

## Build 

Make sure you have installed the following software:

* Apache Maven 3.6
* JDK 11
* Docker for Desktop(for Windows users and MacOS users)

Clone the source codes into your local system.

```
git clone https://github.com/hantsy/spring-reactive-jwt-sample
```

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