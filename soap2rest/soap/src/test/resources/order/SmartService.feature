Feature: SmartService
  Scenario: submit & check latest metrics
    Given we start WireMock server
    When we send a SOAP request to delete all previous metrics
    When we send a SOAP request to put new metrics
    Then we send a SOAP request to get the latest metrics
    Then we send a SOAP request to get all metrics