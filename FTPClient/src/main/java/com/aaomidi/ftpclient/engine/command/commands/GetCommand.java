package com.aaomidi.ftpclient.engine.command.commands;

import com.aaomidi.ftpclient.engine.FTPClient;
import com.aaomidi.ftpclient.engine.command.FTPCommand;
import com.aaomidi.ftpclient.engine.lang.StatusCodes;
import com.aaomidi.ftpclient.engine.lang.Type;
import com.aaomidi.ftpclient.util.Log;

import java.util.List;
import java.util.logging.Level;

public class GetCommand extends FTPCommand {
    public GetCommand(FTPClient client) {
        super(
                client,
                "get",
                "Retrieves a file.",
                "retr", "download", "iwant", "plzgiveme", "feedme"
        );
    }

    @Override
    public void execute(String cmd, List<String> args) {
        client.getDataLock().lock();
        if (args.size() == 0) {
            Log.log(Level.INFO, Type.LOCAL, "Please specify the file you want to get.");
            client.getDataLock().unlock();
            return;
        }

        client.setFileTransfer(true);
        client.setFileName(args.get(0));
        GetStatus status = GetStatus.ERROR;
        List<String> output = null;
        try {
            client.prepareConnection();
            client.writeControl(String.format("RETR %s", args.get(0)));
            output = client.getOutput();
            status = manageOutput(output.get(0));

        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (status) {
            case ERROR:
                Log.log(Level.INFO, Type.LOCAL, "An unidentified error occurred. Here is the response from the server:");
                client.printOutput(output, Level.INFO, Type.LOCAL);
                break;
            case SUCCESS:
                Log.log(Level.INFO, Type.LOCAL, "Transfer was successful.");
                break;
        }
        client.getDataLock().unlock();
    }

    private GetStatus manageOutput(String line) {
        if (line == null) {
            //print error
            return GetStatus.ERROR;
        }

        int statusCode = StatusCodes.getStatusCodeFromString(line);

        if (statusCode == 550) {
            Log.log(Level.INFO, Type.LOCAL, "File not found!");
            return GetStatus.FILE_NOT_FOUND;
        }

        if (statusCode != 226) {
            return GetStatus.ERROR;
        }

        return GetStatus.SUCCESS;
    }

    private enum GetStatus {
        FILE_NOT_FOUND,
        ERROR,
        SUCCESS;
    }
}
