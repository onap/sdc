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
package org.openecomp.sdc.common.util;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fj.data.Either;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.fe.config.Configuration;

public class HttpUtilTest {

    @Test
    public void validateGetObjectFromJsonReturnsValidObjectFromValidJason() throws IOException {
        final Configuration testObject = new Configuration();
        testObject.setVersion("1.0.test");
        testObject.setThreadpoolSize(5);
        final String testObjectAsJson = new GsonBuilder().setPrettyPrinting().create().toJson(testObject);
        final ServletInputStream servletInputStream = new TestServletInputStream(testObjectAsJson);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(servletInputStream);
        Either<Configuration, Exception> result = HttpUtil.getObjectFromJson(request, Configuration.class);
        assertTrue(result.isLeft());
        Configuration returnedConfiguration = result.left().value();
        assertEquals(returnedConfiguration.getVersion(), testObject.getVersion());
        assertEquals(returnedConfiguration.getBeProtocol(), testObject.getBeProtocol());
        assertEquals(returnedConfiguration.getThreadpoolSize(), testObject.getThreadpoolSize());
    }

    @Test
    public void validateGetObjectFromJsonReturnsExceptionIfStreamThrowIOException() throws IOException {
        final String testException = "test exception";
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getInputStream()).thenThrow(new IOException(testException));
        Either<Configuration, Exception> result = HttpUtil.getObjectFromJson(request, Configuration.class);
        assertTrue(result.isRight());
        assertEquals(result.right().value().getMessage(), testException);
    }

    @Test
    public void validateGetObjectFromJsonReturnsExceptionIfInputStringIsInvalid() throws IOException {
        final ServletInputStream servletInputStream = new TestServletInputStream("Wrong Json Object");
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getInputStream()).thenReturn(servletInputStream);
        Either<Configuration, Exception> result = HttpUtil.getObjectFromJson(request, Configuration.class);
        assertTrue(result.isRight());
        assertEquals(result.right().value().getClass(), JsonSyntaxException.class);
    }

    @Test
    public void validateGetObjectFromJsonReturnsExceptionIfInputIsNull() throws IOException {
        Either<Configuration, Exception> result = HttpUtil.getObjectFromJson(null, Configuration.class);
        assertTrue(result.isRight());
        assertEquals(result.right().value().getClass(), NullPointerException.class);
    }

    class TestServletInputStream extends ServletInputStream {

        private InputStream testStream;

        TestServletInputStream(String testJson) {
            testStream = new ByteArrayInputStream(testJson.getBytes());
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
        }

        @Override
        public int read() throws IOException {
            return testStream.read();
        }
    }
}
