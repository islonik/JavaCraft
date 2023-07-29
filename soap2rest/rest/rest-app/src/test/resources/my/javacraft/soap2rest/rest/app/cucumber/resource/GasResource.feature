Feature: GasResource
  Scenario: checking latest gas metric
    Given the account '1' doesn't have gas metrics
    When the account '1' submits a PUT request with a new gas reading: '100', '666.666', '2023-07-16'
    Then check the latest gas reading for the meterId = '100' is equal = '666.666'
    When the account '1' submits a PUT request with a new gas reading: '100', '686.666', '2023-07-17'
    Then check the latest gas reading for the meterId = '100' is equal = '686.666'
    When the account '1' submits a PUT request with a new gas reading: '100', '706.666', '2023-07-18'
    Then check the latest gas reading for the meterId = '100' is equal = '706.666'