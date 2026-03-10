# single-client

## Module purpose

`single-client` contains the non-blocking client side of the Single Echo example.
It connects to the selector-based single-thread server and exchanges line-delimited UTF-8 messages.

## How it is structured

- `my.javacraft.echo.single.client.SingleClient`
  - Public client API: `connectToServer()`, `sendMessage(...)`, `readMessage()`, `close()`, `run()`.
  - Starts one listener worker (`SingleMessageListener`) on a single-thread executor.
  - Interactive loop reads stdin until `bye` or EOF.
- `my.javacraft.echo.single.client.SingleNetworkManager`
  - Owns `SocketChannel` and `Selector`.
  - Uses non-blocking connect with timeout and `wait/notify` handoff for sender/listener.
  - Stores incoming responses in a bounded queue.
- `my.javacraft.echo.single.client.SingleMessageSender`
  - Frames outgoing commands with `\r\n`.
  - Uses `SelectionKey`/`Selector` write interest and a pending-write queue.
- `my.javacraft.echo.single.client.SingleMessageListener`
  - Selector-driven reader for complete `\r\n` framed responses.
  - Preserves meaningful payload whitespace and protects against oversized frames.
- `my.javacraft.echo.single.client.SingleClientApplication`
  - CLI entry point for manual usage.

## Protocol behavior

- Send any text -> `Did you say '...'?`
- Send `stats` (case-insensitive) -> `Simultaneously connected clients: N`
- Send empty message -> `Please type something.`
- Send `bye` (case-insensitive) -> `Have a good day!` and connection closes.

## Build and test

Run tests for this module:

```bash
mvn -pl echo/single/single-client test
```

## Run manually

1. Start `single-server` first (for example on port `8077`).
2. Run main class:
   - `my.javacraft.echo.single.client.SingleClientApplication`
3. Optional first argument: port number (`0..65535`, default `8077`).
