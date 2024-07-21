module module.app {
    // transitive says that the module dependency also should bring all required dependencies to run it
    requires transitive module.util;
    requires transitive module.hello;

    uses my.javacraft.modules.hello.HelloService;
}