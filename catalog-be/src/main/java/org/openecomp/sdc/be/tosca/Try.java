package org.openecomp.sdc.be.tosca;

import fj.data.Either;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Try enables composition over computations that may fail at runtime.
 * <p>
 * A Try will produce a value `A` when executed successfully, and throw an exception otherwise.
 * The advantage compared to `Either` is that the inidication of the line of code throwing the exception is
 * preserved, which eases debugging.
 *
 * @param <A>: The type of the produced value in case of success.
 */
public final class Try<A> {

    private final Supplier<A> lazyValue;

    private Try(Supplier<A> lazyValue) {
        this.lazyValue = lazyValue;
    }

    /**
     * Creates a Try which once executed produces an `A` if it's successful.
     *
     * @param s:   The supplier producing the `A`
     * @param <A>: The type of the successful value
     */
    public static <A> Try<A> make(Supplier<A> s) {
        try {
            return success(s.get());
        } catch (Exception e) {
            return failure(e);
        }
    }

    /**
     * Creates a failed Try
     *
     * @param e: The exception thrown when the Try is executed
     */
    public static <A> Try<A> failure(Exception e) {
        return new Try<>(() -> {
            throw new RuntimeException(e);
        });
    }

    /**
     * Creates a successful Try
     *
     * @param a:   The value resulting from the Try once executed
     * @param <A>: The type of the value
     */
    public static <A> Try<A> success(A a) {
        return new Try<>(() -> a);
    }

    /**
     * Creates a Try from a left biased either. The left value is therefore the successful one.
     *
     * @param eae:     The either to convert
     * @param onRight: defines how to convert a right to an exception
     * @param <A>:     The type of the successful value (left)
     * @param <E>:     The type of the failure resulting from the either (right)
     */
    public static <A, E> Try<A> leftBiased(Either<A, E> eae, Function<E, Exception> onRight) {
        return eae.either(
            Try::success,
            e -> failure(onRight.apply(e))
        );
    }

    /**
     * Converts to an either
     * <p>
     * Keep in mind that calling this function may result in throwing a casting exception if E is not the type of the
     * exception resulting from the failed Try. This should be considered as a programming error.
     *
     * @param onSuccess: defines how a successful value `A` should be converted to an Either
     * @param onFailure: defines how an error should be converted to an Either
     * @param <E>:       The exception resulting from a failed Try
     * @param <L>:       The left type of the  Either
     * @param <R>:       The right type of the  Either
     */
    @SuppressWarnings("unchecked")
    public <E, L, R> Either<L, R> unsafeToEither(
        Function<A, Either<L, R>> onSuccess,
        Function<E, Either<L, R>> onFailure
    ) {
        return get(onSuccess, e -> onFailure.apply((E) e));
    }

    /**
     * Converts to a right biased either where the the value resulting from a successful Try is stored on the right
     * <p>
     * Keep in mind that calling this function may result in throwing a casting exception if E is not the type of the
     * exception resulting from the failed Try. This should be considered as a programming error.
     *
     * @param onFailure: defines how an error should be converted to a `L`
     * @param <E>:       The exception resulting from a failed Try
     * @param <L>:       The type to which `E` should be converted to
     * @return a Right biased Either
     */
    public <E, L> Either<L, A> unsafeToEither(Function<E, Either<L, A>> onFailure) throws ClassCastException {
        return unsafeToEither(Either::right, onFailure);
    }

    /**
     * Converts to a left biased either where the the value resulting from a successful Try is stored on the left
     * <p>
     * Keep in mind that calling this function may result in throwing a casting exception if E is not the type of the
     * exception resulting from the failed Try. This should be considered as a programming error.
     *
     * @param onFailure: defines how an error should be converted to a `R`
     * @param <E>:       The exception resulting from a failed Try
     * @param <R>:       The type to which `E` should be converted to
     * @return a Left biased Either
     */
    public <E, R> Either<A, R> unsafeToLeftBiasedEither(
        Function<E, Either<A, R>> onFailure
    ) throws ClassCastException {
        return unsafeToEither(Either::left, onFailure);
    }

    /**
     * Execute the Try and return the resulting value
     *
     * @param onSuccess: defines how to convert a successful value `A` to a `B`
     * @param onFailure: defines how to convert an error to a `B`
     * @param <B>:       The resulting type
     */
    public <B> B get(Function<A, B> onSuccess, Function<Exception, B> onFailure) {
        try {
            return onSuccess.apply(lazyValue.get());
        } catch (Exception e) {
            return onFailure.apply(e);
        }
    }

    /**
     * Map the value resulting from a successful Try
     *
     * @param f:   The function mapping a successful value `A` to `B`
     * @param <B>: The resulting type
     * @return: A Try which once executed and successful converts the result into a `B`
     */
    public <B> Try<B> map(Function<A, B> f) {
        return make(() -> f.apply(unsafeGet()));
    }

    private A unsafeGet() {
        return lazyValue.get();
    }
}
