Feature: GasService

  Rule: Sync gas service behavior

    @Sync
    Scenario: test a single user with incremental metric updates
      Given we start WireMock server
      When user "1" deletes all previous gas metrics
      Then user "1" has no latest gas metric
      And user "1" has gas metrics list size "0"
      When user "1" puts gas metric id "23" meter "200" reading "2536.708" date "2023-07-28"
      Then user "1" gets latest gas metric id "23" meter "200" reading "2536.708" date "2023-07-28"
      And user "1" has gas metrics list size "1"
      When user "1" puts gas metric id "24" meter "200" reading "2601.125" date "2023-07-29"
      Then user "1" gets latest gas metric id "24" meter "200" reading "2601.125" date "2023-07-29"
      And user "1" has gas metrics list size "2"

    @Sync
    Scenario: test user isolation with three metrics per user
      Given we start WireMock server
      When user "1" deletes all previous gas metrics
      Then user "1" has no latest gas metric
      And user "1" has gas metrics list size "0"
      When user "1" puts gas metrics
        | id | meterId | reading  | date       |
        | 23 | 200     | 2536.708 | 2023-07-28 |
        | 24 | 200     | 2601.125 | 2023-07-29 |
        | 25 | 200     | 2710.900 | 2023-07-30 |
      Then user "1" gets latest gas metric id "25" meter "200" reading "2710.900" date "2023-07-30"
      And user "1" has gas metrics list size "3"

      When user "2" deletes all previous gas metrics
      Then user "2" has no latest gas metric
      And user "2" has gas metrics list size "0"
      When user "2" puts gas metrics
        | id | meterId | reading | date       |
        | 31 | 300     | 100.111 | 2024-02-10 |
        | 32 | 300     | 101.222 | 2024-02-11 |
        | 33 | 300     | 105.555 | 2024-02-12 |
      Then user "2" gets latest gas metric id "33" meter "300" reading "105.555" date "2024-02-12"
      And user "2" has gas metrics list size "3"
      And user "1" gets latest gas metric id "25" meter "200" reading "2710.900" date "2023-07-30"
      And user "1" has gas metrics list size "3"
