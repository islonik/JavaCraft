package my.javacraft.modules.app;

import java.util.ServiceLoader;
import my.javacraft.modules.util.Util;
import my.javacraft.modules.hello.HelloService;

public class App {
    public static void main(String[] args) {
        Iterable<HelloService> services = ServiceLoader.load(HelloService.class);
        HelloService service = services.iterator().next();

        Util.printMessage(service.sayHello());
    }
}
