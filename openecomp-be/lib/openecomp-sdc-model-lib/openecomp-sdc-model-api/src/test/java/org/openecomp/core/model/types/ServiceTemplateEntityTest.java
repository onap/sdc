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

package org.openecomp.core.model.types;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.versioning.dao.types.Version;

class ServiceTemplateEntityTest {

    private static final byte[] BYTE_ARRAY = new byte[]{0xA, 0xB, 0xC, 0xD};

    private static ServiceTemplate createServiceTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setVspId("someIdd");
        serviceTemplate.setVersion(new Version("best"));
        serviceTemplate.setName("name");
        serviceTemplate.setContentData(BYTE_ARRAY);
        return serviceTemplate;
    }

    @Test
    void shouldReturnNonEmptyEntityType() {
        ServiceTemplateEntity entity =
            new ServiceTemplateEntity();
        assertTrue(StringUtils.isNoneEmpty(entity.getEntityType()));
    }

    @Test
    void shouldHaveFirstClassCitizenIdEqualToVspId() {
        ServiceTemplateEntity entity =
            new ServiceTemplateEntity(createServiceTemplate());
        assertEquals(entity.getId(), entity.getFirstClassCitizenId());
    }

    @Test
    void serviceTemplateGetterShouldReturnCorrectData() throws IOException {
        ServiceTemplate serviceTemplate = createServiceTemplate();
        ServiceTemplateEntity entity =
            new ServiceTemplateEntity(serviceTemplate);

        ServiceTemplate actual = entity.getServiceTemplate();

        assertEquals(serviceTemplate.getVspId(), actual.getVspId());
        assertEquals(serviceTemplate.getVersion(), actual.getVersion());
        assertEquals(serviceTemplate.getName(), actual.getName());
        assertArrayEquals(IOUtils.toByteArray(serviceTemplate.getContent()), IOUtils.toByteArray(actual.getContent()));
    }

    @Test
    void shouldFailOnNullContentBytesSupplied() {
        ServiceTemplate serviceTemplateMock = mock(ServiceTemplate.class);
        given(serviceTemplateMock.getContent()).willAnswer(invocation -> {
            throw new IOException("Test exception");
        });
        assertThrows(SdcRuntimeException.class, () -> {
            new ServiceTemplateEntity(serviceTemplateMock);
        });
    }
}
