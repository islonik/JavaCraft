Feature: ElectricService
  Scenario: submit & check latest electric metric
    Given we start WireMock server
    When we send a SOAP request to submit a new electric metric