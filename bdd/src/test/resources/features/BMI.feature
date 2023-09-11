Feature: BMI service

  Scenario: test calculate method
    When a person has weight = '75' kg and height = '1.75' metres
    Then that person should have BMI = '24.49' kg m2

    When a person has weight = '150' lbs and height = '65' inches
    Then that person should have BMI = '24.96'

  Scenario: test calculate method using batch approach
    When we use batch to test BMI calculator
      | 75 | 1.75 | false | 24.49 |
      | 150 | 65 | true | 24.96 |