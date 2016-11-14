# CS472

## Homework: 4 - Homework 3 on steroids

## Amir Omidi

### Description:

In this project we had to:

1. Take homework 3.
2. Feed it steroids.
3. Submit it on bblearn.

Jokes aside, we were supposed to make the FTP server a little more production friendly.

We added a server configuration file, auth file, allowing restriction of PORT or PASV commands and, securing the connection with SSL/TLS.

### Compilation:

Compilation is the same as before:

`mvn clean package`

Running the program however, is different.

There is a `keystore.jks` file inside the `certificates` directory.
You would need to move that to the jar file after you have compiled and run the following command:

`java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password -jar FTPServer.jar LOGFILENAME NORMAL_PORT SECURE_PORT CONFIG_FILE.json`

For example:

`java -Djavax.net.ssl.keyStore=keystore.jks -Djavax.net.ssl.keyStorePassword=password -jar FTPServer.jar logfile 45141 1441 config.json`

### Video

The following video explains the program a little more in depth and can provide a larger insight than just this readme file.

https://www.youtube.com/watch?v=gYbz6hei_EQ