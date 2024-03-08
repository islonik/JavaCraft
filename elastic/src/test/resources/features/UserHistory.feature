Feature: UserHistoryResource

  Scenario: prepare data
    Given index 'user_history' exists

  Scenario: add new events
    Given user 'nl0000' doesn't have any events
    When add new event with expected result = 'Created'
#     | userId | documentId | searchType | searchPattern | client |
      | nl0000 | 12345      | People     | Nikita        | WEB    |
    When add new event with expected result = 'Updated'
#     | userId | documentId | searchType | searchPattern | client |
      | nl0000 | 12345      | People     | Nikita        | WEB    |
    When add new event with expected result = 'Updated'
#     | userId | documentId | searchType | searchPattern | client |
      | nl0000 | 12345      | People     | Nikita        | WEB    |
    Then user 'nl0000' has 3 hit counts for documentId = '12345', searchType = 'People' and pattern = 'Nikita'

  Scenario: test sorting order
    Given user 'nl0000' doesn't have any events
    When add new event with expected result = 'Created'
#     | userId | documentId | searchType | searchPattern | client |
      | nl0000 | 12345      | People     | Nikita        | WEB    |
    When add new event with expected result = 'Created'
#     | userId | documentId | searchType | searchPattern | client |
      | nl0000 | 12345      | Company    | Microsoft     | WEB    |
    When add new event with expected result = 'Updated'
#     | userId | documentId | searchType | searchPattern | client |
      | nl0000 | 12345      | Company    | Microsoft     | WEB    |
    Then user 'nl0000' has next sorting results
#     | Pattern   | order |
      | Microsoft | 2     |
      | Nikita    | 1     |


