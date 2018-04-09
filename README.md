Chat server and client in Java using non-blocking sockets.

Code organization
=================
Network layer is defined through `Server`, `Connection` and `ConnectionEvent`  classes. The `Server` is assumed to 
put new connections and events related to connections to queues, which will be read and processed by another 
threads (server logic classes). The provided implementation relies on `java.nio`. 

The chat logic is implemented in `ConnectionAcceptor` and `ConnectionProcessor` classes which process the aforementioned 
queues. The class `Chat` is responsible for creating the queues and binding them to server and processor classes. 

Chat logic is implementing using `Message` and `Command` objects. `Message` is used to represent different kinds of 
messages. The method `getBytes()` does serialization of message to bytes so it can be written to a connection. The 
messages are read and constructed from bytes using `ChatMessageBuffer` and `MessageFactory` classes. The class 
`Command` represents commands to the server passed from a user from a chat message. It is organized in a similar 
manner as `Message` objects.
    
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