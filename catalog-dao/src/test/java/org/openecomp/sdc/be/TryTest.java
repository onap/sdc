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

import static java.util.function.Function.identity;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openecomp.sdc.be.Try.raise;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Test;


public class TryTest {

    private static final Try<Integer> FAILURE = raise(new RuntimeException());
    private static final Try<Integer> SUCCESS = Try.success(42);
    private static final MappedException mappedException = new MappedException();

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
    public void testToEither2() {
        assertThat(SUCCESS.unsafeToEither(e -> Either.left(0)), is(Either.right(42)));
        assertThat(FAILURE.unsafeToEither(e -> Either.left(0)), is(Either.left(0)));
    }

    @Test
    public void testToLeftBiasedEither() {
        assertThat(SUCCESS.unsafeToLeftBiasedEither(e -> Either.left(0)), is(Either.left(42)));
        assertThat(FAILURE.unsafeToLeftBiasedEither(e -> Either.right(0)), is(Either.right(0)));
    }

    @Test
    public void testMap() {
        assertThat(SUCCESS.map(i -> i + 1).get(identity(), e -> 0), is(43));
        assertThat(FAILURE.map(i -> i + 1).get(identity(), e -> 0), is(0));
    }

    @Test
    public void testConsume() {
        SUCCESS.consume(i -> Assert.assertTrue(true), e -> Assert.fail());
        FAILURE.consume(i -> Assert.fail(), e -> Assert.assertTrue(true));
    }

    @Test
    public void testAndThen() {
        assertThat(SUCCESS.andThen(i -> Try.make(() -> i + 1)).get(identity(), e -> 0), is(43));
        assertThat(FAILURE.andThen(i -> Try.make(() -> i + 1)).get(identity(), e -> 0), is(0));
    }

    @Test
    public void testSuccessMapError() {
        assertThat(SUCCESS.mapError(e -> mappedException).get(identity(), e -> 0), is(42));
    }

    @Test
    public void testFailureMapError() {
        try {
            FAILURE
                .mapError(i -> mappedException)
                .rethrow();
            Assert.fail();
        } catch (Exception ex) {
            assertThat(ex, is(mappedException));
        }
    }

    @Test
    public void testSuccessMapErrorWith() {
        assertThat(SUCCESS.mapErrorWith(e -> raise(mappedException)).get(identity(), e -> 0), is(42));
    }

    @Test
    public void testFailureMapErrorWith() {
        try {
            FAILURE
                .mapErrorWith(i -> raise(mappedException))
                .rethrow();
            Assert.fail();
        } catch (Exception ex) {
            assertThat(ex, is(mappedException));
        }
    }

    static private class MappedException extends Exception {

    }
}
