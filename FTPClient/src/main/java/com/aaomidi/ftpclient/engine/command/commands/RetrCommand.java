package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;

import java.util.List;

public class RetrCommand extends FTPCommand {
    public RetrCommand(FTPClient client) {
        super(
                client,
                "Retrieve",
                "Retrieves a file.",
                "retr", "download", "get", "iwant", "plzgiveme", "feedme"
        );
    }

    @Override
    public void execute(String cmd, List<String> args) {

    }
}
