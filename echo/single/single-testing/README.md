# single-testing

## Module purpose

`single-testing` contains Cucumber end-to-end tests for the non-blocking single-thread echo implementation.
It validates behavior across real classes from:

- `single-client`
- `single-server`

## What is covered

Feature files:

- `src/test/resources/features/single.feature`
  - functional scenarios
  - edge-case/format scenarios
  - high-load scenario
- `src/test/resources/features/benchmark.feature`
  - single benchmark scenario (SingleServer + SingleClient)
  - persisted summary scenario

Step definition classes:

- `SingleStepDefinitions` - functional and load steps with shared implementation.
- `BenchmarkStepDefinitions` - benchmark-only steps.
- `CommonStepDefinitions` - shared `@Given` server startup and `@After` cleanup.

## Tags

- `@Performance` - benchmark scenario
- `@PerformanceSummary` - reads persisted benchmark summary and prints final result

## Benchmark flow

Benchmark step:

- runs warmups (not measured)
- runs measured iterations
- prints per-run and aggregate metrics
- persists summary under `target/performance-results/single-single.properties`

Summary step:

- reads persisted benchmark summary
- prints `[PERF][FINAL]` output for this single case
- prints total measured time and total benchmark scenario time

## How tests are run

Runner:

- `my.javacraft.echo.single.CucumberRunner`

Reports:

- `target/cucumber-reports/cucumber.html`
- `target/cucumber-reports/cucumber.json`

## Execute from project root

Run full suite for this module:

```bash
mvn -pl echo/single/single-testing -am test
```

Run only benchmark scenario:

```bash
mvn -pl echo/single/single-testing -am test -Dcucumber.filter.tags='@Performance'
```

Run only benchmark summary:

```bash
mvn -pl echo/single/single-testing -am test -Dcucumber.filter.tags='@PerformanceSummary'
```

If `@PerformanceSummary` runs before benchmark results exist, it fails by design.
