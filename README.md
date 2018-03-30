Chat server and client in Java using non-blocking sockets.

Code organization
=================
Server-client connection is implementing using non-blocking TCP sockets from `java.nio`: `ServerSocketChannel` and `SocketChannel`.
It allows the server to handle multiple connections in a single thread, which in theory should be an efficient 
approach. The class `Connection` implements IO logic for non-blocking sockets.

The logic is organized as `Server` and `Client` passing `Message` objects between each other. The `Message` class
represents a certain kind of message (text message from a user, text message from the server, authorization request, etc.), 
they are collected in `message` sub-package. 

The `Message` has the following methods:
1. `getBytes()` serializes a message to a byte array so it can be transferred over a socket.
2. `getText()` provides some human-readable representation for logging or simple output.
3. `handleServerReceive(Server server, Connection connection)` and`handleClientReceive(Client client, Connection 
connection)` implement server and client logic when receiving this message.

The last two methods are designed to allow introducing new messages and corresponding logic without the need to 
modify `Server` and `Client` code, assuming that `Server` and `Client` have a stable and sufficient API. 
Obviously this is a somewhat controversial design, because server logic is spread across `Server` and `Message` 
classes and `Server` needs to provide some sort of "API" to work with messages (the same goes for the client). 
However, an alternative design when everything is processed inside `Server` and `Client` seems to violate the 
open/closed principle and forces to write code with switches and reflection to handle different messages. Maybe there 
are better approaches I couldn't come up with.

`Message` objects can be read from byte buffers using `Message.getNext(ByteBuffer buffer)` static method. There is a 
single `getBytes` implementation and a single `getNext` implementation, because a co-existence of different 
serialization/deserialization protocols seems unnecessary.

The subpackage `command` contains handlers of special user text messages, which should be considered as "commands". 
These are very similar to messages, however they are separated from them because I figured that the server should be 
responsible for distinguishing commands in user text messages and thus commands (in this sense) can't be implemented
as messages. In other words, a command is something that can be contained in `ClientText` message.

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

To start a server and connect 1000 chat bots to it:
```
mvn package
java -cp target/chat-0.1-jar-with-dependencies.jar nmayorov/app/StartDemoServer
# In another shell
java -cp target/chat-0.1-jar-with-dependencies.jar nmayorov/app/StartLoadTest
```