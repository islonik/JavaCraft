Feature: test Scheduler jobs

  Scenario: remove old documents
    Given there are 10 outdated records
    Then execute cleanup job with expected result of 10