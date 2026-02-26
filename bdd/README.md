# Behaviour Driven Development

This micro sample demonstrates how to use Cucumber library with Spring boot.

## Content
* [Maven](#maven)
* [Tests](#tests)

### Maven
Add cucumber libraries in your dependecies:
```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-spring</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>${cucumber.version}</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-suite</artifactId>
    <version>${junit.platform.suite.version}</version>
    <scope>test</scope>
</dependency>
```

Plugin to run Cucumber and JUnit 5 tests in <b>maven</b> builds
```xml
<build>
    <plugins>
        <!-- makes cucumber and junit5 tests run in maven -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>${maven.surefire.plugin.version}</version>
            <configuration>
                <includes>
                    <include>**/CucumberRunner.java</include>
                    <include>**/*Test.java</include>
                </includes>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Tests

CucumberRunner is necessary to specify Cucumber configuration.
```java
@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:target/cucumber-reports/cucumber.html, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "my.javacraft.bdd.cucumber")
public class CucumberRunner {
}
```

CucumberSpringConfiguration is necessary to inject cucumber in java Application.
```java
@CucumberContextConfiguration
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CucumberSpringConfiguration {
}
```

Java class BMIStepDefinition provides actual @Given, @When and @Then implementation.

Resource file <b>BMI.feature</b> uses built-in <b>DataTable</b> to supply a table with data.
