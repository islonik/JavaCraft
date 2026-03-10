package my.javacraft.echo.standard.step;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;

public class CommonStepDefinitions {

    private final StandardStepDefinitions standardSteps = new StandardStepDefinitions();

    @After
    public void cleanup() {
        standardSteps.cleanup();
    }

    @Given("the virtual server is running on port {int}")
    public void startVirtualServer(int port) {
        standardSteps.startVirtualServer(port);
    }

    @Given("the platform server is running on port {int}")
    public void startPlatformServer(int port) {
        standardSteps.startPlatformServer(port);
    }
}
