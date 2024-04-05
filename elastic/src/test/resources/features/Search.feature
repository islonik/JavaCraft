Feature: test SearchController

  Scenario: index creation
    Given index 'movies' exists
    Then ingest 'movies.json' json file with 250 entities in 'movies' index

  Scenario: wildcard search
    Then wildcard search for 'Scottish' in 'movies'
    # | name       | director   | ranking | release_year | synopsis |
      | Braveheart | Mel Gibson | 91      | 1995         | Scottish warrior William Wallace leads his countrymen in a rebellion to free his homeland from the tyranny of King Edward I of England. |

  Scenario: fuzzy search
    Then fuzzy search for 'Skywadker' in 'movies'
    # | name                                           | director       | ranking | release_year | synopsis |
      | Star Wars: Episode V - The Empire Strikes Back | Irvin Kershner | 15      | 1980         | After the Rebels are overpowered by the Empire, Luke Skywalker begins his Jedi training with Yoda, while his friends are pursued across the galaxy by Darth Vader and bounty hunter Boba Fett. |
      | Star Wars                                      | George Lucas   | 16      | 1977         | Luke Skywalker joins forces with a Jedi Knight, a cocky pilot, a Wookiee and two droids to save the galaxy from the Empire's world-destroying battle station, while also attempting to rescue Princess Leia from the mysterious Darth ... |

  Scenario: span search
    Then span search for 'redemption compassion' in 'movies'
    # | name                     | director       | ranking | release_year | synopsis |
      | The Shawshank Redemption | Frank Darabont | 1       | 1994         | Over the course of several years, two convicts form a friendship, seeking consolation and, eventually, redemption through basic compassion. |

  Scenario: search
    Then search for 'imprisoned' in 'movies'
    # | name               | director        | ranking | release_year | synopsis |
      | A Clockwork Orange | Stanley Kubrick | 115     | 1971         | In the future, a sadistic gang leader is imprisoned and volunteers for a conduct-aversion experiment, but it doesn't go as planned.   |
      | Oldeuboi           | Park Chan-wook  | 92      | 2003         | After being kidnapped and imprisoned for fifteen years, Oh Dae-Su is released, only to find that he must find his captor in five days.|
