Feature: UserHistoryResource

  Scenario: ingest movies
    Given index 'movies' exists
    Then ingest 'movies.json' json file with 10 entities in 'movies' index