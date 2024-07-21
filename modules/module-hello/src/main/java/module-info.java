module module.hello {
    exports my.javacraft.modules.hello;
    provides my.javacraft.modules.hello.HelloService with my.javacraft.modules.hello.impl.HelloServiceImpl;
}