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
package org.openecomp.sdcrests.errors;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdcrests.errors.ZusammenExceptionMapper.FAILED_TO_PUBLISH_OUT_OF_SYNC;
import static org.openecomp.sdcrests.errors.ZusammenExceptionMapper.FAILED_TO_SYNC;
import static org.openecomp.sdcrests.errors.ZusammenExceptionMapper.SUB_ENTITY_ID_DOES_NOT_EXISTS;
import static org.openecomp.sdcrests.errors.ZusammenExceptionMapper.VLM_VSP_ITEM_ID_DOES_NOT_EXISTS;
import org.junit.Test;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.springframework.http.ResponseEntity;

public class ZusammenExceptionMapperTest {

    @Test
    public void shouldTransformENTITY_NOT_FOUND() {
        ZusammenExceptionMapper zusammenExceptionMapper = new ZusammenExceptionMapper();
        ResponseEntity response = zusammenExceptionMapper
            .handleSdcRuntimeException(new SdcRuntimeException(VLM_VSP_ITEM_ID_DOES_NOT_EXISTS));
        assertEquals(((ErrorCodeAndMessage)response.getBody()).getErrorCode(), Messages.ENTITY_NOT_FOUND.name());
    }

    @Test
    public void shouldTransformSUB_ENTITY_ID_DOES_NOT_EXISTS() {
        ZusammenExceptionMapper zusammenExceptionMapper = new ZusammenExceptionMapper();
        ResponseEntity response = zusammenExceptionMapper
                .handleSdcRuntimeException(new SdcRuntimeException(SUB_ENTITY_ID_DOES_NOT_EXISTS));
       // assertEquals(((ErrorCodeAndMessage)response.getBody()).getErrorCode(), Messages.SUB_ENTITY_NOT_FOUND.name());
        assertEquals(((ErrorCodeAndMessage)response.getBody()).getErrorCode(), Messages.SUB_ENTITY_NOT_FOUND.name());
    }

    @Test
    public void shouldTransformFAILED_TO_SYNC() {
        ZusammenExceptionMapper zusammenExceptionMapper = new ZusammenExceptionMapper();
        ResponseEntity response = zusammenExceptionMapper
            .handleSdcRuntimeException(new SdcRuntimeException(FAILED_TO_SYNC));
        assertEquals(((ErrorCodeAndMessage)response.getBody()).getErrorCode(), Messages.FAILED_TO_SYNC.name());
    }

    @Test
    public void shouldTransformFAILED_TO_PUBLISH_OUT_OF_SYNC() {
        ZusammenExceptionMapper zusammenExceptionMapper = new ZusammenExceptionMapper();
        ResponseEntity response = zusammenExceptionMapper
            .handleSdcRuntimeException(new SdcRuntimeException(FAILED_TO_PUBLISH_OUT_OF_SYNC));
        assertEquals(((ErrorCodeAndMessage)response.getBody()).getErrorCode(), Messages.FAILED_TO_PUBLISH_OUT_OF_SYNC.name());
    }
}