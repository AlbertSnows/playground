package co.unruly.control.result;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the result of an operation which could fail, represented as either
 * a Success (wrapping the successful output) or a Failure (wrapping a value
 * describing how it failed).
 * <p>
 * The interface for Result is minimal: many common sample operations are implemented
 * with static methods on Introducers, Transformers, and Resolvers.
 * <p>
 * These can be composed upon a Result by passing them as arguments to then().
 *
 * @param <S> The type of success
 * @param <F> The type of failure
 */
public abstract class Result<S, F> implements Serializable {

    private Result() {
    }

    /**
     * Creates a new Success
     * @param value to wrap
     * @param <S> success type
     * @param <F> fail type
     * @return success wrapper of S
     */
    @Contract("_ -> new")
    public static <S, F> @NotNull Result<S, F> success(S value) {
        return new Success<>(value);
    }

    // is this needed?
//    /**
//     * Creates a new Success, taking the failure type for contexts where it can't be inferred.
//     * @param value to wrap
//     * @param failureType class type
//     * @param <S> success type
//     * @param <F> failure type
//     * @return S wrapped in success
//     */
//    @Contract("_, _ -> new")
//    public static <S, F> @NotNull Result<S, F> success(S value, Class<F> failureType) {
//        return new Success<>(value);
//    }

    /**
     * Creates a new Failure
     * @param error to wrap
     * @param <S> success type
     * @param <F> fail type
     * @return F wrapped in failure
     */
    @Contract("_ -> new")
    public static <S, F> @NotNull Result<S, F> failure(F error) {
        return new Failure<>(error);
    }

    // is this needed?
//    /**
//     * Creates a new Failure, taking the success type for contexts where it can't be inferred.
//     * @param error to wrap
//     * @param successType for context
//     * @param <S> success type
//     * @param <F> fail type
//     * @return F wrapped in failure
//     */
//    @Contract("_, _ -> new")
//    public static <S, F> @NotNull Result<S, F> failure(F error, Class<S> successType) {
//        return new Failure<>(error);
//    }

    /**
     * Takes two functions, the first of which is executed in the case that this
     * Result is a Success, the second of which is executed in the case that it
     * is a Failure, on the wrapped value in either case.
     *
     * @param onSuccess the function to process the success value, if this is a Success
     * @param onFailure the function to process the failure value, if this is a Failure
     * @param <R>       the type of the end result
     * @return The result of executing onSuccess if this result is a Success, or onFailure if it's a failure
     */
    public abstract <R, R1 extends R, R2 extends R> R either(Function<S, R1> onSuccess, Function<F, R2> onFailure);

    /**
     * Applies a function to this Result. This permits inverting the calling convention, so that instead of the following:
     * <pre>
     * {@code
     * Result<Shop, String> shop;
     * Result<Hat, String> hat = map(shop, Shop::purchaseHat);
     * }
     * </pre>
     * <p>
     * We can write:
     * <pre>
     * {@code
     * Result<Shop, String> shop;
     * Result<Hat, String> hat = shop.then(map(Shop::purchaseHat));
     * }
     * </pre>
     * <p>
     * The advantage of this is that it composes more nicely: instead of this:
     * <pre>
     * {@code
     * Result<Town, String> town;
     * Result<Hat, String> hat = map(map(shop, Town::findHatShop), Shop::purchaseHat);
     * }
     * </pre>
     * <p>
     * We can write:
     * * <pre>
     * {@code
     * Result<Town, String> town;
     * Result<Hat, String> hat = town.then(map(Town::findHatShop)
     *                               .then(map(Shop::purchaseHat));
     * }
     * </pre>
     */
    public <T, T2 extends T> T then(@NotNull Function<Result<S, F>, T2> biMapper) {
        return biMapper.apply(this);
    }

    private static final class Success<L, R> extends Result<L, R> {
        private final L value;

        private Success(L value) {
            this.value = value;
        }

        @Override
        public <S, S1 extends S, S2 extends S> S either(@NotNull Function<L, S1> onSuccess, Function<R, S2> onFailure) {
            return onSuccess.apply(value);
        }

        @Contract(pure = true)
        @Override
        public @NotNull String toString() {
            return "Success{" + value + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Success<?, ?> that = (Success<?, ?>) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    private static final class Failure<L, R> extends Result<L, R> {
        private final R value;

        private Failure(R value) {
            this.value = value;
        }

        @Override
        public <S, S1 extends S, S2 extends S> S either(Function<L, S1> onSuccess, @NotNull Function<R, S2> onFailure) {
            return onFailure.apply(value);
        }

        @Contract(pure = true)
        @Override
        public @NotNull String toString() {
            return "Failure{" + value + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Failure<?, ?> that = (Failure<?, ?>) o;
            return Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

}
