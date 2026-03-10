Feature: Netty benchmark

  Rule: Performance benchmark

    @Performance
    Scenario: [Performance] NettyServer + NettyClient benchmark includes warmups and 3 measured runs
      Given the Netty server is running on port 8100
      When performance benchmark for netty server and netty client runs 2 warmups and 3 measured runs with 100 clients and 100 messages on port 8100

    @PerformanceSummary
    Scenario: [Performance] Compare persisted average for netty server and netty client benchmark
      Then performance average for netty server and netty client is printed with total execution time
