# Java Platform Module System (JPMS)

JPMS was introduced in Java 9 to organize applications into modules with explicit dependencies and strong encapsulation.

This module group demonstrates Java Platform Module System (JPMS) basics:

- `module.util`: exports utility API to a specific consumer module.
- `module.hello`: defines a service contract and publishes a provider.
- `module.app`: consumes the service via `ServiceLoader` using `uses`.

## Module graph

- `module.app` requires `module.util`
- `module.app` requires `module.hello`
- `module.app` uses `my.javacraft.modules.hello.HelloService`
- `module.hello` provides `my.javacraft.modules.hello.HelloService`
- `module.util` exports `my.javacraft.modules.util` to `module.app` only

## Service loader example (`uses` / `provides`)

`module.app/src/main/java/module-info.java`:

```java
module module.app {
    requires transitive module.util;
    requires transitive module.hello;
    uses my.javacraft.modules.hello.HelloService;
}
```

`module.hello/src/main/java/module-info.java`:

```java
module module.hello {
    exports my.javacraft.modules.hello;
    provides my.javacraft.modules.hello.HelloService
            with my.javacraft.modules.hello.impl.HelloServiceImpl;
}
```

`module.app/src/main/java/my/javacraft/modules/app/App.java` uses:

```java
ServiceLoader.load(HelloService.class).findFirst()
```

##  JPMS-focused tests

Tests are in `module-app` and validate:

- `uses` declaration in `module.app`
- `provides` declaration in `module.hello`
- qualified export from `module.util` to `module.app`
- hidden `my.javacraft.modules.util.secret` package
- end-to-end module-path execution with `ServiceLoader`

## Build and test

```bash
mvn -pl modules/module-app -am test
```

## Run modular app manually

```bash
mvn -pl modules/module-app -am package
java --module-path modules/module-util/target/module-util-1.0-SNAPSHOT.jar:modules/module-hello/target/module-hello-1.0-SNAPSHOT.jar:modules/module-app/target/module-app-1.0-SNAPSHOT.jar --module module.app/my.javacraft.modules.app.App
```
