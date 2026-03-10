Feature: Blocking server benchmark

  Rule: Performance benchmark

    @Performance
    @ServerPlatform
    @ClientPlatform
    Scenario: [Performance] PlatformServer + PlatformThreadClient benchmark includes warmups and 3 measured runs
      Given the platform server is running on port 8260
      When performance benchmark for platform server and platform client runs 2 warmups and 3 measured runs with 100 clients and 100 messages on port 8260

    @Performance
    @ServerPlatform
    @ClientVirtual
    Scenario: [Performance] PlatformServer + VirtualThreadClient benchmark includes warmups and 3 measured runs
      Given the platform server is running on port 8261
      When performance benchmark for platform server and virtual client runs 2 warmups and 3 measured runs with 100 clients and 100 messages on port 8261

    @Performance
    @ServerVirtual
    @ClientPlatform
    Scenario: [Performance] VirtualServer + PlatformThreadClient benchmark includes warmups and 3 measured runs
      Given the virtual server is running on port 8160
      When performance benchmark for virtual server and platform client runs 2 warmups and 3 measured runs with 100 clients and 100 messages on port 8160

    @Performance
    @ServerVirtual
    @ClientVirtual
    Scenario: [Performance] VirtualServer + VirtualThreadClient benchmark includes warmups and 3 measured runs
      Given the virtual server is running on port 8161
      When performance benchmark for virtual server and virtual client runs 2 warmups and 3 measured runs with 100 clients and 100 messages on port 8161

    @PerformanceSummary
    Scenario: [Performance] Compare persisted averages from all server/client combinations
      Then performance averages for all server - client combinations are compared and total execution time is printed
