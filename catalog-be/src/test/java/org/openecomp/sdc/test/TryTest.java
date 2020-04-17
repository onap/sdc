package org.openecomp.sdc.test;

import static java.util.function.Function.identity;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import fj.data.Either;
import org.junit.Test;
import org.openecomp.sdc.be.tosca.Try;

public class TryTest {

    private static final Try<Integer> FAILURE = Try.failure(new RuntimeException());
    private static final Try<Integer> SUCCESS = Try.success(42);

    @Test
    public void testSuccessfulMake() {
        assertThat(Try.make(() -> 42).get(i -> 42, e -> 0), is(42));
    }

    @Test
    public void testFailedMake() {
        assertThat(Try.make(() -> {
            throw new RuntimeException();
        }).get(i -> 42, e -> 0), is(0));
    }

    @Test
    public void testSuccess() {
        assertThat(SUCCESS.get(i -> 42, e -> 0), is(42));
    }

    @Test
    public void testOnFailure() {
        assertThat(FAILURE.get(i -> 42, e -> 0), is(0));
    }

    @Test
    public void testLeftBiased() {
        assertThat(Try.leftBiased(
            Either.left(42),
            i -> new RuntimeException()
        ).get(i -> 42, e -> 0), is(42));
    }

    @Test
    public void testSuccessToEither() {
        assertThat(SUCCESS.unsafeToEither(
            i -> Either.right(42),
            e -> Either.left(0)
        ), is(Either.right(42)));
    }

    @Test
    public void testFailureToEither() {
        assertThat(FAILURE.unsafeToEither(
            i -> Either.right(42),
            e -> Either.left(0)
        ), is(Either.left(0)));
    }

    @Test
    public void testSuccessToEither2() {
        assertThat(SUCCESS.unsafeToEither(e -> Either.left(0)), is(Either.right(42)));
    }

    @Test
    public void testFailureToEither2() {
        assertThat(FAILURE.unsafeToEither(e -> Either.left(0)), is(Either.left(0)));
    }

    @Test
    public void testSuccessToLeftBiasedEither() {
        assertThat(SUCCESS.unsafeToLeftBiasedEither(e -> Either.left(0)), is(Either.left(42)));
    }

    @Test
    public void testFailureToLeftBiasedEither() {
        assertThat(FAILURE.unsafeToLeftBiasedEither(e -> Either.right(0)), is(Either.right(0)));
    }

    @Test
    public void testSuccessMap() {
        assertThat(SUCCESS.map(i -> i + 1).get(identity(), e -> 0), is(43));
    }

    @Test
    public void testFailureMap() {
        assertThat(FAILURE.map(i -> i + 1).get(identity(), e -> 0), is(0));
    }
}
