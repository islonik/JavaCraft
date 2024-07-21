// when someone does 'requires' module.util, they will have access to the public types
// in our 'my.javacraft.modules.util', but not any other package.
module module.util {
    // we declare a 'my.javacraft.modules.util' package as exported.
    // but, we also list which modules we are allowing to import this package as a 'requires'.
    exports my.javacraft.modules.util to module.app;
}