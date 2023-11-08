# Implementing Non-blocking I/O with Netty

Netty is a non-blocking framework and this project demonstrates how to write a simple NIO client/server application using Netty.

## Core concepts

### Channel
<b>Channel</b> is the base of Java NIO. It represents an open connection which is capable of IO operations such as reading and writing.

Every IO operation on a Channel in Netty is non-blocking.

### Future

There is a Future interface in the standard Java library, but it’s not convenient for Netty purposes — we can only ask the Future about the completion of the operation or to block the current thread until the operation is done.

That’s why Netty has its own ChannelFuture interface. We can pass a callback to ChannelFuture which will be called upon operation completion.

### Events and Handlers

Netty uses an event-driven application paradigm, so the pipeline of the data processing is a chain of events going through handlers. Events and handlers can be related to the inbound and outbound data flow. Inbound events can be the following:

* Channel activation and deactivation
* Read operation events
* Exception events
* User events

See more in [NettyServerHandler](netty-server/src/main/java/my/javacraft/echo/netty/server/NettyServerHandler.java)

### Encoders and Decoders

As we work with the network protocol, we need to perform data serialization and deserialization. For this purpose, Netty introduces special extensions of the ChannelInboundHandler for decoders which are capable of decoding incoming data. The base class of most decoders is ByteToMessageDecoder.

For encoding outgoing data, Netty has extensions of the ChannelOutboundHandler called encoders. MessageToByteEncoder is the base for most encoder implementations. We can convert the message from byte sequence to Java object and vice versa with encoders and decoders.
