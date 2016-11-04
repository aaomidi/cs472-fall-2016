package com.aaomidi.ftpserver.engine;

import java.util.regex.Pattern;

public class FTPRegex {
    public static Pattern ACTIVE_PORT = Pattern.compile("^\\d+.*(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+).*", Pattern.DOTALL);
    public static Pattern EACTIVE_PORT = Pattern.compile("\\|(\\d*)\\|(.*)\\|(\\d*)\\|", Pattern.DOTALL);
}
