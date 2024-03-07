Feature: UserHistoryResource

  Scenario: prepare data
    Given index 'user_history' exists
    Given user 'nl8888' doesn't have any events

  Scenario: add new events
    When add new event with expected result = 'Created'
#     | userId | documentId | searchType | searchPattern | client |
      | nl8888 | 12345      | People     | Nikita        | WEB    |
    When add new event with expected result = 'Updated'
#     | userId | documentId | searchType | searchPattern | client |
      | nl8888 | 12345      | People     | Nikita        | WEB    |

