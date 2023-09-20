# Spring Reactive JWT Sample

[![pre-commit](https://img.shields.io/badge/pre--commit-enabled-brightgreen?logo=pre-commit&logoColor=white)](https://github.com/pre-commit/pre-commit)
[![DeepSource](https://deepsource.io/gh/hantsy/spring-reactive-jwt-sample.svg/?label=active+issues&show_trend=true)](https://deepsource.io/gh/hantsy/spring-reactive-jwt-sample/?ref=repository-badge)

![Compile and build](https://github.com/hantsy/spring-reactive-jwt-sample/workflows/build/badge.svg)
![Dockerize](https://github.com/hantsy/spring-reactive-jwt-sample/workflows/dockerize/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=hantsy_spring-reactive-jwt-sample&metric=alert_status)](https://sonarcloud.io/dashboard?id=hantsy_spring-reactive-jwt-sample)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=hantsy_spring-reactive-jwt-sample&metric=coverage)](https://sonarcloud.io/dashboard?id=hantsy_spring-reactive-jwt-sample)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=hantsy_spring-reactive-jwt-sample&metric=code_smells)](https://sonarcloud.io/dashboard?id=hantsy_spring-reactive-jwt-sample)

[![Build Status](https://travis-ci.com/hantsy/spring-reactive-jwt-sample.svg?branch=master)](https://travis-ci.com/hantsy/spring-reactive-jwt-sample)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/14860630dabd4e6eb98488723868a728)](https://www.codacy.com/manual/hantsy/spring-reactive-jwt-sample?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=hantsy/spring-reactive-jwt-sample&amp;utm_campaign=Badge_Grade)[![Codacy Badge](https://app.codacy.com/project/badge/Coverage/14860630dabd4e6eb98488723868a728)](https://www.codacy.com/manual/hantsy/spring-reactive-jwt-sample?utm_source=github.com&utm_medium=referral&utm_content=hantsy/spring-reactive-jwt-sample&utm_campaign=Badge_Coverage)

[![Build Status](https://hantsy.semaphoreci.com/badges/spring-reactive-jwt-sample/branches/master.svg)](https://hantsy.semaphoreci.com/projects/spring-reactive-jwt-sample)
[![Coverage Status](https://coveralls.io/repos/github/hantsy/spring-reactive-jwt-sample/badge.svg?branch=master)](https://coveralls.io/github/hantsy/spring-reactive-jwt-sample?branch=master)

[![CircleCI](https://circleci.com/gh/hantsy/spring-reactive-jwt-sample.svg?style=svg)](https://circleci.com/gh/hantsy/spring-reactive-jwt-sample)
[![codecov](https://codecov.io/gh/hantsy/spring-reactive-jwt-sample/branch/master/graph/badge.svg)](https://codecov.io/gh/hantsy/spring-reactive-jwt-sample)

[![Build Status](https://cloud.drone.io/api/badges/hantsy/spring-reactive-jwt-sample/status.svg)](https://cloud.drone.io/hantsy/spring-reactive-jwt-sample)

[![Build status](https://ci.appveyor.com/api/projects/status/n217cgnf22rkpnwy?svg=true)](https://ci.appveyor.com/project/hantsy/spring-reactive-jwt-sample-7fhef)

[![Codefresh build status]( https://g.codefresh.io/api/badges/pipeline/hantsy/spring-reactive-jwt-sample%2Fbuild?type=cf-2&key=eyJhbGciOiJIUzI1NiJ9.NWI4ZGZjMjM0MDc1NmYwMDAxNTViZGQw.xqU1hpod9YdRPhYJdXP462qUlgfdimLXU9CqZCC2MYw)]( https://g.codefresh.io/pipelines/edit/new/builds?id=5fcba0d054e90922d62934dd&pipeline=build&projects=spring-reactive-jwt-sample&projectId=5fcb9e7484fbdc2cb6bf1a5b)

[![Build Status](https://hantsy.visualstudio.com/spring-reactive-jwt-sample/_apis/build/status/hantsy.spring-reactive-jwt-sample?branchName=master)](https://hantsy.visualstudio.com/spring-reactive-jwt-sample/_build/latest?definitionId=1&branchName=master)

[![Dependency Vulnerabilities](https://img.shields.io/endpoint?url=https%3A%2F%2Fapi-hooks.soos.io%2Fapi%2Fshieldsio-badges%3FbadgeType%3DDependencyVulnerabilities%26pid%3Depbyoz0sm%26branchName%3Dmaster)](https://app.soos.io)

As an alternative of  [spring-webmvc-jwt-sample](https://github.com/hantsy/spring-webmvc-jwt-sample) which is implemented in Spring Servlet stack,  this sample project combines the latest Spring WebFlux, Spring Security to implement JWT token based authentication in Spring Reactive stack.

## Guide

[Secures RESTful APIs with Spring Security WebFlux and JWT Token Authentication](./docs/GUIDE.md)

## Build

Make sure you have installed the following software:

* Apache Maven 3.6
* JDK 17
* Docker for Desktop(for Windows users and MacOS users)
* Python (Optional for contributors)

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

If you want to send a PR directly, install python and `pre-commit` to check your git comments style and code style.

```python
pip install pre-commit
pre-commit install --hook-type commit-msg --hook-type pre-push
```
