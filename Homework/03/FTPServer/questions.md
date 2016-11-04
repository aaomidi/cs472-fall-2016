# CS472
## Homework: 3 questions

### Question 1:

FTP is not a secure protocol on its own. It's not the best client for the purpose of security. BitTorrent isn't secure either but you can encrypt the transfer of files inside the BitTorrent protocol. SFTP is a much better protocol since it is based on SSH which can be heavily encrypted. These security issues aren't very related to their implementations, it is just the way the protocol was designed. However you can secure FTP using the SSL/TLS sublayer.

### Question 2:

It would be wrong to assume this FTP server is not hackable. One reason why it would be hackable is that I do not have much experience with sockets and networking and I could've overlooked very important security features.

One method that I know I can be attack is output analysis. A better way to verify a password is to do it byte per byte so a hacker can't figure out which combination of letters are correct by the time it takes the server to respond.

### Question 3:

Disabling anonymous FTP will stop random people from putting in corrupt data to the server. Another method is to be able to call another program using a config file to check a file on arrival to the server. Another method is to just not execute the file when it is received. A non executed virus can not do anything.

Another thing that is important is to run the FTP server without root/sudo permissions. The only reason to make FTP run with admin privileges is for port 21 to be assigned to it, however this goal could be achieved by just using IP Tables and forwarding.

Anyway the files are transferred in plain text, so anyone listening to the connection can get these files and read whats in them.
