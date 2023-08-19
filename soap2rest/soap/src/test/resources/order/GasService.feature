Feature: GasService
  Scenario: submit & check latest gas metric
    Given we start WireMock server
    When we send a SOAP request to submit a new gas metric