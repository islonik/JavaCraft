Feature: test SearchController

  Scenario: index creation
    Given index 'movies' exists
    Then ingest 'movies.json' json file with 10 entities in 'movies' index

  Scenario: wildcard search
    Then wildcard search for 'imprisoned' in 'movies'
    # | title                    | director       | release_year | synopsis |
      | The Shawshank Redemption | Frank Darabont | 1994         | Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency. |

  Scenario: fuzzy search
    Then fuzzy search for 'imprtdoned' in 'movies'
    # | title                    | director       | release_year | synopsis |
      | The Shawshank Redemption | Frank Darabont | 1994         | Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency. |
