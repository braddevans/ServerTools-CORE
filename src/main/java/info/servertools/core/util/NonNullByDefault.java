package info.servertools.core.util;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

@Nonnull
@TypeQualifierDefault({
        FIELD,
        LOCAL_VARIABLE,
        METHOD,
        PARAMETER
})
public @interface NonNullByDefault {
}
