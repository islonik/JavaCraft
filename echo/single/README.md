# Non-blocking I/O

Demonstrates how to write a simple client/server application using just a single thread in Java.

Non-blocking I/O allows you to use a single thread to handle multiple concurrent connections. 

* In NIO based systems, instead of writing data onto output streams and reading data from input streams, we read and write data from “buffers”. You can think of the “buffer” as a temporary storage place and there are different types of Java NIO buffer classes (eg:- ByteBuffer , CharBuffer , ShortBuffer etc..) available for us to use, even though most network programs use ByteBuffer exclusively.
* “Channel” is the medium that transports bulk of data into and out of buffers and it can be viewed as an endpoint for communication. (For example if we take “SocketChannel” class, it reads from and writes to TCP sockets. But the data must be encoded in ByteBuffer objects for reading and writing.)
* Then we need to understand a concept called “Readiness Selection” which basically means “the ability to choose a socket that will not block when data is read or written”.
  
Java NIO has a class called “Selector” that allows a single thread to examine I/O events on multiple channels. That is, this selector can check the readiness of a channel for operations, such as reading and writing. Now remember different channels can be registered with a “Selector” object and you can specify which operations you are interested in observing and a another thing to remember is that each of these channels are assigned a separate “SelectionKey” which serve as a pointer to a channel.