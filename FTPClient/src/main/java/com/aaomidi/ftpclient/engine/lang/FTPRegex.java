package com.aaomidi.ftpclient.engine.lang;

import java.util.regex.Pattern;

public class FTPRegex {
    public static Pattern RESPONSE_CODE = Pattern.compile("^(\\d+).*", Pattern.DOTALL);
    public static Pattern PASSIVE_PORT = Pattern.compile("^\\d+.*\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)\\).*", Pattern.DOTALL);
}
