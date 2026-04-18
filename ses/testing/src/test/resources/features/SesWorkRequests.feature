Feature: SES work requests

  Scenario: create approve start complete happy path
    When a work request is created with title "Ship projection docs", priority "CRITICAL", budget code "PLATFORM-2026", estimate 40, requested by "Nikita"
    And work request "last" is approved by "Lead"
    And work request "last" is started by "Worker"
    And work request "last" is completed by "Worker"
    Then the last command response has HTTP 200 and status "COMPLETED"
    And projected work request "last" eventually has status "COMPLETED"
    And budget projection "PLATFORM-2026" eventually has reserved 40 and remaining 210
    And work request "last" timeline eventually equals statuses "CREATED,ACCEPTED,RUNNING,COMPLETED"

  Scenario: create approve denied rejected path
    When a work request is created with title "Overspend ops budget", priority "MAJOR", budget code "OPS-2026", estimate 130, requested by "Nikita"
    And work request "last" is approved by "Lead"
    Then the last command response has HTTP 200 and status "REJECTED"
    And projected work request "last" eventually has status "REJECTED"
    And budget projection "OPS-2026" eventually has reserved 0 and remaining 120
    And work request "last" timeline eventually equals statuses "CREATED,REJECTED"

  Scenario: create explicit reject path
    When a work request is created with title "Pause risky migration", priority "NORMAL", budget code "RND-2026", estimate 20, requested by "Nikita"
    And work request "last" is rejected by "Operator" with reason "Missing sign-off"
    Then the last command response has HTTP 200 and status "REJECTED"
    And projected work request "last" eventually has status "REJECTED"
    And budget projection "RND-2026" eventually has reserved 0 and remaining 180
    And work request "last" timeline eventually equals statuses "CREATED,REJECTED"

  Scenario: invalid transition appends no extra event
    When a work request is created with title "Cannot start yet", priority "MINOR", budget code "OPS-2026", estimate 10, requested by "Nikita"
    And starting work request "last" by "Worker" fails with conflict
    Then the last error response has HTTP 409
    And work request "last" timeline eventually equals statuses "CREATED"

  Scenario: replay rebuild reproduces request and budget projections
    When a work request is created with title "First rebuild request", priority "CRITICAL", budget code "PLATFORM-2026", estimate 35, requested by "Nikita"
    And work request "last" is approved by "Lead"
    And a work request is created with title "Second rebuild request", priority "NORMAL", budget code "RND-2026", estimate 15, requested by "Nikita"
    And work request "last" is rejected by "Operator" with reason "Manual rejection"
    And projections are rebuilt
    Then rebuild response reports 4 events, 2 requests, and 3 budgets
    And projected work request list eventually contains 2 items
    And budget projection "PLATFORM-2026" eventually has reserved 35 and remaining 215
    And budget projection "RND-2026" eventually has reserved 0 and remaining 180
