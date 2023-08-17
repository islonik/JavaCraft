Feature: MetricService
  Scenario: checking latest electric metric
    Given we start WireMock server
    When we send SOAP request
    When we submit a new gas metric
    Then we shutdown WireMock server