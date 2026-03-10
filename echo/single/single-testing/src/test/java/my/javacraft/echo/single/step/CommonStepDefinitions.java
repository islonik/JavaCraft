package my.javacraft.echo.single.step;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import my.javacraft.echo.single.client.SingleClient;
import my.javacraft.echo.single.server.SingleServer;

public class CommonStepDefinitions {

    private static final Map<String, SingleClient> connections = new ConcurrentHashMap<>();
    private static final List<ExecutorService> serverExecutors = new ArrayList<>();

    @After
    public void cleanup() {
        connections.values().forEach(SingleClient::close);
        connections.clear();
        serverExecutors.forEach(ExecutorService::shutdownNow);
        serverExecutors.clear();
    }

    @Given("socket server started up on port = '{int}'")
    @Given("the single-thread server is running on port {int}")
    public void startUpSocketServer(int port) {
        SingleServer server = new SingleServer(port);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(server);
        serverExecutors.add(executorService);
    }

    static Map<String, SingleClient> connections() {
        return connections;
    }

}
