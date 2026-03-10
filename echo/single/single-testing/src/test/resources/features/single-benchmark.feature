Feature: Single-thread benchmark

  Rule: Performance benchmark

    @Performance
    Scenario: [Performance] SingleServer + SingleClient benchmark includes warmups and 3 measured runs
      Given the single-thread server is running on port 8100
      When performance benchmark for single server and single client runs 2 warmups and 3 measured runs with 100 clients and 100 messages on port 8100

    @PerformanceSummary
    Scenario: [Performance] Compare persisted average for single server and single client benchmark
      Then performance average for single server and single client is printed with total execution time
