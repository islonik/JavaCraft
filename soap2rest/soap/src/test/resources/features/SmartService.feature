Feature: SmartService

  Rule: Sync smart service behavior

    @Sync
    Scenario: test a single user with incremental metric updates
      Given we start WireMock server
      When user "1" deletes all previous smart metrics
      Then user "1" has no latest smart metrics
      And user "1" has smart metrics list size "0"
      When user "1" puts smart metrics gas id "23" meter "200" reading "2531.111" date "2023-07-28" electric id "13" meter "100" reading "674.444" date "2023-07-28"
      Then user "1" gets latest smart metrics gas id "23" meter "200" reading "2531.111" date "2023-07-28" electric id "13" meter "100" reading "674.444" date "2023-07-28"
      And user "1" has smart metrics list size "1"
      When user "1" puts smart metrics gas id "24" meter "200" reading "2537.777" date "2023-07-29" electric id "14" meter "100" reading "678.888" date "2023-07-29"
      Then user "1" gets latest smart metrics gas id "24" meter "200" reading "2537.777" date "2023-07-29" electric id "14" meter "100" reading "678.888" date "2023-07-29"
      And user "1" has smart metrics list size "2"

    @Sync
    Scenario: test user isolation with three metrics per user
      Given we start WireMock server
      When user "1" deletes all previous smart metrics
      Then user "1" has no latest smart metrics
      And user "1" has smart metrics list size "0"
      When user "1" puts smart metrics
        | gasId | gasMeterId | gasReading | gasDate     | elecId | elecMeterId | elecReading | elecDate    |
        | 23    | 200        | 2531.111   | 2023-07-28  | 13     | 100         | 674.444     | 2023-07-28  |
        | 24    | 200        | 2537.777   | 2023-07-29  | 14     | 100         | 678.888     | 2023-07-29  |
        | 25    | 200        | 2600.001   | 2023-07-30  | 15     | 100         | 699.111     | 2023-07-30  |
      Then user "1" gets latest smart metrics gas id "25" meter "200" reading "2600.001" date "2023-07-30" electric id "15" meter "100" reading "699.111" date "2023-07-30"
      And user "1" has smart metrics list size "3"

      When user "2" deletes all previous smart metrics
      Then user "2" has no latest smart metrics
      And user "2" has smart metrics list size "0"
      When user "2" puts smart metrics
        | gasId | gasMeterId | gasReading | gasDate     | elecId | elecMeterId | elecReading | elecDate    |
        | 31    | 300        | 100.111    | 2024-02-10  | 41     | 400         | 10.250      | 2024-02-10  |
        | 32    | 300        | 101.222    | 2024-02-11  | 42     | 400         | 10.750      | 2024-02-11  |
        | 33    | 300        | 105.555    | 2024-02-12  | 43     | 400         | 11.900      | 2024-02-12  |
      Then user "2" gets latest smart metrics gas id "33" meter "300" reading "105.555" date "2024-02-12" electric id "43" meter "400" reading "11.900" date "2024-02-12"
      And user "2" has smart metrics list size "3"
      And user "1" gets latest smart metrics gas id "25" meter "200" reading "2600.001" date "2023-07-30" electric id "15" meter "100" reading "699.111" date "2023-07-30"
      And user "1" has smart metrics list size "3"
