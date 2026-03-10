Feature: Selector benchmark

  Rule: Performance benchmark

    @Performance
    Scenario: [Performance] SelectorServer + SelectorClient benchmark includes warmups and 3 measured runs
      Given the selector server is running on port 8100
      When performance benchmark for selector server and selector client runs 2 warmups and 3 measured runs with 100 clients and 100 messages on port 8100

    @PerformanceSummary
    Scenario: [Performance] Compare persisted average for selector server and selector client benchmark
      Then performance average for selector server and selector client is printed with total execution time
