package my.javacraft.modules.app;

import java.util.ServiceLoader;
import my.javacraft.modules.util.Util;
import my.javacraft.modules.hello.HelloService;

public class App {

    public static void main(String[] args) {
        App app = new App();
        Util.printMessage(app.resolveMessage());
    }

    String resolveMessage() {
        return loadHelloService().sayHello();
    }

    HelloService loadHelloService() {
        return ServiceLoader.load(HelloService.class)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No HelloService provider found"));
    }
}
