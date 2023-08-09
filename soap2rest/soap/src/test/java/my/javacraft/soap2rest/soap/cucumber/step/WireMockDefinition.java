package my.javacraft.soap2rest.soap.cucumber.step;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import my.javacraft.soap2rest.soap.service.HttpCallService;
import my.javacraft.soap2rest.soap.service.order.MetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
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
        wireMockServer = new WireMockServer(restPort);
        wireMockServer.start();

        wireMockServer.stubFor(put(urlEqualTo("/api/v1/smart/1/gas"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBodyFile("response.json")));
        log.info("Setup Stubs is done for MetricService");
    }

    @Then("we shutdown WireMock server")
    public void teardown() {
        wireMockServer.stop();
    }
}
