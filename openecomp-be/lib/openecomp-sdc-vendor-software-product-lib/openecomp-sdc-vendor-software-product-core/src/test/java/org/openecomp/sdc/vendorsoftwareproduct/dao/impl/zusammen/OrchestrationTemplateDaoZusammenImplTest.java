/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

class OrchestrationTemplateDaoZusammenImplTest {

    private static final String VSP_ID = "vsp-id";
    private static final Version VERSION = new Version("version-id");
    private static final Id VSP_MODEL_ID = new Id("vsp-model-id");
    private static final Id TEMPLATE_ID = new Id("template-id");

    private final ZusammenAdaptor zusammenAdaptor = mock(ZusammenAdaptor.class);
    private final OrchestrationTemplateDaoZusammenImpl dao = new OrchestrationTemplateDaoZusammenImpl(zusammenAdaptor);

    @BeforeEach
    void setUp() {
        SessionContextProviderFactory.getInstance().createInterface().create("test", "dox");
        doReturn(Optional.of(info(VSP_MODEL_ID))).when(zusammenAdaptor)
            .getElementInfoByName(any(), any(), isNull(), eq(ElementType.VspModel.name()));
        doReturn(Optional.of(info(TEMPLATE_ID))).when(zusammenAdaptor)
            .getElementInfoByName(any(), any(), eq(VSP_MODEL_ID), eq(ElementType.OrchestrationTemplate.name()));
    }

    @AfterEach
    void tearDown() {
        SessionContextProviderFactory.getInstance().createInterface().close();
    }

    @Test
    void getReadsTheTemplateAndItsChildrenInOneTreeRead() {
        ZusammenElement validation = element(ElementType.OrchestrationTemplateValidationData, "{\"validation\":true}");
        validation.getInfo().addProperty("fileSuffix", "zip");
        validation.getInfo().addProperty("fileName", "vsp");
        ZusammenElement template = element(ElementType.OrchestrationTemplate, "package");
        template.addSubElement(validation);
        template.addSubElement(element(ElementType.OrchestrationTemplateStructure, "{\"structure\":true}"));
        doReturn(Optional.of(template)).when(zusammenAdaptor).getElementTree(any(), any(), eq(TEMPLATE_ID), eq(1));

        OrchestrationTemplateEntity entity = dao.get(VSP_ID, VERSION);

        assertEquals(ByteBuffer.wrap(bytes("package")), entity.getContentData());
        assertEquals("zip", entity.getFileSuffix());
        assertEquals("vsp", entity.getFileName());
        assertEquals("{\"validation\":true}", entity.getValidationData());
        assertEquals("{\"structure\":true}", entity.getFilesDataStructure());
        verify(zusammenAdaptor, never()).getElement(any(), any(), any());
        verify(zusammenAdaptor, never()).getElementByName(any(), any(), any(), any());
    }

    @Test
    void getIsEmptyWhenTheTemplateElementIsMissing() {
        doReturn(Optional.empty()).when(zusammenAdaptor).getElementTree(any(), any(), eq(TEMPLATE_ID), eq(1));

        OrchestrationTemplateEntity entity = dao.get(VSP_ID, VERSION);

        assertNull(entity.getContentData());
        assertNull(entity.getFileSuffix());
    }

    private static ElementInfo info(Id id) {
        ElementInfo elementInfo = new ElementInfo();
        elementInfo.setId(id);
        return elementInfo;
    }

    private static ZusammenElement element(ElementType type, String data) {
        ZusammenElement element = new ZusammenElement();
        Info info = new Info();
        info.setName(type.name());
        element.setInfo(info);
        element.setData(new ByteArrayInputStream(bytes(data)));
        return element;
    }

    private static byte[] bytes(String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }
}
