Feature: ElectricService

  Rule: Sync electric service behavior

    @Sync
    Scenario: test a single user with incremental metric updates
      Given we start WireMock server
      When user "1" deletes all previous electric metrics
      Then user "1" has no latest electric metric
      And user "1" has electric metrics list size "0"
      When user "1" puts electric metric id "13" meter "100" reading "678.439" date "2023-07-28"
      Then user "1" gets latest electric metric id "13" meter "100" reading "678.439" date "2023-07-28"
      And user "1" has electric metrics list size "1"
      When user "1" puts electric metric id "14" meter "100" reading "700.111" date "2023-07-29"
      Then user "1" gets latest electric metric id "14" meter "100" reading "700.111" date "2023-07-29"
      And user "1" has electric metrics list size "2"

    @Sync
    Scenario: test user isolation with three metrics per user
      Given we start WireMock server
      When user "1" deletes all previous electric metrics
      Then user "1" has no latest electric metric
      And user "1" has electric metrics list size "0"
      When user "1" puts electric metrics
        | id | meterId | reading | date       |
        | 13 | 100     | 678.439 | 2023-07-28 |
        | 14 | 100     | 700.111 | 2023-07-29 |
        | 15 | 100     | 720.333 | 2023-07-30 |
      Then user "1" gets latest electric metric id "15" meter "100" reading "720.333" date "2023-07-30"
      And user "1" has electric metrics list size "3"

      When user "2" deletes all previous electric metrics
      Then user "2" has no latest electric metric
      And user "2" has electric metrics list size "0"
      When user "2" puts electric metrics
        | id | meterId | reading | date       |
        | 21 | 220     | 54.321  | 2024-01-15 |
        | 22 | 220     | 60.999  | 2024-01-16 |
        | 23 | 220     | 61.222  | 2024-01-17 |
      Then user "2" gets latest electric metric id "23" meter "220" reading "61.222" date "2024-01-17"
      And user "2" has electric metrics list size "3"
      And user "1" gets latest electric metric id "15" meter "100" reading "720.333" date "2023-07-30"
      And user "1" has electric metrics list size "3"
