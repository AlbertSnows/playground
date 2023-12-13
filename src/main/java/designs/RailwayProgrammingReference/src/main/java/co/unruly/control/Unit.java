package co.unruly.control;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Unit type, which contains only one possible value.
 * <p>
 * This exists to offer a bridge between void and regular functions, providing
 * convenience methods to convert between them.
 */
public enum Unit {

    UNIT;

    /**
     * Converts a Consumer to a Function, which returns Unit.UNIT
     */
    @Contract(pure = true)
    public static <T> @NotNull Function<T, Unit> functify(Consumer<T> toVoid) {
        return x -> {
            toVoid.accept(x);
            return Unit.UNIT;
        };
    }

    /**
     * Converts a Function to a Consumer, throwing away the return value
     */
    @Contract(pure = true)
    public static <T> @NotNull Consumer<T> voidify(@NotNull Function<T, ?> function) {
        return function::apply;
    }

    /**
     * A no-op function which takes any argument, does nothing, and returns Unit.UNIT
     */
    public static <T> Unit noOp(T __) {
        return UNIT;
    }

    /**
     * A no-op consumer which takes any argument and does nothing
     */
    public static <T> void noOpConsumer(T __) {
        // do nothing
    }
}
