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

package org.openecomp.sdc;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;

public class TestUtils {
    public static boolean downloadedPayloadMatchesExpected(Response response, byte[] expected) {
        boolean result = false;
        try (InputStream is = response.readEntity(InputStream.class)) {
            byte[] body = IOUtils.toByteArray(is);
            assertArrayEquals(expected, body);
            result = true;
        } catch(Exception ex) {
            result = false;
        }
        return result;
    }
}
