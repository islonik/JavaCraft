# Micro java samples.

* [algs](algs/README.md) - Algorithms
* [blueprints](blueprints/README.md) - Architectural blueprints
* [echo](echo/README.md) - Different Server types
* [mathparser](mathparser/README.md) - Complex Math parser with GUI
* [microservices](microservices/README.md) - Microservices
* [SES](ses/README.md) - Simple Event System
* [tic-tac-toe](tic-tac-toe/README.md) - Tic-tac-toe game with GUI
* [vfs](vfs/README.md) - Virtual File Server
* [xlspaceship](xlspaceship/README.md) - Battleship game

## Dependency management

### Overview of dependencies

```bash
mvn dependency:tree
```

### To find unused dependencies
```bash
mvn dependency:analyze
```

### To see new dependencies without snapshots, prereleases, or major upgrades
```bash
mvn versions:display-dependency-updates -DprocessDependencyManagement=false -DprocessDependencyManagementTransitive=false
```

### To see new dependencies including major upgrades
```bash
mvn versions:display-dependency-updates -DallowMajorUpdates=true -DprocessDependencyManagement=false -DprocessDependencyManagementTransitive=false
```
