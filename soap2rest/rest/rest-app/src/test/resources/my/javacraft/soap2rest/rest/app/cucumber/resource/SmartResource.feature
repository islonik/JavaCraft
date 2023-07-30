Feature: SmartResource
  Scenario: insert two types of two values, no issues
    Given the account 1 doesn't have any metrics
    When the account 1 submits a PUT request with new metrics
      | gas | 100 | 686.666 | 2023-07-17 |
      | gas | 100 | 700.502 | 2023-07-20 |
      | ele | 200 | 2345.505 | 2023-07-17 |
      | ele | 200 | 2536.708 | 2023-07-20 |
    Then check the latest gas reading for the meterId = 100 is equal = 700.502
    Then check the latest electric reading for the meterId = 200 is equal = 2536.708

  Scenario: insert two types of two values, validation fails, transaction rollouts
    Given the account 1 doesn't have any metrics
    When the account 1 submits a PUT request with new metrics
      | gas | 100 | 686.666 | 2023-07-17 |
      | gas | 100 | 700.502 | 2023-07-20 |
      | ele | 200 | 2345.505 | 2023-07-20 |
      | ele | 200 | 2536.708 | 2023-07-17 |
    Then check there is no gas readings for the meterId = 100
    Then check there is no electric readings for the meterId = 200
