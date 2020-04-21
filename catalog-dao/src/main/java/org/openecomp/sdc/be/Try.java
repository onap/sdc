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
 * <code>Try</code> enables composition over computations that may fail at runtime.
 * <p>
 * A <code>Try</code> will produce a value <code>A</code> when executed successfully, and throw an exception otherwise.
 * <p>
 * In contrast with <code>Either</code>, <code>Try</code> relies on exception handling and is designed to encapsulate
 * computations resulting in an exception. The advantage compared to <code>Either</code> is that it can indicate the
 * line of code where exception is thrown which minimizing the stack trace. In other words, it can get rid of the noise
 * caused by nesting functions.
 * <p>
 * In addition, it also provides a higher level of composition:
 * <pre>{@code
 * User getUserById(Long userId) throws UserNotFoundException { ... }
 * Address getUserAddress(User user) throws UserAddressNotFoundException { ... }
 *
 * Long userId = 42;
 * try {
 *   User user = getUserById(userId);
 *   Address address = getUserAddress(user);
 *   return address;
 * } catch (UserNotFoundException e1) {
 *   log.error("User " + userId +" not found", e1);
 *   // error handling
 * } catch (UserAddressNotFoundException e2) {
 *   log.error("User " + userId + "'s address " not found", e2);
 *   // error handling
 * }</pre>
 * <p>
 * This example can be refactored to the following using <code>Try</code>:
 * <pre>{@code
 * Try<Address> address = getUserById(userId)
 *   .evalIfFailed(e1 -> log.error("User " + userId +" not found", e1))
 *   .andThen(user -> getUserAddress(user))
 *   .evalIfFailed(e2 -> log.error("User " + userId + "'s address " not found", e2));
 * </pre>
 * Note that as long <code>get</code> has not been called on <code>address</code>, no computation is performed. So
 * <code>Try</code>, also provides declarative semantic providing even more composability. Here is how a
 * <code>Try</code> could be evaluated:
 * <pre>{@code
 * address.get(
 *   address -> System.out.println(address), // on success
 *   e       -> e.printStacktrace(),         // on failure
 * );
 * </pre>
 * As shown here, the caller has no other option than to provide a failure handler in order to get the result of a
 * <code>Try</code>. This is an additional safety compared to what try-catch can provide with.
 *
 * @param <A>: The type of the produced value in case of success.
 */
public final class Try<A> {

    private final Supplier<A> lazyValue;

    private Try(Supplier<A> lazyValue) {
        this.lazyValue = lazyValue;
    }

    /**
     * Creates a <code>Try</code> which once executed produces an <code>A</code> if it's successful.
     *
     * @param s:   The supplier producing the <code>A</code>
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
     * Creates a failed <code>Try</code>
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

    /**
     * Discards the value resulting from a <code>Try</code> and returns <code>B</code> instead
     *
     * @param b:   The value to returned once the <code>Try</code> is successfully computed
     * @param <B>: The type of the returned value
     */
    public <B> Try<B> as(B b) {
        return map(a -> b);
    }

    /**
     * Raises an failure if the predicate is true
     *
     * @param predicate: defines if an error should be raised or not
     * @param e:         The exception returned by the Try once evaluated
     */
    public static Try<Void> raiseIf(boolean predicate, Exception e) {
        return predicate ? raise(e) : success(null);
    }

    /**
     * Raises an failure if the value produced by the <code>Try</code> is <code>null</code>
     *
     * @param e: The exception returned by the Try once evaluated
     */
    public Try<A> raiseIfNull(Exception e) {
        return andThen(a -> a == null ? raise(e) : success(a));
    }

    /**
     * Maps the exception returned by the <code>Try</code> if it results in a failure
     *
     * @param e0: The exception returned by the <code>Try</code> if it fails.
     */
    public Try<A> raiseIfFailed(Exception e0) {
        return mapError(e1 -> e0);
    }

    /**
     * Evalutes a side-effect if the <code>Try</code> results in a failure
     *
     * @param c: The side-effect being evaluated
     */
    public Try<A> evalIfFailed(Consumer<Exception> c) {
        return mapError(e -> {
            c.accept(e);
            return e;
        });
    }

    /**
     * Creates a successful <code>Try</code>
     *
     * @param a:   The value resulting from the <code>Try</code> once executed
     * @param <A>: The type of the value
     */
    public static <A> Try<A> success(A a) {
        return new Try<>(() -> a);
    }

    /**
     * Creates a <code>Try</code> from a left biased either. The left value is therefore the successful one.
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
     * Chain a <code>Try</code> with another one.
     * <p>
     * Pass the result of a successful <code>Try</code> to a function producing another <code>Try</code>. If the initial
     * <code>Try</code> is a failure, this function will never be called.
     *
     * @param f    : The function converting a success value <code>A</code> to a <code>Try<B></code>
     * @param <B>: The type of the value produced by the final <code>Try</code> when successful
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
     * Converts an Exception resulting from a failure to another <code>Try</code>.
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
     * exception resulting from the failed <code>Try</code>. This should be considered as a programming error.
     *
     * @param onSuccess: defines how a successful value <code>A</code> should be converted to an Either
     * @param onFailure: defines how an error should be converted to an Either
     * @param <E>:       The exception resulting from a failed <code>Try</code>
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
     * Converts to a right biased either where the the value resulting from a successful <code>Try</code> is stored on
     * the right
     * <p>
     * Keep in mind that calling this function may result in throwing a casting exception if E is not the type of the
     * exception resulting from the failed <code>Try</code>. This should be considered as a programming error.
     *
     * @param onFailure: defines how an error should be converted to a <code>L</code>
     * @param <E>:       The exception resulting from a failed <code>Try</code>
     * @param <L>:       The type to which <code>E</code> should be converted to
     * @return a Right biased Either
     * @throws ClassCastException if <code>E</code> is not the type of the exception resulting from the failed
     *                            <code>Try</code>. This should be considered as a programming error.
     */
    public <E, L> Either<L, A> unsafeToEither(Function<E, Either<L, A>> onFailure) {
        return unsafeToEither(Either::right, onFailure);
    }

    /**
     * Converts to a left biased either where the the value resulting from a successful <code>Try</code> is stored on
     * the left
     * <p>
     * Keep in mind that calling this function may result in throwing a casting exception if E is not the type of the
     * exception resulting from the failed <code>Try</code>. This should be considered as a programming error.
     *
     * @param onFailure: defines how an error should be converted to a <code>R</code>
     * @param <E>:       The exception resulting from a failed <code>Try</code>
     * @param <R>:       The type to which <code>E</code> should be converted to
     * @return a Left biased Either
     * @throws ClassCastException if <code>E</code> is not the type of the exception resulting from the failed
     *                            <code>Try</code>. This should be considered as a programming error.
     */
    public <E, R> Either<A, R> unsafeToLeftBiasedEither(
        Function<E, Either<A, R>> onFailure
    ) {
        return unsafeToEither(Either::left, onFailure);
    }

    /**
     * Execute the <code>Try</code> and return the resulting value
     *
     * @param onSuccess: defines how to convert a successful value <code>A</code> to a <code>B</code>
     * @param onFailure: defines how to convert an error to a <code>B</code>
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
     * Executes the <code>Try</code>
     *
     * @param onSuccess: defines how to handle a successful value <code>A</code>
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
     * Convert the value resulting from a successful <code>Try</code> to another type
     *
     * @param f:   The function mapping a successful value <code>A</code> to <code>B</code>
     * @param <B>: The resulting type
     * @return A <code>Try</code> which once executed and successful converts the result into a <code>B</code>
     */
    public <B> Try<B> map(Function<A, B> f) {
        return make(() -> f.apply(unsafeGet()));
    }

    /**
     * Re-throw an exception resulting from a failed computation
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
