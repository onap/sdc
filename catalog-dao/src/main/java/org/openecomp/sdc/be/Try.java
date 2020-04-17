/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be;

import fj.data.Either;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Try enables composition over computations that may fail at runtime.
 * <p>
 * A Try will produce a value `A` when executed successfully, and throw an exception otherwise.
 * <p>
 * In contrast with `Either`, `Try` relies on exception handling and is designed to encapsulate computations resulting
 * in an exception. The advantage compared to `Either` is that the indication of the line of code throwing the exception
 * is preserved, which eases debugging while minimizing the stack trace. On the other it also provides a higher level of
 * composition, so it is pretty much the best of both worlds: ``` String unsafe() throws RuntimeException { ... }
 * Try<String> safe = Try.make(() -> unsafe()); ``` In order to get the value result of a `Try`,one has to call `get`.
 * `get` takes two functions,one that is called in case of success,one that is called in case of errors: ``` Try<String>
 * safe= Try.make(() -> unsafe()); safe.consume(s -> System.out.println(s), exception -> { throw exception; }); ``` A
 * safer way is to provide an exception handler: ``` safe.get(s -> s), exception-> { // handle the exception and return
 * a fallback value }); ```
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
        } catch (RaiseException e) {
            return raise(cleanStackTrace(e.getWrapped()));
        } catch (Exception e) {
            return raise(cleanStackTrace(e));
        }
    }

    /**
     * Creates a failed Try
     *
     * @param e: The exception thrown when the Try is executed
     */
    public static <A> Try<A> raise(Exception e) {
        return new Try<>(() -> {
            throw new RaiseException(cleanStackTrace(e));
        });
    }

    private static class RaiseException extends RuntimeException {
        private final Exception cause;
        public RaiseException(Exception cause) {
            super(cause);
            this.cause = cause;
        }

        public Exception getWrapped() {
            return cause;
        }
    }

    public <B> Try<B> as(B b) {
        return map(a -> b);
    }

    public static Try<Void> raiseIf(boolean predicate, Exception e) {
        return predicate ? raise(e) : success(null);
    }

    public Try<A> raiseIfNull(Exception e) {
        return andThen(a -> a == null ? raise(e) : success(a));
    }

    public Try<A> raiseIfFailed(Exception e0) {
        return mapError(e1 -> e0);
    }

    public Try<A> evalIfFailed(Consumer<Exception> c) {
        return mapError(e -> {
            c.accept(e);
            return e;
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
            e -> raise(onRight.apply(e))
        );
    }

    /**
     * Chain a Try with another one.
     * <p>
     * Pass the result of a successful `Try` to a function producing another `Try`. If the initial Try is a failure,
     * this function will never be called.
     *
     * @param f    : The function converting a success value `A` to a `Try<B>`
     * @param <B>: The type of the value produced by the final Try when successful
     */
    public <B> Try<B> andThen(Function<A, Try<B>> f) {
        return make(() -> get(f, Try::raise).unsafeGet());
    }

    /**
     * Converts an Exception resulting from a failure to another Exception
     *
     * @param f    : The function converting the exception
     * @param <E>: The type of Exception resulting from the failure
     */
    public <E extends Exception> Try<A> mapError(Function<E, Exception> f) {
        return mapErrorWith((E e) -> raise(f.apply(e)));
    }

    /**
     * Converts an Exception resulting from a failure to another Try.
     *
     * @param f    : The function converting the exception
     * @param <E>: The type of Exception resulting from the failure
     */
    @SuppressWarnings("unchecked")
    public <E extends Exception> Try<A> mapErrorWith(Visit<A, E> f) {
        return get(Try::success, e -> {
            try {
                return f.visit((E) e);
            } catch (ClassCastException cce) {
                cce.printStackTrace();
                return raise(new MatchException(cce));
            }
        });
    }

    @FunctionalInterface
    public interface Visit<A, E> {

        Try<A> visit(E e);
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
     * @throws ClassCastException if `E` is not the type of the exception resulting from the failed Try. This should be
     *                            considered as a programming error.
     */
    public <E, L> Either<L, A> unsafeToEither(Function<E, Either<L, A>> onFailure) {
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
     * @throws ClassCastException if `E` is not the type of the exception resulting from the failed Try. This should be
     *                            considered as a programming error.
     */
    public <E, R> Either<A, R> unsafeToLeftBiasedEither(
        Function<E, Either<A, R>> onFailure
    ) {
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
            return onSuccess.apply(unsafeGet());
        } catch (RaiseException e) {
            return onFailure.apply(cleanStackTrace(e.getWrapped()));
        } catch (Exception e) {
            return onFailure.apply(cleanStackTrace(e));
        }
    }

    /**
     * Executes the Try
     *
     * @param onSuccess: defines how to handle a successful value `A`
     * @param onFailure: defines how to handle an error
     */
    public void consume(Consumer<A> onSuccess, Consumer<Throwable> onFailure) {
        try {
            onSuccess.accept(lazyValue.get());
        } catch (Exception e) {
            onFailure.accept(cleanStackTrace(e));
        }
    }

    /**
     * Convert the value resulting from a successful Try to another type
     *
     * @param f:   The function mapping a successful value `A` to `B`
     * @param <B>: The resulting type
     * @return A Try which once executed and successful converts the result into a `B`
     */
    public <B> Try<B> map(Function<A, B> f) {
        return make(() -> f.apply(unsafeGet()));
    }

    /**
     * Re-throw an exception resulting from a failed computation
     *
     * @throws Exception
     */
    public void rethrow() throws Exception {
        try {
            this.unsafeGet();
        } catch (RaiseException e) {
            throw cleanStackTrace((Exception) e.getCause());
        }
    }

    private A unsafeGet() {
        return lazyValue.get();
    }

    // This function enables us to simplify the stack trace during debugging. It points right where the exception has
    // been created to enhance readability.
    private static Exception cleanStackTrace(Exception e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        ArrayList<StackTraceElement> result = new ArrayList<>();
        for (StackTraceElement se : stackTrace) {
            if (!se.getClassName().equals(Try.class.getName())) {
                result.add(se);
            }
        }
        e.setStackTrace(result.toArray(new StackTraceElement[0]));
        return e;
    }
}
