# Soap to Rest - Soap

## Content
* [SOAP](#SOAP) 
* [WSDL -> Java](#WSDL-to-Java)
* [Cucumber](#cucumber) 
* [SoapUI](#SoapUI) 

### SOAP
<b>SOAP Web Endpoint</b>:
```java
@Endpoint
public class DSEndpoint {

    public static final String NAMESPACE = "http://www.nikilipa.org/SoapServiceRequest/v01";

    @Autowired
    EndpointService endpointService;

    @PayloadRoot(
            namespace = NAMESPACE,
            localPart = "DSRequest")
    @ResponsePayload
    public DSResponse getCountry(@RequestPayload DSRequest dsRequest) {
        return endpointService.executeDsRequest(dsRequest);
    }
}
```

### WSDL to Java
The endpoint requires generated java classes. 

These classes are generated during maven build process by <b>jaxws-maven-plugin</b>

Plugin itself:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.sun.xml.ws</groupId>
            <artifactId>jaxws-maven-plugin</artifactId>
            <version>4.0.1</version>
            <configuration>
                <!-- use all wsdl files from there -->
                <wsdlDirectory>${project.basedir}/src/main/resources/wsdl</wsdlDirectory>
                <packageName>my.javacraft.soap2rest.soap.generated.ds.ws</packageName>
                <sourceDestDir>${project.build.directory}/generated-sources</sourceDestDir>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>wsimport</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Cucumber
All HTTP calls to RESTful services are mocked by WireMock.

```java
    void addGasStubs(WireMockServer wireMockServer) {
        wireMockServer.stubFor(put(urlEqualTo("/api/v1/smart/1/gas"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBodyFile("put_1_gas.json")
                )
        );
        wireMockServer.stubFor(delete(urlEqualTo("/api/v1/smart/1/gas"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBody("true")
                )
        );
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/smart/1/gas/latest"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBodyFile("put_1_gas.json")
                )
        );
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/smart/1/gas"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBodyFile("get_1_gas.json")
                )
        );
    }
```

### SoapUI
SOAP module contains SoapUI folder for SoapUI project.
SoapUI project allows you to send/test samples of SOAP messages to the SOAP Web Endpoint.

https://www.soapui.org/