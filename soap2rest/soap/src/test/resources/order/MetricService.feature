Feature: MetricService
  Scenario: checking latest electric metric
    Given we start WireMock server
    When we send a SOAP request to submit a new gas metric
    Then we shutdown WireMock server