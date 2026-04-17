# Virtual File Server
<sub>[Back to JavaCraft](../README.md#micro-java-samples)</sub>

`vfs` is a multi-module Java project that exposes a shared in-memory file tree over TCP.
Multiple terminal clients can connect to one server, navigate the same directory structure,
create and manage nodes, and coordinate access with per-node locks.

The system is intentionally simple:

- single server process
- in-memory state only
- no persistence across restarts
- session-based locking, not distributed consensus

**Modules:** `vfs-core`, `vfs-client`, `vfs-server`  
**Stack:** Java 25, Spring Boot, Netty, Protocol Buffers

For the deeper runtime design, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Contents
1. [Quick Start](#1-quick-start)
2. [Module Layout](#2-module-layout)
3. [Protocol](#3-protocol)
4. [Commands](#4-commands)
5. [File System And Locking](#5-file-system-and-locking)
6. [Configuration](#6-configuration)
7. [Local Operations](#7-local-operations)
8. [Tests](#8-tests)

## 1. Quick Start
<sub>[Back to top](#virtual-file-server)</sub>

### Build

From the repo root:

```bash
mvn -f vfs/pom.xml package
```

From the module directory:

```bash
cd vfs
mvn package
```

### Run the server

```bash
java -jar vfs/server/target/VFS.jar
```

Default address: `localhost:4499`

### Run the client

```bash
java -jar vfs/client/target/VFS-client.jar
```

### Connect

The client command parser accepts both forms:

```text
connect localhost:4499 alice
connect localhost 4499 alice
```

### Example session

```text
connect localhost:4499 alice
mkdir docs
cd docs
mkfile notes.txt
print
whoami
quit
exit
```

## 2. Module Layout
<sub>[Back to top](#virtual-file-server)</sub>

| Module | Responsibility |
| --- | --- |
| `vfs-core` | shared protocol, command parsing, request/response factories, shared exceptions/utilities |
| `vfs-client` | interactive CLI client, Netty client bootstrap, response handling |
| `vfs-server` | Spring Boot server, command execution, in-memory tree, locking, session timeout |

Key code paths:

- `vfs-core/src/main/resources/protocol.proto` defines the wire protocol
- `vfs-core` generates `Protocol.java` during Maven `generate-sources`
- `vfs-client` provides the REPL and Netty client pipeline
- `vfs-server` hosts the shared tree and all command implementations

## 3. Protocol
<sub>[Back to top](#virtual-file-server)</sub>

Communication uses Protocol Buffers with Netty varint framing.
The schema lives in [vfs/core/src/main/resources/protocol.proto](core/src/main/resources/protocol.proto).

### Messages

#### `User`

| Field | Type | Meaning |
| --- | --- | --- |
| `id` | `string` | session id |
| `login` | `string` | user login |

#### `Request`

| Field | Type | Meaning |
| --- | --- | --- |
| `user` | `User` | caller identity |
| `command` | `string` | raw command text |

#### `Response`

| Field | Type | Meaning |
| --- | --- | --- |
| `code` | `ResponseType` | result status |
| `message` | `string` | human-readable output |
| `specificCode` | `string` | optional extra value; used for the assigned session id on connect |

#### `ResponseType`

| Value | Meaning |
| --- | --- |
| `OK` | command succeeded |
| `FAIL` | command failed |
| `SUCCESS_CONNECT` | login accepted |
| `FAIL_CONNECT` | login rejected |
| `SUCCESS_QUIT` | session closed |
| `FAIL_QUIT` | session close failed |

### Connect handshake

On the first request, the client sends a placeholder user id (`0`).
If login succeeds:

- `specificCode` contains the real session UUID
- `message` contains the current working directory path

## 4. Commands
<sub>[Back to top](#virtual-file-server)</sub>

### Client-local commands

| Command | Syntax | Description |
| --- | --- | --- |
| `connect` | `connect <host>:<port> <login>` | open a socket and request a server session |
| `connect` | `connect <host> <port> <login>` | equivalent space-separated form |
| `exit` | `exit` | close the client process; if connected, sends `quit` first |

### Server-backed commands

| Command | Syntax | Description |
| --- | --- | --- |
| `quit` | `quit` | disconnect from the server and remove the user home directory |
| `cd` | `cd <directory>` | change current working directory |
| `mkdir` | `mkdir <directory>` | create a directory |
| `mkfile` | `mkfile <file>` | create a file |
| `rm` | `rm <node>` | remove a node |
| `rename` | `rename <node> <name>` | rename a node |
| `copy` | `copy <node> <directory>` | deep-copy a node into a directory |
| `move` | `move <node> <directory>` | move a node into a directory |
| `print` | `print` | print the tree starting from the current directory |
| `lock` | `lock [-r] <node>` | lock one node or a subtree |
| `unlock` | `unlock [-r] <node>` | unlock one node or a subtree |
| `whoami` | `whoami` | print the current login |
| `help` | `help` | print command help |

## 5. File System And Locking
<sub>[Back to top](#virtual-file-server)</sub>

### File tree

The tree exists only in server memory.
At startup the server creates:

```text
/
└── home
```

On successful connect, the server creates:

```text
/home/<login>
```

On `quit` or timeout, that home directory is removed.

### Path rules

- `/` is the root
- paths are relative to the current working directory unless they start with `/`
- `.` resolves to the current node
- `..` resolves to the parent node
- duplicate child names under the same parent are rejected

### Node model

Each node stores:

- `name`
- `type` as `DIR` or `FILE`
- `parent`
- ordered child list

`print` sorts directories and files before rendering the tree.

### Locking

Locks are tracked in memory by `LockService` with a `ConcurrentHashMap<Node, NodeLock>`.

- locks are owned by the session user
- `-r` applies the operation recursively
- a restart clears all locks
- mutating commands consult the lock state before changing the tree

## 6. Configuration
<sub>[Back to top](#virtual-file-server)</sub>

Server configuration lives in [vfs/server/src/main/resources/application.yaml](server/src/main/resources/application.yaml).

| Key | Default | Meaning |
| --- | --- | --- |
| `delimiter` | `/` | path separator |
| `server.name` | `localhost` | bind address |
| `server.port` | `4499` | TCP port |
| `server.pool` | `100` | configured network pool size |
| `server.timeout` | `10` | idle timeout in minutes for logged-in sessions |

`TimeoutJob` also removes empty pre-login sessions after one minute.

## 7. Local Operations
<sub>[Back to top](#virtual-file-server)</sub>

### Find and stop a running server

```bash
lsof -i tcp:4499
kill -9 <PID>
```

## 8. Tests
<sub>[Back to top](#virtual-file-server)</sub>

Run the full VFS module test suite:

```bash
mvn -f vfs/pom.xml test
```

The test suite covers:

- core command parsing and protocol factories
- client command flow and Netty client helpers
- server node management, locking, timeout, and session behavior
