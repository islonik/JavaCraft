Feature: GasService
  Scenario: submit & check latest gas metric
    Given we start WireMock server
    When we send a SOAP request to delete all previous gas metrics
    When we send a SOAP request to put a new gas metric
    Then we send a SOAP request to get the latest gas metric