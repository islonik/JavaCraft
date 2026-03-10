# standard-client

## Module purpose

`standard-client` contains the socket clients used by the Standard Echo example.  
It provides two client implementations:

- `PlatformThreadClient` - uses a platform daemon thread for the response listener.
- `VirtualThreadClient` - uses a virtual thread for the response listener.

Both clients share the same core behavior through `UserClient`.

## How it is structured

- `my.javacraft.echo.standard.client.common.UserClient`
  - Common connection lifecycle and protocol behavior.
  - Connects with explicit timeout (`CONNECT_TIMEOUT_MILLIS`).
  - Uses UTF-8 for input/output streams.
  - Sends messages to server and reads responses via a bounded queue (`MAX_QUEUED_RESPONSES`).
  - Tracks closure state with `closedByClient` and `closedByServer`.
  - Provides interactive console loop in `readUserMessages(...)`.
- `my.javacraft.echo.standard.client.platform.PlatformThreadClient`
  - Starts response listener on a platform daemon thread.
- `my.javacraft.echo.standard.client.virtual.VirtualThreadClient`
  - Starts response listener on a virtual thread.
- `my.javacraft.echo.standard.client.common.PortValidator`
  - Validates CLI port (`1..65535`) with default fallback (`8075`).
- `PlatformClientApplication` / `VirtualClientApplication`
  - Runnable entry points for manual interactive usage.

## Protocol behavior

The client sends one text line per message and expects one response line:

- Send any text -> receive `Did you say '...'?`
- Send `stats` -> receive `Simultaneously connected clients: N`
- Send empty line -> receive `Please type something.`
- Send `bye` -> receive `Have a good day!` and then socket closes from server side.

Important details:

- User input is sent as entered (including meaningful surrounding spaces).
- Reads and writes are UTF-8.
- `sendMessage(...)` fails fast when connection is unavailable.
- Queue overflow closes the client to avoid unbounded memory growth.

## Build and test

Run tests only for this module:

```bash
mvn -pl echo/standard/standard-client test
```

## Run manually

1. Start a server (`standard-server`) on a port, for example `8075`.
2. Run one of these main classes from your IDE:
   - `my.javacraft.echo.standard.client.platform.PlatformClientApplication`
   - `my.javacraft.echo.standard.client.virtual.VirtualClientApplication`
3. Optionally pass the port as first argument (default is `8075`).
4. Type messages in the console and finish with `bye` (or EOF).
