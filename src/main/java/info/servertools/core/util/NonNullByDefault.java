package info.servertools.core.util;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.Retention;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Nonnull
@TypeQualifierDefault({
        METHOD,
        FIELD,
        PARAMETER
})
@Retention(RUNTIME)
public @interface NonNullByDefault {
}
