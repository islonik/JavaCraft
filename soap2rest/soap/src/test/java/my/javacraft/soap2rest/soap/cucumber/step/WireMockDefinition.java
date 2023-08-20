package my.javacraft.soap2rest.soap.cucumber.step;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class WireMockDefinition {

    @Value("${rest-app.port}")
    int restPort;

    WireMockServer wireMockServer;

    @Given("we start WireMock server")
    public void setup () {
        try {
            wireMockServer = new WireMockServer(restPort);

            wireMockServer.start();

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

            // electric
            wireMockServer.stubFor(put(urlEqualTo("/api/v1/smart/1/electric"))
                    .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBodyFile("put_1_electric.json")
                    )
            );
            wireMockServer.stubFor(delete(urlEqualTo("/api/v1/smart/1/electric"))
                    .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(200)
                            .withBody("true")
                    )
            );
            wireMockServer.stubFor(get(urlEqualTo("/api/v1/smart/1/electric/latest"))
                    .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(200)
                            .withBodyFile("put_1_electric.json")
                    )
            );
            wireMockServer.stubFor(get(urlEqualTo("/api/v1/smart/1/electric"))
                    .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .withStatus(200)
                            .withBodyFile("get_1_electric.json")
                    )
            );
            log.info("Setup Stubs is done for MetricService");
        } catch (Exception e) {
            log.info("WireMock start exception: " + e.getMessage());
        }
    }

    @Then("we shutdown WireMock server")
    public void teardown() {
//        try {
//            wireMockServer.stop();
//            log.info("WireMock stopped.");
//        } catch (Exception e) {
//            log.info("WireMock stop exception: " + e.getMessage());
//        }
    }
}
