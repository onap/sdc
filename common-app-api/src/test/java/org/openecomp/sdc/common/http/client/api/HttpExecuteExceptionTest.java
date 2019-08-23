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

package org.openecomp.sdc.common.http.client.api;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpExecuteExceptionTest {

    @Test( expected = HttpExecuteException.class)
    public void validateOneArgConstructorWithMessage() throws Exception{
        final String testMessage = "test error message";

        try {
            throw new HttpExecuteException(testMessage);
        } catch (Exception e) {
            assertEquals(e.getMessage(),testMessage);
            throw e;
        }
    }

    @Test( expected = HttpExecuteException.class)
    public void validateOneArgConstructorWithThrowable() throws Exception{
        final String testThrowableMessage = "test throwable error message";
        final Throwable throwable = new Throwable(testThrowableMessage);

        try {
            throw new HttpExecuteException(throwable);
        } catch (Exception e) {
            assertEquals(e.getCause().getMessage(),testThrowableMessage);
            throw e;
        }
    }

    @Test( expected = HttpExecuteException.class)
    public void validateAllArgConstructor() throws Exception{
        final String testMessage = "test error message";
        final String testThrowableMessage = "test throwable error message";
        final Throwable testThrowable = new Throwable(testThrowableMessage);

        try {
            throw new HttpExecuteException(testMessage, testThrowable);
        } catch (Exception e) {
            assertEquals(e.getCause().getMessage(),testThrowableMessage);
            assertEquals(e.getMessage(),testMessage);
            throw e;
        }
    }
}
