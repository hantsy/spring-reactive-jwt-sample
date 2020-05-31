# Protect REST APIs with Spring Security Reactive and JWT



I have  published a post [Protect REST APIs with Spring Security and JWT](https://medium.com/@hantsy/protect-rest-apis-with-spring-security-and-jwt-5fbc90305cc5) which demonstrated how to use Spring Security and JWT token based authentication in a  Spring WebMvc/Servlet environment, Spring WebFlux as a new member introduced since Spring 5.0, brought a programming model for developers to building  RESTful  APIs.

In this post, we will reimplement the same features, but use the  new Spring WebFlux aka Reactive stack.

To start a new project,  go to http://start.spring.io to generate the project skeleton.

![Spring Initializer](./start.png)



And select the following requirements to generate the project.

* Project: Maven

* Language: Java

* Spring Boot: 2.3.0

* Project Metadata/Java: 11

* Dependencies:

  * Reactive Web
  * Lombok
  * Spring Data Reactive MongoDB

  

Click *Generate* button or use shortcut *Ctrl+Enter* to generate the project into an archive for download, download the zip file and extract into the disc.

To simplify the development work, I will skip the steps of creating REST APIs in this post, and reuse the codebase we have created in [Build a reactive application with Spring Boot 2.0 and Angular](https://medium.com/@hantsy/build-a-reactive-application-with-spring-boot-2-0-and-angular-de0ee5837fed), and then focus on how to replace the authentication part with the brand new JWT token based authentication.

Import the project into your favorite IDE, eg, IntelliJ IDEA. Open the *pom.xml* in the project root folder. First of all, add the `jjwt` dependencies.

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.1</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.1</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId> <!-- or jjwt-gson if Gson is preferred -->
    <version>0.11.1</version>
    <scope>runtime</scope>
</dependency>
```

In the package `com.example.demo.security.jwt` , create a new `JwtProvider` class.

```java
@Component
@Slf4j
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "roles";
    @Autowired
    JwtProperties jwtProperties;

    private SecretKey secretKey;

    @PostConstruct
    protected void init() {
        var secret = Base64.getEncoder().encodeToString(jwtProperties.getSecretKey().getBytes());
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Authentication authentication) {

        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(AUTHORITIES_KEY, authorities.stream().map(GrantedAuthority::getAuthority).collect(joining(",")));

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.getValidityInMs());

        return Jwts.builder()//
                .setClaims(claims)//
                .setIssuedAt(now)//
                .setExpiration(validity)//
                .signWith(secretKey, SignatureAlgorithm.HS256)//
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(claims.get(AUTHORITIES_KEY).toString());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(token);

            if (claims.getBody().getExpiration().before(new Date())) {
                return false;
            }

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace.", e);
        }
        return false;
    }

}
```

And create a `JwtProperties` class, annotated with `@ConfigurationProperties`.

```java
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String secretKey = "flzxsqcysyhljt";

    //validity in milliseconds
    private long validityInMs = 3600000; // 1h
}
```

On the `Application` class, add a `@ConfigurationPropertiesScan` annotation, similar with `@ComponentScan`, it will scan `@ConfigurationProperties` components in the application's start stage.

Next, create a `WebFilter` to handle the authentication if there is a JWT token existed in the http request headers.

```java
@RequiredArgsConstructor
public class JwtTokenAuthenticationFilter implements WebFilter {

    public static final String HEADER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = resolveToken(exchange.getRequest());
        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
            Authentication authentication = this.tokenProvider.getAuthentication(token);
            return chain.filter(exchange).subscriberContext(ReactiveSecurityContextHolder.withAuthentication(authentication));
        }
        return chain.filter(exchange);
    }

    private String resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(HEADER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

Now, register this filter in the `SecurityWebFilterChain`,  create a   `SecurityWebFilterChain` `@Bean`.

```java
    @Bean
    SecurityWebFilterChain springWebFilterChain(
            ServerHttpSecurity http,
            JwtTokenProvider tokenProvider) {

        return http
                .csrf(it -> it.disable())
                .httpBasic(it -> it.disable())
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(it ->
                        it.pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                                .pathMatchers("/posts/**").authenticated()
                                .pathMatchers("/me").authenticated()
                                .pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                                .anyExchange().permitAll()
                )
                .addFilterAt(new JwtTokenAuthenticationFilter(tokenProvider), SecurityWebFiltersOrder.HTTP_BASIC)
                .build();

    }
```

The `.securityContextRepository()` will prevent it to create a `WebSession`, it is similar with [STATELESS strategy](https://github.com/hantsy/spring-webmvc-jwt-sample/blob/master/src/main/java/com/example/demo/config/SecurityConfig.java#L33) in Servlet stack. 

Create a RESTful API to authenticate a user.

```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider tokenProvider;

    private final ReactiveAuthenticationManager authenticationManager;

    @PostMapping("/token")
    public Mono<ResponseEntity> login(@Valid @RequestBody Mono<AuthenticationRequest> authRequest) {
        return authRequest
                .flatMap(login -> authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()))
                        .map(auth -> tokenProvider.createToken(auth))
                )
                .map(jwt -> {
                            HttpHeaders httpHeaders = new HttpHeaders();
                            httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
                            var tokenBody = Map.of("id_token", jwt);
                            return new ResponseEntity<>(tokenBody, httpHeaders, HttpStatus.OK);
                        }
                );
    }

}
```

When a user is authenticated successfully, put the token in the response headers and body.

Declare a `ReactiveAuthenticationManager` @Bean.

```java
@Bean
public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    authenticationManager.setPasswordEncoder(passwordEncoder);
    return authenticationManager;
}
```

And `ReactiveUserDetailsService` @Bean.

```java
@Bean
public ReactiveUserDetailsService userDetailsService(UserRepository users) {
    return (username) -> users.findByUsername(username)
        .map(u -> User.withUsername(u.getUsername())
             .password(u.getPassword())
             .authorities(u.getRoles().toArray(new String[0]))
             .accountExpired(!u.isActive())
             .credentialsExpired(!u.isActive())
             .disabled(!u.isActive())
             .accountLocked(!u.isActive())
             .build()
            );
}
```

Let's start up a MongoDB server, you can simplify start it via [`dockerc-compose.yml`](https://github.com/hantsy/spring-reactive-jwt-sample/blob/master/docker-compose.yml) file.

```bash
docker-compose up mongodb   
```

Execute  the following  command to start up the application

```bash
mvn clean spring-boot:run
```
Or run the `Application` in your IDEs directly.

Let's start to try the  APIs via `curl`.

```bash
 curl -X POST http://localhost:8080/auth/token -H "Content-Type:application/json" -d "{\"username\":\"user\", \"password\":\"password\"}"
{"id_token":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE1OTA5MTE0ODIsImV4cCI6MTU5MDkxNTA4Mn0.lqsWeWEx9pkgg1xGfghpnKV7PkrgEb7R0FOeWrDQuF0"}
```

Fetch the current user info. 

```bash
curl http://localhost:8080/me -H "Authorization:Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwicm9sZXMiOiJST0xFX1VTRVIiLCJpYXQiOjE1OTA5MTE0ODIsImV4cCI6MTU5MDkxNTA4Mn0.lqsWeWEx9pkgg1xGfghpnKV7PkrgEb7R0FOeWrDQuF0"
{"name":"user","roles":["ROLE_USER"]}
```

Grab the [source codes](https://github.com/hantsy/spring-reactive-jwt-sample) from my Github account, and explore it freely.
