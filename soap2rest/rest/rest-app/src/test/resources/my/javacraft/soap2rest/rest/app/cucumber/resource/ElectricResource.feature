Feature: ElectricResource
  Scenario: checking latest electric metric
    Given the account '1' doesn't have electric metrics
    When an account '1' submits a PUT request with a new electric reading: '200', '1777.777', '2023-07-15'
    Then check the latest electric reading for the meterId = '200' is equal = '1777.777'
    When an account '1' submits a PUT request with a new electric reading: '200', '1787.777', '2023-07-16'
    Then check the latest electric reading for the meterId = '200' is equal = '1787.777'
    When an account '1' submits a PUT request with a new electric reading: '200', '1797.777', '2023-07-17'
    Then check the latest electric reading for the meterId = '200' is equal = '1797.777'