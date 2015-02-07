package info.servertools.core;

import lombok.Getter;

public final class STVersion {

    @Getter
    private static final String major = "@MAJOR@";

    @Getter
    private static final String minor = "@MINOR@";

    @Getter
    private static final String revision = "@REV@";

    @Getter
    private static final String build = "@BUILD@";

    @Getter
    private static final String version = "@MAJOR@.@MINOR@.@REV@.@BUILD@";

    private STVersion() {}
}
