package my.javacraft.echo.standard.step;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class BenchmarkStepDefinitions {

    private final StandardStepDefinitions standardSteps = new StandardStepDefinitions();

    @When("performance benchmark for {word} server and {word} client runs {int} warmups and {int} measured runs with {int} clients and {int} messages on port {int}")
    public void runThreadPerformanceBenchmark(
            String serverType,
            String clientType,
            int warmups,
            int measuredRuns,
            int clientCount,
            int messagesPerClient,
            int port) {
        standardSteps.runThreadPerformanceBenchmark(
                serverType,
                clientType,
                warmups,
                measuredRuns,
                clientCount,
                messagesPerClient,
                port
        );
    }

    @Then("performance averages for all server - client combinations are compared and total execution time is printed")
    public void comparePerformanceAveragesFromSeparateRuns() {
        standardSteps.comparePerformanceAveragesFromSeparateRuns();
    }
}
