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
 */

package org.openecomp.sdc.healing.healers;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.Mockito.any;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.healing.healers.NetworkPackageHealer;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.versioning.dao.types.Version;
import java.util.ArrayList;
import java.util.Collection;

public class NetworkPackageHealerTest {

  @Mock
  private VendorSoftwareProductInfoDao vspInfoDaoMock;
  @Mock
  private ZusammenAdaptor zusammenAdaptorMock;
  @Mock
  private CandidateService candidateService;

  private NetworkPackageHealer networkPackageHealer;
  private static final String tenant = "dox";

  @Before
  public void init() {
    SessionContextProviderFactory.getInstance().createInterface().create("test", tenant);
    MockitoAnnotations.initMocks(this);
    networkPackageHealer = new NetworkPackageHealer(vspInfoDaoMock, zusammenAdaptorMock, candidateService);
  }

  @After
  public void tearDown(){
    SessionContextProviderFactory.getInstance().createInterface().close();
  }

  @Test
  public void testIsHealingNeeded_Positive() {
    VspDetails vspDetails = new VspDetails("ITEM_ID",new Version());
    vspDetails.setOnboardingMethod("NetworkPackage");
    Mockito.when(vspInfoDaoMock.get(any())).thenReturn(vspDetails);
    Collection<ElementInfo> elementInfos = new ArrayList<>();
    ElementInfo elementInfo = new ElementInfo();
    Info info = new Info();
    info.setName(ElementType.OrchestrationTemplateCandidate.name());
    elementInfo.setInfo(info);
    elementInfos.add(elementInfo);
    Mockito.when(zusammenAdaptorMock.listElementsByName(any(),any(),any(),any())).thenReturn
        (elementInfos);
    Assert.assertEquals(TRUE,networkPackageHealer.isHealingNeeded("ITEM_ID", new Version()));
}

  @Test
  public  void testIsHealingNeeded_Negative() {
    VspDetails vspDetails = new VspDetails("ITEM_ID",new Version());
    vspDetails.setOnboardingMethod("NetworkPackage");
    Mockito.when(vspInfoDaoMock.get(any())).thenReturn(vspDetails);
    Collection<ElementInfo> elementInfos = new ArrayList<>();

    ElementInfo elementInfo = new ElementInfo();
    Info info = new Info();
    info.setName(ElementType.OrchestrationTemplateCandidate.name());
    elementInfo.setInfo(info);
    elementInfos.add(elementInfo);

    ElementInfo elementInfo1 = new ElementInfo();
    Info info1 = new Info();
    info1.setName(ElementType.OrchestrationTemplateCandidateValidationData.name());
    elementInfo1.setInfo(info1);
    elementInfos.add(elementInfo1);

    Mockito.when(zusammenAdaptorMock.listElementsByName(any(),any(),any(),any())).thenReturn
        (elementInfos);
    Assert.assertEquals(FALSE,networkPackageHealer.isHealingNeeded("ITEM_ID", new Version()));
  }

  @Test
  public void testIsHealingNeeded_OnboardingMethod() {
    VspDetails vspDetails = new VspDetails("ITEM_ID",new Version());
    vspDetails.setOnboardingMethod("Manual");
    Mockito.when(vspInfoDaoMock.get(any())).thenReturn(vspDetails);

    Assert.assertEquals(FALSE,networkPackageHealer.isHealingNeeded("ITEM_ID", new Version()));
  }
}
