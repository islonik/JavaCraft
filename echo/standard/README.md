# Socket server

Socket server using <b>blocking I/O</b> and demonstrates how to write a simple client/server application in Java.

Every connection/client creates a new thread.

By definition, a <u>socket</u> is one endpoint of a two-way communication link between two programs running on different computers on a network. A socket is bound to a port number so that the transport layer can identify the application that data is destined to be sent to.

With blocking I/O, when a client makes a request to connect with the server, the thread that handles that connection is blocked until there is some data to read, or the data is fully written. Until the relevant operation is complete that thread can do nothing else but wait. Now to fulfill concurrent requests with this approach we need to have multiple threads, that is we need to allocate a new thread for each client connection.

