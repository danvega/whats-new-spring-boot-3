# What's new in Spring Boot 3.0

Spring Framework 6 and Spring Boot 3 have been released. This is an example project that I am using to 
demo some of my favorite new features in Spring Framework 6 & Spring Boot 3. If you want to read
more about the Spring Boot 3 you can use the following links:`

- [Spring Boot 3 goes GA](https://spring.io/blog/2022/11/24/spring-boot-3-0-goes-ga)
- [Spring Boot 3 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes)

## Baseline Upgrades

- Java 17
- Support for Jakarta EE 10 with an EE 9 baseline

### Java 17

Spring has supported Java 17 for a while now but with Spring Framework 6 and Spring Boot 3 this is the required baseline. 
If you're moving from Java 8 here are a few of the amazing benefits you will get out of the box just by moving to 
Spring Boot 3 and Java 17: 

- Performance Improvements 
- Security Updates
- New Features (JDK 9+)
  - JShell 
  - Local variable type inference (var keyword)
  - HttpClient
  - Switch Expressions
  - Text Blocks / Multiline Strings
  - Records
  - Pattern Matching
  - Sealed Classes

### Jakarta EE 9/10

A bulk of the work in this release has been around the Jakarta EE upgrade and support for GraalVM. This is an exciting 
update because it allows you to start using the Jakarta APIs which will now start to evolve instead of sitting around 
and collecting dust like they have been over the last 10 years. 

- javax.* -> jakarta.* namespace

It might be a little confusing to see both Jakarta EE 9 & 10 so let's talk about why both versions are listed. Spring Boot
3 upgraded to Tomcat 10.1 (since 10.0 is EOL already) which has a dependency on Servlet 6.0. The goal is now to provide a set of EE 10 providers in Boot 3.0 (as far as possible) while retaining an EE 9 baseline for optional downgrades (e.g. for Jetty 11 - Servlet 5.0).

What this boils down to is that we can support either Jakarta EE 9 or 10 at runtime, but only 10 in a test suite that uses
servlet for the time being. This is because the Spring-provided servlet mocks have a Servlet 6.0 baseline now. This restriction only applies to mock-based servlet and tests using `TestRestTemplate` aren't affected.

Spring Boot 3.0 has migrated from Java EE to Jakarta EE APIs for all dependencies. Wherever possible, Jakarta EE 10 compatible dependencies have been chosen, including:

- Jakarta Activation 2.1
- Jakarta JMS 3.1
- Jakarta JSON 2.1
- Jakarta JSON Bind 3.0
- Jakarta Mail 2.1
- Jakarta Persistence 3.1
- Jakarta Servlet 6.0
- Jakarta Validation 3.0
- Jakarta WebSocket 2.1

We’ve also upgraded to the latest stable releases of third-party jars wherever possible. Some notable dependency upgrades here include:

- Groovy 4.0
- Hibernate 6.1
- Hibernate Validator 8.0
- Jackson 2.14
- Jetty 11
- R2DBC 1.0
- SLF4J 2.0
- Tomcat 10
- Thymeleaf 3.1.0.M2

Check the [release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes) for a more complete list. 

## Native Images with GraalVM

Spring Boot 3.0 applications can now be converted into GraalVM native images which can provide significant memory and startup-up performance improvements. Spring Boot requires GraalVM 22.3 or later and Native Build Tools Plugin 0.9.17 
or later to build native images. If you would like to learn more about GraalVM Native Image Support you can check
out the [reference documentation](https://docs.spring.io/spring-boot/docs/3.0.0/reference/html/native-image.html#native-image).

With the native profile active, you can invoke the native:compile goal to trigger native-image compilation:

`mvn -Pnative native:compile`

The result will be a native executable in the `target/` directory. 

Spring Boot includes buildpack support for native images directly for both Maven and Gradle. This means you can just type a single command and quickly get a sensible image into your locally running Docker daemon. The resulting image will not contain a JVM, instead the native image is compiled statically. This leads to smaller images.

To build the image, you can run the `spring-boot:build-image` goal with the native profile active:

`mvn -Pnative spring-boot:build-image`

If you're using buildpacks on ARM64 (macOS) you will want to check out the article below by DaShaun Carter.

[A new builder for Spring Boot 3 RC1 on ARM64](https://dashaun.com/posts/paketo-aarch64-builder-spring-boot-3-rc1/)

## Observability

Observability is the ability to observe the internal state of a running system from the outside. It consists of the three pillars logging, metrics and traces.

Observability is not only: 

- Logging
- Metrics
- Distributed Tracing
- Data collection and visualization tools

What is observability? In our understanding, it is "how well you can understand the internals of your system by examining its outputs". We believe that the interconnection between metrics, logging, and distributed tracing gives you the ability to reason about the state of your system in order to debug exceptions and latency in your applications.

Spring Boot 3 adds built-in support for distributed tracing where previously we dependent on a library like Spring Cloud Sleuth to handle this. There is now a single observation api from Micrometer that is being used by the framework and available to you to use in your own code.

```java
@Component
public class MyCustomObservation {

    private final ObservationRegistry observationRegistry;

    public MyCustomObservation(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    public void doSomething() {
        Observation.createNotStarted("doSomething", this.observationRegistry)
                .lowCardinalityKeyValue("locale", "en-US")
                .highCardinalityKeyValue("userId", "42")
                .observe(() -> {
                    // Execute business logic here
                });
    }

}
```

💡 Low cardinality tags will be added to metrics and traces, while high cardinality tags will only be added to traces.

If you want to learn more you can read through the following documentation: 

[Observability Reference Documentation](https://docs.spring.io/spring-boot/docs/3.0.0/reference/html/actuator.html#actuator.observability)
[Observability with Spring Boot 3](https://spring.io/blog/2022/10/12/observability-with-spring-boot-3)
[Observability for JDBC](https://jdbc-observations.github.io/datasource-micrometer/docs/current/docs/html/)

## Http Interfaces 

The Spring Framework lets you define an HTTP service as a Java interface with annotated methods for HTTP exchanges. You can then generate a proxy that implements this interface and performs the exchanges. This helps to simplify HTTP remote access which often involves a facade that wraps the details of using the underlying HTTP client.

In the following example I need to call a public API. Prior to Spring Boot 3 I would use the `RestTemplate` or `WebClient` to construct
this call: 

```java
public List<Post> loadPosts() {
    ResponseEntity<List<Post>> exchange = restTemplate.exchange("https://jsonplaceholder.typicode.com/posts", HttpMethod.GET, null, new ParameterizedTypeReference<List<Post>>() {});
    return exchange.getBody();
}
```

In Spring Boot 3 you can declare an interface with the methods you would like to support. 

```java
public interface JsonPlaceholderService {

    @GetExchange("/posts")
    List<Post> loadPosts();

}
```

Next, create a proxy that will perform the declared HTTP exchanges:

```java
WebClient client = WebClient.builder().baseUrl("https://jsonplaceholder.typicode.com").build();
HttpServiceProxyFactory factory = HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build();

JsonPlaceholderService jps = factory.createClient(JsonPlaceholderService.class);
```

If you want to learn more about Http Interfaces check out the [reference documentation](https://docs.spring.io/spring-framework/docs/6.0.0-RC2/reference/html/integration.html#rest-http-interface)

## Problem Details for HTTP APIs

A common requirement for REST services is to include details in the body of error responses. The Spring Framework supports the "Problem Details for HTTP APIs" specification, [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807.html).

context propagation - show hitting the url in a browser and then from the command line

```bash
http :8080/api/posts/999

HTTP/1.1 500
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: close
Content-Type: application/json
Date: Fri, 02 Dec 2022 20:24:58 GMT
Expires: 0
Pragma: no-cache
Transfer-Encoding: chunked
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 0

{
    "error": "Internal Server Error",
    "path": "/api/posts/999",
    "status": 500,
    "timestamp": "2022-12-02T20:24:58.104+00:00"
}

```

This is ok, but I would like to see more details about the problem that is happening here.

```bash
http :8080/api/posts/999

HTTP/1.1 404 
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Type: application/problem+json
Date: Wed, 07 Dec 2022 15:54:07 GMT
Expires: 0
Keep-Alive: timeout=60
Pragma: no-cache
Transfer-Encoding: chunked
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 0

{
    "detail": "Post not found!",
    "instance": "/api/posts/999",
    "postId": null,
    "status": 404,
    "title": "Not Found",
    "type": "http://localhost:8080/problems/post-not-found"
}
```

You can do this by returning a `ProblemDetail`: 

```java 
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(PostNotFoundException.class)
    public ProblemDetail handlePostNotFoundException(PostNotFoundException e) throws URISyntaxException {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());
        problemDetail.setProperty("postId",e.getId());
        problemDetail.setType(new URI("http://localhost:8080/problems/post-not-found"));
        return problemDetail;
    }

}
```

If you would like to learn more about error responses in Spring MVC check out the [reference documentation](https://docs.spring.io/spring-framework/docs/6.0.2/reference/html/web.html#mvc-ann-rest-exceptions)

## Dependency Upgrades

If you want to see a full list of third-party upgrades you can read through the [release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes#third-party-library-upgrades). In this first look at
Spring Boot I am just going to cover a few things that stick out to me but I will try and cover more of them in the future.

### Spring Framework 6

- [Trailing Slash Matching Configuration](https://github.com/spring-projects/spring-framework/issues/28552)

### Spring Security 6.0 

- WebSecurityConfigurerAdapter deprecated
- .authorizeRequests() - deprecated 
- RequestMatchers

https://twitter.com/sergialmar/status/1592895963252854784

Confused about request matchers in
@SpringSecurity
? Should you use antMatchers, mvcMatchers, regexMatchers? 🤔
They will all be deprecated in Spring Security 5.8 and removed in Spring Security 6.0 in favor of  requestMatchers.
The new request matcher methods use MVC matches if Spring MVC is in the classpath, otherwise, they fallback to ant path matchers.
And also make sure to replace any authorizeRequests with authorizeHttpRequests!

### Spring Data 2022

- ListCrudRepository 
- PagingAndSortingRepository no longer extends CrudRepository

https://github.com/spring-projects/spring-data-commons/wiki/Spring-Data-2022.0-%28Turing%29-Release-Notes

## Upgrading to Spring Boot 3

I have been receiving a lot of questions lately around upgrading to Spring Boot 3. I will need to create a separate tutorial
on this because there is a lot to get into here. I think the biggest advice I can give is to make sure you incrementally upgrade
to to 2.7.x before moving from something like 2.4 -> 3.0. Below are some things to look out for as well as a really good
Migration Guide.

- Upgrade to 2.7.x release 
- Upgrade to Java 17
- Review Dependencies 
- Spring Security Updates
- Review deprecations in code and properties in application.properties
- Read the [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)