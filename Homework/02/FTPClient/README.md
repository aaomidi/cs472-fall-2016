# CS472
## Homework: 2 - FTP Client
## Amir Omidi

### Description:
In this project I was tasked with writing a FTP Client using a programming language. For this I used RFC 959 and RFC 2428 to learn how the client works and implement it into my program. I used the Java programming language since it handles sockets quite cleanly and the use of exceptions makes error handling much easier.

### Command line processing and connection with server:
The main static method retrives the `hostname` `logfile` and `port (optional)` from the user and cretates network sockets to the host and port provided.

### PDU Delineation:
All responses(except the first connecting one) is as follows:

`statuscode written message`

We can easily seperate the `statuscode` from the message using regular expressions.

### Commands and responses:
All commands required from us are implemented. These commands are:

- `USER`
- `PASS`
- `CWD`
- `CDUP`
- `QUIT`
- `PASV`
- `EPSV`
- `PORT`
- `EPRT`
- `RETR`
- `PWD`
- `LIST`
- `HELP`

### Error handling:
Most of the errors are handled using the `lang` package.

### Sample run:
The sample run is contained in `log.log`.

### Questions:
The questions are located in `questions.md`

### How to run:
This program uses Maven. Simply type in `mvn clean package`. Go to the jar folder it created and do `java -jar FTPClient.jar hostname logfile port`.

If you try to run this in an IDE, please make sure you have the lombok plugin for your IDE or errors are going to show up everywhere!