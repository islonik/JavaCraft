package my.javacraft.modules.hello.impl;

import my.javacraft.modules.hello.HelloService;

public class HelloServiceImpl implements HelloService {

    public static void printHelloWorld() {
        System.out.println("Hello, World!");
        System.out.println("Hello, Modules!");
    }

    public String sayHello() {
        return "Hello World from Modules!";
    }
}
