XL-Spaceship simulator.
==================================
XL-Spaceship is a guessing game (a modified version of battleship game) where you play against AI.

Technology stack: Java21, Spring framework, Spring Boot, Spring Mvc, HTML, JQuery, Ajax and Thymeleaf.

To be able to build this project you should have installed maven.
./build.sh - will build this project.

To be able to run this project: you should launch run.sh and runAI.sh files (you can customize these files in advance).

## Design

![plot](./docs/sequence.png)

## Dependency management

### Overview of dependencies

```bash
mvn dependency:tree
```

### To find unused dependencies
```bash
mvn dependency:analyze
```

### To check new dependencies
```bash
mvn versions:display-dependency-updates
```

*******************************************
### How to enable Swagger UI
*******************************************

#### 1. Add dependency
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

#### 2. Add @OpenAPIDefinition in Application file
```java

@OpenAPIDefinition(info = @Info(
        title = "Battleship game",
        version = "1.0",
        description = "Swagger UI for Battleship online game"
))
public class Application implements ApplicationRunner {
```

#### 3. Put OpenAPI annotations
##### 3.1 @Tag annotation

```java
@Tag(name = "User", description = "List of APIs for user actions")
```

so it might look like:

```java
@Slf4j
@RestController
@Tag(name = "User", description = "List of APIs for user actions")
@RequestMapping(value = RestResources.USER_PATH)
public class UserResource {
```

##### 3.2. @Operation annotation

```java
    @Operation(
        summary = "Get status by gameId.",
        description = "API to get status of the game."
)
```

so it might look like:

```java
/*
    GET http://localhost:8079/xl-spaceship/user/game/match-1
*/
@Operation(
        summary = "Get status by gameId.",
        description = "API to get status of the game."
)
@RequestMapping(
        value = "/game/{gameId}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
)
public ResponseEntity<?> getStatusByGameId(
```

#### 4. Open URL link
```bash
http://localhost:8079/swagger-ui/index.html
```

*******************************************
### How to enable pretty json output
*******************************************
<b>Attention!!!</b>

If you are running a boot app but elected to configure the web application yourself, either by @EnableWebMvc annotation 
or implementing WebMvcConfigurationSupport, then you need to configure your swagger-ui on your own (add resource handlers). 
The web jar will not be auto configured for you.

```java
@Configuration
public class OverrideDefaultConfiguration extends WebMvcConfigurationSupport {

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                jacksonConverter.setPrettyPrint(true);
            }
        }
    }

}
```
