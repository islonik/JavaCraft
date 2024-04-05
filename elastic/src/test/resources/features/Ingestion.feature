Feature: UserHistoryResource

  Scenario: ingest movies
    Given index 'movies' exists
    Then ingest 'movies.json' json file with 250 entities in 'movies' index

  Scenario: ingest books
    Given index 'books' exists
    Then ingest 'books.json' json file with 100 entities in 'books' index