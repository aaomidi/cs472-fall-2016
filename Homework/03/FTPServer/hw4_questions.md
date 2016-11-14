# CS472

## Homework: 4 questions

### Question 1:

    Why is logging an important part of security?

So first, let me mention that software should be made as secure as possible. IT security have a responsibility to make attacking a system practically impossible or at least very hard.

Not all security measures are effective and sometimes bad apples get through your thousands of golden gates of security. It could be something very simple, or something out-of-this-world complex. How can you know the complexity of the attack if you were not around the server when the attack took place?

Logs allow you to take a look at the attack after it is done and figure out what went wrong, what data was stolen/corrupted, what can you do in the future to prevent this attack and how to handle telling your customers about the attack.

I should mention that this does not just apply to networking, everything dealing with computers should have logs and different levels of logging involved in it.


### Question 2:

    Do you see any problems with concurrent servers and log files? Brainstorm how to solve this problem.

Yes. If you treat files as you would on a single threaded program, in a multi thread program you will hit a lot of race conditions and concurrent modification issues very fast.

There are multiple ways to solve it, the method I used was to create a list of stuff to be printed to the file and a worker that took care of it.

A concurrent list takes a bunch of strings from any thread, puts it inside a concurrent list and a worker that is running on a timer takes each string and writes it into a file.

Another method is to use locks and each thread that wants to log to a file requests a lock, writes to it and then unlocks it.

One more thing to keep in mind is to distinguish between each "connection". In other words, we need to figure out what log message is related to what connection. What I did for this was when printing the logs, I printed the thread ID associated with that log.


### Question 3:

    What are the security coinsidersations with port_mode? With pasv_mode? Why would I want one or the other? Think of the conversation between the client and server?
    
The first issue that comes to mind with port mode is using an FTP server as a DDoS system. You could request an FTP server to connect to a specific IP which sends data to that IP. If a person abuses a lot of FTP servers like this, they can use it as an amplification attack and cause havoc on the internet.

This issue is related to the FTP bounce attack. This attack can be used for other purposes such as discreet port mapping.

In terms of issues for pasv_mode, port stealing comes to mind. Port stealing is effectively a method of DoS where the attacker takes a service offline by the means of using up all the ports on the system to the point that it cant take more users.

I think anyone writing or running an FTP server should always have PORT mode disabled. PORT mode exposes a larger attack vector for an attacker. It allows people who aren't authenticated to abuse the server and create havoc. The vulnerability that comes with pasv_mode is only possible if either the server is in anonymous mode or it is used by "hackers".

You might want to use passive mode if you're going to be dealing with clients behind a NAT firewall. If you do use passive mode, it is best to limit the number of active ports at a time, or add a range of possible ports, since each open port adds another attack vector to the server.

Active mode has been deprecated entirely and should NOT be used for any case.

### Question 4:

    What are the different issues with securing the connections with IMPLICIT mode and EXPLICIT mode? What are the "it depends" part of the tradeoffs?
    
Implicit mode ensures the server will have an encrypted connection throughout its process. It ensures that no data will go to the server without being encrypted first. However it is not backwards compatible. The socket will assume all data is encrypted and if a SSL/TLS 

Explicit mode, the version recommended by RFCs, is backwards compatible. However it does not ensure that the data will always be encrypted.

In explicit mode, the client has the power to decide what parts of the connection need to be encrypted and what parts can continue in plain text.

Explicit mode is better because it gives you control over when you want to use encryption since in some scenarios encryption is not useful. For example, if the FTP is being established over an encrypted network such as a VPN or SSH tunneling, adding another layer of encryption is not useful.

Explicit mode is the mode that is suggested to use. It is backwards compatible and gives more power to the client to decide how they want to encrypt the data.

### Question 5:

    Why is the 3 person method of FTP (as originally defined in the RFC) really insecure? Think of what you could do to cause trouble with the approach and what you can do in your clients and servers to stop that from happening. Do you have to do any checking in your program(s) with PORT/PASV to make sure that it isn’t happening (that YOU ARE talking to the same host)? Think about the data channel as well as the control channel. 
    
The three person FTP was very great at the time since it allowed people who had dial-up internet be able to transfer very large files between hosts without having to worry about their own data connection.

Unfortunately, the original RFC did not consider what downsides this design has. The three person method of FTP basically created a completely open port where anyone could write data to and potentially wreck havoc on the system. This method also created a doorway to FTP amplification attacks where a host just sent a ton of data to another host regardless if that host was actually the intended host to receive the data.

What I have done to mitigate this issue is just ignore the first four arguments in the PORT command and take the port and create a socket to the address connected at the control port and use only the port arguments as valuable data.

Other popular FTP clients have took a different approach, some have left it up to the config. Some send a unsupported error back to the client. And some, like mine, just ignore the first four arguments.

### Question 6:

What I have done is log every IP and port attempting to connect to the server. This will allow an administrator to figure out if some user is trying to attack by checking the times a specific IP was denied access due to incorrect passwords.

Another system we can use is if the user uses a password incorrectly 3 times in a row, it will close the connection with them and add them to a black list for 5 minutes.

I will implement the blacklisting feature into the code to decrease the speed of a person trying to attack it.


### Extra Credit Question

FTP and SFTP are usually, and incorrectly, grouped as the same protocol. SFTP stands for secure/SSH file transfer protocol. It uses the SSH protocol to create a secure confection using the SSH/SSH2 protocol.

SFTP itself does not provide security and the security comes from the secure shell (SSH) protocol that is the base for it. SSH uses public-key cryptography to authenticate and encrypt the communication. This method, unlike the FTPS, does not need to have signed certificates from certificate providers such as Comodo or LetsEncrypt. The servers sends a fingerprint on the first connection and the client has to save that finger print. In all future connections the server must be able to provide the same fingerprint, if the finger print has changed then the client can know something is terribly wrong.

SFTP does not have a specific channel for data and connection. This is much better since it makes the attack vector much smaller compared to FTP which has two different connections for control and file transfer. Having one connection also ensures that the user has to be authenticated before they can send or request any messages.

BitTorrent is a completely different type of protocol. BitTorrent is a peer to peer transfer protocol and usually does not involve any kind of authentication, we'll get into where authentication can take place later.

BitTorrent works by peers requesting a list of people who have a specific file from a tracker. A tracker is just what it sounds like, it tracks who has what file and responds to people asking for that file. Some torrent providers can lock down these trackers with a special code as an argument where that code is used to authenticate the user.

BitTorrent trackers are not encrypted by default, however SSL/TLS can be used to encrypt it and not expose the tracker password in plain text.

BitTorrent can also operate without trackers. BitTorrents can have a decentralized tracker, called a decentralized hash table(DHT). When a user is looking for a person, it can look for DHTs and ask other peers for their copy of their DHT and create a completely decentralized, peer based, lookup table where the client can find who has what file.

BitTorrent's file transfer can be encrypted but it is not encrypted by default. It is a very effective file transfer mechanism for illegal content since there is no specific server who you can send a C&D or DMCA takedown request to get your file off the internet. BitTorrent can also run over both TCP and UDP which means it can be much faster in transferring larger files since it could use the network as strongly as it needs.

In conclusion, there is no holy grail of transfer protocols. Each protocol has its pros and cons and as a system designer and engineer, it is your job to determine the needs of your system and implement a protocol or even create your own protocol to solve your problem and be the best for your needs.

I will provide some scenarios and explain which one to use and why:

1. You're hosting a single player game on your server and you want to make sure your server can handle the data when you publish an update. 

    `BitTorrent in this case would be very effective since it is self scaling and will ensure everyone gets the update with the fastest speed possible.`
     
2. You've created a system to serve confidential files to specific people.
    
   `SFTP would be your best choice here. Private-Public key authentication is much stronger than using passwords and your server fingerprint, if not ignored, would provide protection against any MITM attack.`
   
3. You have very slow internet and want to transfer files between two servers.

    `FTP would be your best choice here. FTP can create a connection between two servers using your system as the control center.`
    
4. You want to show the world, live, how awesomely you play the Ice Satan (Mei) in Overwatch?
   
    `None of these protocols would be suitable for this and you should look for another protocol or write your own.`
 