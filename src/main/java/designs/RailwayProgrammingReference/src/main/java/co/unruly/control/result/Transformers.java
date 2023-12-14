package co.unruly.control.result;

import co.unruly.control.ConsumableFunction;
import co.unruly.control.HigherOrderFunctions;
import co.unruly.control.ThrowingLambdas;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static co.unruly.control.HigherOrderFunctions.peek;
import static co.unruly.control.result.Introducers.tryTo;

/**
 * A collection of functions which take a Result and return a Result.
 */
@SuppressWarnings("unused")
public interface Transformers {

    /**
     * Returns a function which takes a Result and, if it's a success, applies the mapping value to that
     * success, otherwise returning the original failure.
     */
    static <IS, OS, F> @NotNull Function<Result<IS, F>, Result<OS, F>>
    onSuccess(@NotNull Function<IS, OS> mappingFunction) {
        return attempt(mappingFunction.andThen(Result::success));
    }

    /**
     * Returns a Consumer which takes a Result and, if it's a Success, passes it to the provided consumer.
     */
    @Contract(pure = true)
    static <S, F> @NotNull ConsumableFunction<Result<S, F>> onSuccessDo(Consumer<S> consumer) {
        return r -> r.then(onSuccess(peek(consumer)));
    }

    /**
     * Returns a function which takes a Result with an Exception failure type and, if it's a success, applies
     * the mapping value to that success, returning a new success unless an exception is thrown, when it
     * returns a failure of that exception. If the input was a failure, it returns that failure.
     */
    static <IS, OS, X extends Exception> @NotNull Function<Result<IS, Exception>, Result<OS, Exception>>
    onSuccessTry(
        ThrowingLambdas.ThrowingFunction<IS, OS, X> throwingFunction
    ) {
        return attempt(tryTo(throwingFunction));
    }


    /**
     * Returns a function which takes a Result and, if it's a success, applies the mapping value
     * to that success, returning a new success unless an exception is thrown, when it
     * returns a failure of the exception-mapper applied to the exception.
     * If the input was a failure, it returns that failure.
     */
    static <IS, OS, F, X extends Exception> @NotNull Function<Result<IS, F>, Result<OS, F>> onSuccessTry(
        ThrowingLambdas.ThrowingFunction<IS, OS, X> throwingFunction,
        Function<Exception, F> exceptionMapper
    ) {
        return attempt(tryTo(throwingFunction).andThen(onFailure(exceptionMapper)));
    }

    /**
     * Returns a function which takes a Result and, if it's a success, applies the provided function
     * to that success - generating a new Result - and returns that Result. Otherwise, returns the
     * original failure.
     */
    @Contract(pure = true)
    static <IS, OS, F, OF extends F> @NotNull Function<Result<IS, F>, Result<OS, F>>
    attempt(Function<IS, Result<OS, OF>> mappingFunction) {
        return r -> r.either(
                mappingFunction.andThen(onFailure((Function<OF, F>) HigherOrderFunctions::upcast)),
                Result::failure);
    }

    /**
     * Returns a function which takes a Result and, if it's a failure, applies the provided function
     * to that failure. Otherwise, returns the original success.
     */
    static <S, IF, OF> @NotNull Function<Result<S, IF>, Result<S, OF>>
    onFailure(@NotNull Function<IF, OF> mappingFunction) {
        return recover(mappingFunction.andThen(Result::failure));
    }

    /**
     * Returns a consumer which takes a Result and, if it's a failure, passes it to the provided consumer
     */
    @Contract(pure = true)
    static <S, F> @NotNull ConsumableFunction<Result<S, F>> onFailureDo(Consumer<F> consumer) {
        return r -> r.then(onFailure(peek(consumer)));
    }

    /**
     * Takes a Result of a Stream as a success or a single failure, and returns a Stream of Results
     * containing all the successes, or the single failure.
     * <p>
     * The main use-case for this is to follow mapping over tryTo() on a function which was designed to
     * be flat-mapped over.
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Result<Stream<S>, F>, Stream<Result<S, F>>> unwrapSuccesses() {
        return r -> r.either(
            successes -> successes.map(Result::success),
            failure -> Stream.of(Result.failure(failure))
        );
    }

    /**
     * Returns a function which takes a Result and, if it's a failure, applies the provided function to
     * that failure - generating a new Result - and returns that Result. Otherwise, return the original
     * success.
     */
    @Contract(pure = true)
    static <S, IF, OF, OS extends S> @NotNull Function<Result<S, IF>, Result<S, OF>>
    recover(Function<IF, Result<OS, OF>> recoveryFunction) {
        return r -> r.either(
                Result::success,
                recoveryFunction.andThen(onSuccess((Function<OS, S>) HigherOrderFunctions::upcast)));
    }

    /**
     * Returns a function which takes a Result, and converts failures to successes and vice versa.
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Result<S, F>, Result<F, S>> invert() {
        return r -> r.either(Result::failure, Result::success);
    }

    /**
     * Returns a function which takes a Result whose success type is itself a Result, and merges the failure cases,
     * so we have a flat Result.
     * <p>
     * Note that *most of the time* this shouldn't be required, and indicates using onSuccess() when attempt() would
     * be more appropriate.
     * <p>
     * There are some situations though where we do end up with constructs like this: one example
     * is when a function which returns a Result can throw exceptions (eg, when a Result-returning handler is passed
     * into a database context). Passing that call into a tryTo() will yield a success type of the inner Result, wrapped
     * in an outer Result for the tryTo().
     * <p>
     * If the failure types of the inner and outer failure types do not match, you'll need to either first convert the
     * failures of the outer Result or use the overload which maps the failures of the inner Result.
     */
    @Contract(pure = true)
    static <S, F> @NotNull Function<Result<Result<S, F>, F>, Result<S, F>> mergeFailures() {
        return attempt(i -> i);
    }

    /**
     * Returns a function which takes a Result whose success type is itself a Result, and merges the failure cases
     * so we have a flat Result.
     * <p>
     * Note that *most of the time* this shouldn't be required, and indicates using onSuccess() when attempt() would
     * be more appropriate.
     * <p>
     * There are some situations though where we do end up with constructs like this: one example
     * is when a function which returns a Result can throw exceptions (eg, when a Result-returning handler is passed
     * into a database context). Passing that call into a tryTo() will yield a success type of the inner Result, wrapped
     * in an outer Result for the tryTo().
     * <p>
     * In these cases, it's more likely the inner failure type is domain-specific, so the default approach is to map the
     * outer failure to the inner failure and then merge.
     */
    @Contract(pure = true)
    static <S, F1, F2> @NotNull Function<Result<Result<S, F1>, F2>, Result<S, F1>>
    mergeFailures(Function<F2, F1> failureMapper) {
        return r -> r.then(onFailure(failureMapper)).then(mergeFailures());
    }
}
