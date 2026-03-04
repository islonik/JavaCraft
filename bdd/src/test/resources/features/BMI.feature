Feature: BMI service

  Scenario: A person gets BMI from metric inputs
    Given the BMI calculator is available
    When a person has weight = '75' kg and height = '1.75' metres
    Then that person should have BMI = '24.49' kg m2

  Scenario: A person gets BMI from imperial inputs
    Given the BMI calculator is available
    When a person has weight = '150' lbs and height = '65' inches
    Then that person should have BMI = '24.96'

  Scenario: Several BMI calculations can be checked in one batch
    Given the BMI calculator is available
    When we use batch to test BMI calculator
   #  | weight | height | is imperial | BMI   |
      | 75     | 1.75   | false       | 24.49 |
      | 150    | 65     | true        | 24.96 |

  Scenario: BMI categories are assigned at boundary values
    Given the BMI calculator is available
    When we classify BMI values into categories
   #  | BMI   | message                                                  |
      | 0.01  | Severe thinness as your BMI is less than 16.00           |
      | 15.99 | Severe thinness as your BMI is less than 16.00           |
      | 16.00 | Moderate thinness as your BMI is between 16.00 and 16.99 |
      | 16.99 | Moderate thinness as your BMI is between 16.00 and 16.99 |
      | 17.00 | Mild thinness as your BMI is between 17.00 and 18.49     |
      | 18.49 | Mild thinness as your BMI is between 17.00 and 18.49     |
      | 18.50 | Normal range as your BMI is between 18.50 and 24.99      |
      | 22.00 | Normal range as your BMI is between 18.50 and 24.99      |
      | 24.99 | Normal range as your BMI is between 18.50 and 24.99      |
      | 25.00 | Pre-obese as your BMI is between 25.00 and 29.99         |
      | 29.99 | Pre-obese as your BMI is between 25.00 and 29.99         |
      | 30.00 | Obese class I as your BMI is between 30.00 and 34.99     |
      | 34.99 | Obese class I as your BMI is between 30.00 and 34.99     |
      | 35.00 | Obese class II as your BMI is between 35.00 and 39.99    |
      | 39.99 | Obese class II as your BMI is between 35.00 and 39.99    |
      | 40.00 | Obese class III as your BMI is 40.00 or more             |
