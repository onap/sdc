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

class EnrichedServiceArtifactEntityTest {

    private static final byte[] BYTE_ARRAY = new byte[]{0xA, 0xB, 0xC, 0xD};

    private static ServiceArtifact createServiceArtifact() {
        ServiceArtifact serviceArtifact = new ServiceArtifact();
        serviceArtifact.setVspId("someIdd");
        serviceArtifact.setVersion(new Version("best"));
        serviceArtifact.setName("name");
        serviceArtifact.setContentData(BYTE_ARRAY);
        return serviceArtifact;
    }

    @Test
    void shouldReturnNonEmptyEntityType() {
        EnrichedServiceArtifactEntity entity =
            new EnrichedServiceArtifactEntity();
        assertTrue(StringUtils.isNoneEmpty(entity.getEntityType()));
    }

    @Test
    void shouldHaveFirstClassCitizenIdEqualToVspId() {
        EnrichedServiceArtifactEntity entity =
            new EnrichedServiceArtifactEntity(createServiceArtifact());
        assertEquals(entity.getId(), entity.getFirstClassCitizenId());
    }

    @Test
    void serviceArtifactGetterShouldReturnCorrectData() throws IOException {
        ServiceArtifact serviceArtifact = createServiceArtifact();
        EnrichedServiceArtifactEntity entity =
            new EnrichedServiceArtifactEntity(serviceArtifact);

        ServiceArtifact actual = entity.getServiceArtifact();

        assertEquals(serviceArtifact.getVspId(), actual.getVspId());
        assertEquals(serviceArtifact.getVersion(), actual.getVersion());
        assertEquals(serviceArtifact.getName(), actual.getName());
        assertArrayEquals(IOUtils.toByteArray(serviceArtifact.getContent()), IOUtils.toByteArray(actual.getContent()));
    }

    @Test
    void shouldFailOnNullContentBytesSupplied() {
        ServiceArtifact serviceArtifactMock = mock(ServiceArtifact.class);
        given(serviceArtifactMock.getContent()).willAnswer(invocation -> {
            throw new IOException("Test exception");
        });
        assertThrows(SdcRuntimeException.class, () -> {
            new EnrichedServiceArtifactEntity(serviceArtifactMock);
        });
    }
}
