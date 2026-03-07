package my.javacraft.soap2rest.soap.cucumber.step;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.en.Given;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.cucumber.spring.CucumberTestContext.SCOPE_CUCUMBER_GLUE;

@Slf4j
@Scope(SCOPE_CUCUMBER_GLUE)
public class WireMockDefinition {

    private static final String FIRST_METRIC_ADDED = "FIRST_METRIC_ADDED";
    private static final String SECOND_METRIC_ADDED = "SECOND_METRIC_ADDED";
    private static final String THIRD_METRIC_ADDED = "THIRD_METRIC_ADDED";
    private static final String ELECTRIC_METRIC_TEMPLATE = "electric/metric.json.hbs";
    private static final String ELECTRIC_METRICS_TEMPLATE = "electric/metrics.json.hbs";

    @Value("${rest-app.port}")
    int restPort;

    private static final Object LOCK = new Object();
    private static WireMockServer wireMockServer;

    @Given("we start WireMock server")
    public void setup () {
        synchronized (LOCK) {
            try {
                if (wireMockServer == null) {
                    wireMockServer = new WireMockServer(
                            options().port(restPort).templatingEnabled(true).globalTemplating(true)
                    );
                }

                if (!wireMockServer.isRunning()) {
                    wireMockServer.start();
                } else {
                    wireMockServer.resetAll();
                }

                wireMockServer.resetScenarios();

                addGasStubs(wireMockServer);
                addElectricStubs(wireMockServer);
                addSmartStubs(wireMockServer);

                log.info("Setup Stubs is done for MetricService");
            } catch (Exception e) {
                log.error("WireMock setup failed", e);
            }
        }
    }

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

    void addElectricStubs(WireMockServer wireMockServer) {
        addElectricStubsForAccount(
                wireMockServer,
                "1",
                List.of(
                        electricMetric(13, 100, "678.439", "2023-07-28"),
                        electricMetric(14, 100, "700.111", "2023-07-29"),
                        electricMetric(15, 100, "720.333", "2023-07-30")
                )
        );

        addElectricStubsForAccount(
                wireMockServer,
                "2",
                List.of(
                        electricMetric(21, 220, "54.321", "2024-01-15"),
                        electricMetric(22, 220, "60.999", "2024-01-16"),
                        electricMetric(23, 220, "61.222", "2024-01-17")
                )
        );
    }

    void addElectricStubsForAccount(
            WireMockServer wireMockServer,
            String accountId,
            List<Map<String, Object>> metrics
    ) {
        String scenarioName = "electric-flow-account-" + accountId;
        String baseUrl = "/api/v1/smart/" + accountId + "/electric";

        addDeleteStubForState(wireMockServer, scenarioName, baseUrl, STARTED);
        addDeleteStubForState(wireMockServer, scenarioName, baseUrl, FIRST_METRIC_ADDED);
        addDeleteStubForState(wireMockServer, scenarioName, baseUrl, SECOND_METRIC_ADDED);
        addDeleteStubForState(wireMockServer, scenarioName, baseUrl, THIRD_METRIC_ADDED);

        wireMockServer.stubFor(put(urlEqualTo(baseUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willSetStateTo(FIRST_METRIC_ADDED)
                .willReturn(withTemplateMetric(metrics.getFirst()))
        );

        wireMockServer.stubFor(put(urlEqualTo(baseUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(FIRST_METRIC_ADDED)
                .willSetStateTo(SECOND_METRIC_ADDED)
                .willReturn(withTemplateMetric(metrics.get(1)))
        );

        wireMockServer.stubFor(put(urlEqualTo(baseUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(SECOND_METRIC_ADDED)
                .willSetStateTo(THIRD_METRIC_ADDED)
                .willReturn(withTemplateMetric(metrics.get(2)))
        );

        wireMockServer.stubFor(get(urlEqualTo(baseUrl + "/latest"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200))
        );

        wireMockServer.stubFor(get(urlEqualTo(baseUrl + "/latest"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(FIRST_METRIC_ADDED)
                .willReturn(withTemplateMetric(metrics.getFirst()))
        );

        wireMockServer.stubFor(get(urlEqualTo(baseUrl + "/latest"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(SECOND_METRIC_ADDED)
                .willReturn(withTemplateMetric(metrics.get(1)))
        );

        wireMockServer.stubFor(get(urlEqualTo(baseUrl + "/latest"))
                .inScenario(scenarioName)
                .whenScenarioStateIs(THIRD_METRIC_ADDED)
                .willReturn(withTemplateMetric(metrics.get(2)))
        );

        wireMockServer.stubFor(get(urlEqualTo(baseUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(STARTED)
                .willReturn(withTemplateMetrics(List.of()))
        );

        wireMockServer.stubFor(get(urlEqualTo(baseUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(FIRST_METRIC_ADDED)
                .willReturn(withTemplateMetrics(metrics.subList(0, 1)))
        );

        wireMockServer.stubFor(get(urlEqualTo(baseUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(SECOND_METRIC_ADDED)
                .willReturn(withTemplateMetrics(metrics.subList(0, 2)))
        );

        wireMockServer.stubFor(get(urlEqualTo(baseUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(THIRD_METRIC_ADDED)
                .willReturn(withTemplateMetrics(metrics))
        );
    }

    Map<String, Object> electricMetric(int id, int meterId, String reading, String date) {
        return Map.of(
                "id", id,
                "meterId", meterId,
                "reading", reading,
                "date", date
        );
    }

    com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder withTemplateMetric(
            Map<String, Object> metric) {
        return aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withStatus(200)
                .withBodyFile(ELECTRIC_METRIC_TEMPLATE)
                .withTransformers("response-template")
                .withTransformerParameter("metric", metric);
    }

    com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder withTemplateMetrics(
            List<Map<String, Object>> metrics) {
        return aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withStatus(200)
                .withBodyFile(ELECTRIC_METRICS_TEMPLATE)
                .withTransformers("response-template")
                .withTransformerParameter("metrics", metrics);
    }

    void addDeleteStubForState(
            WireMockServer wireMockServer, String scenarioName, String baseUrl, String state) {
        wireMockServer.stubFor(delete(urlEqualTo(baseUrl))
                .inScenario(scenarioName)
                .whenScenarioStateIs(state)
                .willSetStateTo(STARTED)
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBody("true"))
        );
    }

    void addSmartStubs(WireMockServer wireMockServer) {
        wireMockServer.stubFor(put(urlEqualTo("/api/v1/smart/1"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBody("true")
                )
        );
        wireMockServer.stubFor(delete(urlEqualTo("/api/v1/smart/1"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBody("true")
                )
        );
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/smart/1/latest"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBodyFile("get_1_metrics.json")
                )
        );
        wireMockServer.stubFor(get(urlEqualTo("/api/v1/smart/1"))
                .willReturn(aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBodyFile("put_1_metrics.json")
                )
        );
    }

}
