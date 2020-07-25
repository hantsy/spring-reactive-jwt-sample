# Spring Reactive JWT Sample

[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white)](https://github.com/pre-commit/pre-commit)

![Compile and build](https://github.com/hantsy/spring-reactive-jwt-sample/workflows/build/badge.svg)
![Dockerize](https://github.com/hantsy/spring-reactive-jwt-sample/workflows/dockerize/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hantsy_spring-reactive-jwt-sample&metric=alert_status)](https://sonarcloud.io/dashboard?id=hantsy_spring-reactive-jwt-sample)

[![Build Status](https://travis-ci.com/hantsy/spring-reactive-jwt-sample.svg?branch=master)](https://travis-ci.com/hantsy/spring-reactive-jwt-sample)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/14860630dabd4e6eb98488723868a728)](https://www.codacy.com/manual/hantsy/spring-reactive-jwt-sample?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hantsy/spring-reactive-jwt-sample&amp;utm_campaign=Badge_Grade)[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/14860630dabd4e6eb98488723868a728)](https://www.codacy.com/manual/hantsy/spring-reactive-jwt-sample?utm_source=github.com&utm_medium=referral&utm_content=hantsy/spring-reactive-jwt-sample&utm_campaign=Badge_Coverage)

[![Semaphore Status](https://hantsy.semaphoreci.com/badges/spring-reactive-jwt-sample.svg)](https://hantsy.semaphoreci.com/badges/spring-reactive-jwt-sample.svg)
[![Coverage Status](https://coveralls.io/repos/github/hantsy/spring-reactive-jwt-sample/badge.svg?branch=master)](https://coveralls.io/github/hantsy/spring-reactive-jwt-sample?branch=master)

[![CircleCI](https://circleci.com/gh/hantsy/spring-reactive-jwt-sample.svg?style=svg)](https://circleci.com/gh/hantsy/spring-reactive-jwt-sample)
[![codecov](https://codecov.io/gh/hantsy/spring-reactive-jwt-sample/branch/master/graph/badge.svg)](https://codecov.io/gh/hantsy/spring-reactive-jwt-sample)

[![Build Status](https://cloud.drone.io/api/badges/hantsy/spring-reactive-jwt-sample/status.svg)](https://cloud.drone.io/hantsy/spring-reactive-jwt-sample)

[![Run Status](https://api.shippable.com/projects/5f1a44e33a77910007dd8282/badge?branch=master)](https://app.shippable.com/github/hantsy/spring-reactive-jwt-sample/dashboard)
[![Coverage Badge](https://api.shippable.com/projects/5f1a44e33a77910007dd8282/coverageBadge?branch=master)](https://app.shippable.com/github/hantsy/spring-reactive-jwt-sample/dashboard)

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
