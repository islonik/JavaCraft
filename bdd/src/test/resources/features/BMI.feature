Feature: BMI service

  Scenario: test calculate method using metric system
    Given the BMI calculator is available
    When a person has weight = '75' kg and height = '1.75' metres
    Then that person should have BMI = '24.49' kg m2

  Scenario: test calculate method using imperial system
    Given the BMI calculator is available
    When a person has weight = '150' lbs and height = '65' inches
    Then that person should have BMI = '24.96'

  Scenario: test calculate method using batch approach
    Given the BMI calculator is available
    When we use batch to test BMI calculator
      | 75 | 1.75 | false | 24.49 |
      | 150 | 65 | true | 24.96 |

  Scenario: test BMI category classification
    Given the BMI calculator is available
    When we classify BMI values into categories
      | 15.00 | Severe thinness as your BMI is less than 16.00    |
      | 16.50 | Moderate thinness as your BMI is less than 16.99  |
      | 17.50 | Mild thinness as your BMI is less than 18.49      |
      | 22.00 | Normal range as your BMI is less than 24.99       |
      | 27.00 | Pre-obese as your BMI is less than 29.99          |
      | 32.00 | Obese class I as your BMI is less than 34.99      |
      | 37.00 | Obese class II as your BMI is less than 39.99     |
      | 42.00 | Obese class III as your BMI is over 40            |
