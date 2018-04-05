Chat server and client in Java using non-blocking sockets.

Code organization
=================
Server-client connection is implementing using non-blocking TCP sockets from `java.nio`: `ServerSocketChannel` and `SocketChannel`.
It allows the server to handle many connections in a single thread, which in theory should be an efficient 
approach. The class `Connection` implements IO logic for non-blocking sockets.

The class `Server` implements a server IO loop. It runs `ConnectionAcceptor` in a single thread and 
`ConnectionProcessor` in several threads (configurable and equal to the number of CPU cores by default). Server logic
 is injected by an object implementing `ServerLogic` interface. 

Chat server logic is implemented in `ChatLogic`. It uses `Message` objects to represent different kinds of messages. 
The method `getBytes()` does serialization of message to bytes so it can be transferred over a socket. The 
messages are read and constructed from byte buffers using `nextMessage(ByteBuffer buffer)` static method. Also there 
is `Command` class which represen commands to the server passed from a user from a chat message. It is organized in a
 similar manner as `Message` objects.
    
`Client` is configurable with `InputSystem` and `DisplaySystem`, which in theory should allow to use `Client` in 
different GUI settings or use it for a chat bot.

Building and running
====================
Maven build system is used, so you can execute different maven goals as `mvn goal`. To start a demo server on 
localhost:5000 and clients execute the following in the root of this repository:
```
mvn package
java -cp target/chat-0.1-jar-with-dependencies.jar nmayorov/app/StartDemoServer
# And in another shell
java -cp target/chat-0.1-jar-with-dependencies.jar nmayorov/app/StartDemoClient
# And another client in another shell
java -cp target/chat-0.1-jar-with-dependencies.jar nmayorov/app/StartDemoClient
... 
```

To start a server and connect a bunch of bots to it:
```
mvn package
java -cp target/chat-0.1-jar-with-dependencies.jar nmayorov/app/StartDemoServer
# In another shell
java -cp target/chat-0.1-jar-with-dependencies.jar nmayorov/app/StartLoadTest [number of bots]
```
If `[number of bots]` is not provided, 100 will be used.