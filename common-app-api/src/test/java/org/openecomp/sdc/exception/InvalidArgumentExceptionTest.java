/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdc.exception;

import static org.hamcrest.core.Is.is;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InvalidArgumentExceptionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void validateOneArgConstructorsThrowsCorrectException() {
        final String testMessage = "test error message";
        expectedException.expect(InvalidArgumentException.class);
        expectedException.expectMessage(testMessage);
        throw new InvalidArgumentException(testMessage);
    }

    @Test
    public void validateTwoArgConstructorsThrowsCorrectException() {
        final String testMessage = "test error message";
        final String testThrowableMessage = "test throwable";
        final Throwable testThrowable = new Throwable(testThrowableMessage);
        expectedException.expect(InvalidArgumentException.class);
        expectedException.expectMessage(testMessage);
        expectedException.expectCause(is(testThrowable));
        throw new InvalidArgumentException(testMessage, testThrowable);
    }
}
