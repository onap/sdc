/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 * ================================================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.healing.healers;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.Mockito.any;
import static org.openecomp.sdc.datatypes.model.ElementType.OrchestrationTemplateCandidateValidationData;
import static org.openecomp.sdc.datatypes.model.ElementType.OrchestrationTemplateValidationData;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.healing.healers.NetworkPackageHealer;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.utils.CandidateEntityBuilder;
import org.openecomp.sdc.versioning.dao.types.Version;
import java.util.ArrayList;
import java.util.Collection;

public class NetworkPackageHealerTest {

    private static final String ITEM_ID = "ITEM_ID";
    private static final String ANY = "ANY";
    private static final String UTF_8 = "UTF-8";
    private static final String FILE_SUFFIX = "fileSuffix";
    private static final String FILE_NAME = "fileName";
    private static final String OTHER_THAN_ANY = "OTHER_THAN_ANY";
    private static final String NETWORK_PACKAGE = "NetworkPackage";

    @Mock
    private VendorSoftwareProductInfoDao vspInfoDaoMock;
    @Mock
    private ZusammenAdaptor zusammenAdaptorMock;
    @Mock
    private Element element;
    @Mock
    private Element subElement;
    @Mock
    private ElementInfo elementInfo;
    @Mock
    private Element subElement2;
    @Mock
    private CandidateEntityBuilder candidateEntityBuilder;
    @Mock
    private OrchestrationTemplateCandidateData orchestrationData;

    private NetworkPackageHealer networkPackageHealer;
    private static final String tenant = "dox";

    @Before
    public void init() {
        SessionContextProviderFactory.getInstance().createInterface().create("test", tenant);
        MockitoAnnotations.initMocks(this);
        networkPackageHealer = new NetworkPackageHealer(vspInfoDaoMock, zusammenAdaptorMock, candidateEntityBuilder);
    }

    @After
    public void tearDown() {
        SessionContextProviderFactory.getInstance().createInterface().close();
    }

    @Test
    public void testIsHealingNeeded_Positive() {
        VspDetails vspDetails = new VspDetails(ITEM_ID, new Version());
        vspDetails.setOnboardingMethod(NETWORK_PACKAGE);
        Mockito.when(vspInfoDaoMock.get(any())).thenReturn(vspDetails);
        Collection<ElementInfo> elementInfos = new ArrayList<>();
        ElementInfo elementInfo = new ElementInfo();
        Info info = new Info();
        info.setName(ElementType.OrchestrationTemplateCandidate.name());
        elementInfo.setInfo(info);
        elementInfos.add(elementInfo);
        Mockito.when(zusammenAdaptorMock.listElementsByName(any(), any(), any(), any())).thenReturn(elementInfos);
        Assert.assertEquals(TRUE, networkPackageHealer.isHealingNeeded(ITEM_ID, new Version()));
    }

    @Test
    public void testIsHealingNeeded_Negative() {
        VspDetails vspDetails = new VspDetails(ITEM_ID, new Version());
        vspDetails.setOnboardingMethod(NETWORK_PACKAGE);
        Mockito.when(vspInfoDaoMock.get(any())).thenReturn(vspDetails);
        Collection<ElementInfo> elementInfos = new ArrayList<>();

        ElementInfo elementInfo = new ElementInfo();
        Info info = new Info();
        info.setName(ElementType.OrchestrationTemplateCandidate.name());
        elementInfo.setInfo(info);
        elementInfos.add(elementInfo);

        ElementInfo elementInfo1 = new ElementInfo();
        Info info1 = new Info();
        info1.setName(OrchestrationTemplateCandidateValidationData.name());
        elementInfo1.setInfo(info1);
        elementInfos.add(elementInfo1);

        Mockito.when(zusammenAdaptorMock.listElementsByName(any(), any(), any(), any())).thenReturn(elementInfos);
        Assert.assertEquals(FALSE, networkPackageHealer.isHealingNeeded(ITEM_ID, new Version()));
    }

    @Test
    public void testIsHealingNeeded_OnboardingMethod() {
        VspDetails vspDetails = new VspDetails(ITEM_ID, new Version());
        vspDetails.setOnboardingMethod("Manual");
        Mockito.when(vspInfoDaoMock.get(any())).thenReturn(vspDetails);

        Assert.assertEquals(FALSE, networkPackageHealer.isHealingNeeded(ITEM_ID, new Version()));
    }

    @Test
    public void shouldHeal() throws Exception {
        Mockito.when(zusammenAdaptorMock.getElementByName(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Optional.of(element));
        Mockito.when(zusammenAdaptorMock.saveElement(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(element);
        Mockito.when(element.getSubElements()).thenReturn(Collections.singletonList(subElement));
        Mockito.when(
                zusammenAdaptorMock.getElementInfoByName(Mockito.any(), Mockito.any(), Mockito.isNull(), Mockito.any())).thenReturn(Optional.of(elementInfo));
        ArrayList<Element> subElements = new ArrayList<>();
        Mockito.when(element.getData()).thenReturn(IOUtils.toInputStream(ANY, UTF_8));
        Mockito.when(subElement2.getData()).thenReturn(IOUtils.toInputStream(ANY, UTF_8));
        subElements.add(subElement);
        subElements.add(subElement2);
        Mockito.when(zusammenAdaptorMock.listElementData(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(subElements);
        Info info = new Info();
        info.setName(OrchestrationTemplateCandidateValidationData.name());
        Mockito.when(element.getInfo()).thenReturn(info);
        Id id = new Id();
        Mockito.when(element.getElementId()).thenReturn(id);
        Info subinfo = new Info();
        Info subinfo2 = new Info();
        Map<String, Object> props2 = new HashMap<>();
        props2.put(FILE_SUFFIX, ANY);
        props2.put(FILE_NAME, ANY);
        subinfo2.setProperties(props2);
        subinfo2.setName(ElementType.OrchestrationTemplateCandidateContent.name());
        Map<String, Object> props = new HashMap<>();
        props.put(FILE_SUFFIX, ANY);
        props.put(FILE_NAME, OTHER_THAN_ANY);
        subinfo.setProperties(props);
        subinfo.setName(OrchestrationTemplateValidationData.name());
        Mockito.when(subElement.getInfo()).thenReturn(subinfo);
        Mockito.when(subElement2.getInfo()).thenReturn(subinfo2);
        Mockito.when(candidateEntityBuilder.buildCandidateEntityFromZip(Mockito.isNull(), Mockito.any(), Mockito.any(), Mockito.isNull())).thenReturn(orchestrationData);
        Mockito.when(orchestrationData.getFilesDataStructure()).thenReturn(ANY);

        networkPackageHealer.heal(ITEM_ID, Version.valueOf("1.1"));

        Mockito.verify(zusammenAdaptorMock, Mockito.times(3)).saveElement(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }
}
