/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

/*
 * Copyright © 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import static org.junit.Assert.assertEquals;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;

public class ElementToComponentMonitoringUploadConvertorTest {

    private static final String ENTITY_ID = "entityId1";
    private static final String ARTIFACT_NAME ="testArtifact.zip";

    private ElementToComponentMonitoringUploadConvertor converter = new ElementToComponentMonitoringUploadConvertor();


    @Test
    public void shouldConvertElementToComponentMonitoringUploadEntity() throws IOException {
        ZusammenElement elementToConvert = new ZusammenElement();
        elementToConvert.setElementId(new Id(ENTITY_ID));
        elementToConvert.setInfo(createInfo());
        InputStream inputStreamMock = IOUtils.toInputStream("some test data for my input stream", "UTF-8");
        elementToConvert.setData(inputStreamMock);
        ComponentMonitoringUploadEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID, result.getId());
        assertEquals(ARTIFACT_NAME, result.getArtifactName());
        assertEquals("SNMP_TRAP", result.getType().name());
    }

    @Test
    public void shouldConvertElementInfoToComponentMonitoringUploadEntity() {
        ElementInfo elementToConvert = new ElementInfo();
        elementToConvert.setId(new Id(ENTITY_ID));
        elementToConvert.setInfo(createInfo());
        ComponentMonitoringUploadEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID, result.getId());
        assertEquals(ARTIFACT_NAME, result.getArtifactName());
        assertEquals("SNMP_TRAP", result.getType().name());
    }


    private Info createInfo() {
        Info info = new Info();
        info.setName("SNMP_TRAP");
        info.addProperty("artifactName", ARTIFACT_NAME);
        return info;
    }

}
