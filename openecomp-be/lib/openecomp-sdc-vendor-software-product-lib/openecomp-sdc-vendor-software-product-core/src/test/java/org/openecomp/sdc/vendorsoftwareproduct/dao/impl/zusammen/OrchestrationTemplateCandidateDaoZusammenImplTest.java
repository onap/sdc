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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.versioning.dao.types.Version;

class OrchestrationTemplateCandidateDaoZusammenImplTest {

    private static final String VSP_ID = "vsp-id";
    private static final Version VERSION = new Version("version-id");
    private static final Id CANDIDATE_ID = new Id("candidate-id");

    private final ZusammenAdaptor zusammenAdaptor = mock(ZusammenAdaptor.class);
    private final OrchestrationTemplateCandidateDaoZusammenImpl dao = new OrchestrationTemplateCandidateDaoZusammenImpl(zusammenAdaptor);

    @BeforeEach
    void setUp() {
        SessionContextProviderFactory.getInstance().createInterface().create("test", "dox");
        ElementInfo candidateInfo = new ElementInfo();
        candidateInfo.setId(CANDIDATE_ID);
        doReturn(Optional.of(candidateInfo)).when(zusammenAdaptor)
            .getElementInfoByName(any(), any(), isNull(), eq(ElementType.OrchestrationTemplateCandidate.name()));
    }

    @AfterEach
    void tearDown() {
        SessionContextProviderFactory.getInstance().createInterface().close();
    }

    @Test
    void getReadsTheCandidateAndItsChildrenInOneTreeRead() {
        doReturn(Optional.of(candidateTree())).when(zusammenAdaptor).getElementTree(any(), any(), eq(CANDIDATE_ID), eq(1));

        OrchestrationTemplateCandidateData candidate = dao.get(VSP_ID, VERSION).orElseThrow();

        assertEquals("{\"structure\":true}", candidate.getFilesDataStructure());
        assertEquals(ByteBuffer.wrap(bytes("package")), candidate.getContentData());
        assertEquals("zip", candidate.getFileSuffix());
        assertEquals("vsp", candidate.getFileName());
        assertEquals("{\"validation\":true}", candidate.getValidationData());
        verify(zusammenAdaptor, never()).getElement(any(), any(), any());
    }

    @Test
    void getInfoReadsTheChildrenInOneTreeReadWithoutTheirContent() {
        doReturn(Optional.of(candidateTree())).when(zusammenAdaptor).getElementTree(any(), any(), eq(CANDIDATE_ID), eq(1));

        OrchestrationTemplateCandidateData candidate = dao.getInfo(VSP_ID, VERSION).orElseThrow();

        assertEquals("zip", candidate.getFileSuffix());
        assertEquals("vsp", candidate.getFileName());
        assertEquals("{\"validation\":true}", candidate.getValidationData());
        assertNull(candidate.getContentData());
        verify(zusammenAdaptor, never()).getElement(any(), any(), any());
    }

    @Test
    void getAndGetInfoAreEmptyWhenTheCandidateElementIsMissing() {
        doReturn(Optional.empty()).when(zusammenAdaptor).getElementTree(any(), any(), eq(CANDIDATE_ID), eq(1));

        assertFalse(dao.get(VSP_ID, VERSION).isPresent());
        assertFalse(dao.getInfo(VSP_ID, VERSION).isPresent());
    }

    private static ZusammenElement candidateTree() {
        ZusammenElement content = element(ElementType.OrchestrationTemplateCandidateContent, "package");
        content.getInfo().addProperty("fileSuffix", "zip");
        content.getInfo().addProperty("fileName", "vsp");
        ZusammenElement candidate = element(ElementType.OrchestrationTemplateCandidate, "{\"structure\":true}");
        candidate.setElementId(CANDIDATE_ID);
        candidate.addSubElement(content);
        candidate.addSubElement(element(ElementType.OrchestrationTemplateCandidateValidationData, "{\"validation\":true}"));
        return candidate;
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
