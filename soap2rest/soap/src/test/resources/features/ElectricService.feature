Feature: ElectricService
  Scenario: submit & check latest electric metric
    Given we start WireMock server
    When we send a SOAP request to delete all previous electric metrics
    When we send a SOAP request to put a new electric metric
    Then we send a SOAP request to get the latest electric metric
    Then we send a SOAP request to get all electric metrics