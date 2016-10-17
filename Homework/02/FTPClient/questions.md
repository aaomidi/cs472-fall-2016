# CS472
## Homework: 2 questions

### Question 1:
    Think about the conversation of FTP – how does each side validate the other (on the connection and data ports – think of each separately)? How do they trust that they’re getting a connection from the right person?

The server associates any specific data channel with the user that requested it. Once it's done with that single operation of the data channel, the server deconstructs and closes it. This way the server could implement state based security that if another user connects to a specific data port (IP addresses) they could block it off.

On the client side of things, the client has requested the IP and PORT from the server. If the client trusts the server (which is a whole other topic) then the client should trust the response from the server.

The above is true for passive mode. For active mode, it is the same issue, just the other way around. The client should keep track of who it's given access to the data port to, and if its not that server. It should block the connection.

### Question 2:
    Think about the conversation as a 1-1 client/server connection – does it scale to one to many or not? 

The reason FTP was seperated into control and data sockets was that a client could connect two FTP servers to eachother and transfer files between them without actually having to use their data connection. Thus you could say it can scale from 1 to 2, but not 1 to many.

### Question 3:
    How does your client know that it’s sending the right commands in the right order? How does it know the sender is trustworthy? 

There are two ways:
- The server is a FSM. If a command comes in that is out of place it'll respond back with an error.
- The client could have an internal `state` and make sure the commands being sent are in their proper order per the RFC.

The trust worthiness of the actual connection can't be 100% determined, unless certificates and other stuff are involved. But with authentication the server should know who the client is. On the client side, since it's the client connecting to the server, they have some trust of the server.


