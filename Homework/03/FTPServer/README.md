# CS472
## Homework: 3 - FTP Server
## Amir Omidi

### Description:
In this project I was tasked with writing a FTP Server using a programming language. For this I used RFC 959 and RFC 2428 to learn how the client works and implement it into my program. I used the Java programming language since it handles sockets quite cleanly and the use of exceptions makes error handling much easier.

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
- `NOOP`

### Sample run:
The sample run is contained in `server.log`.

### Questions:
The questions are located in `questions.md`

### How to compile and run:
This program uses Maven. Simply type in `mvn clean package`. Go to the jar folder it created and do `java -jar FTPServer.jar logpath port [configPath(optional)]`.

If you try to run this in an IDE, please make sure you have the lombok plugin for your IDE or errors are going to show up everywhere!