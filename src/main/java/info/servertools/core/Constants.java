package info.servertools.core;

import java.nio.charset.Charset;

public final class Constants {

    public static final String MOD_ID = "ServerTools-CORE";
    public static final String MOD_NAME = MOD_ID;

    public static final String MC_VERSION = "1.8";
    public static final String DEPENDENCIES = "required-after:Forge@[11.14.4,)";

    public static final String CHARSET_NAME = "UTF-8";
    public static final Charset CHARSET = Charset.forName(CHARSET_NAME);

    private Constants() {}
}
