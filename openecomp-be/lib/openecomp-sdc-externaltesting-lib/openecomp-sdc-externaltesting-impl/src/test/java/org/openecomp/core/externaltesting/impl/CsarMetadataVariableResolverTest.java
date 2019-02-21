/*
 * Copyright © 2019 iconectiv
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

package org.openecomp.core.externaltesting.impl;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.externaltesting.api.TestExecutionRequest;
import org.openecomp.core.externaltesting.api.TestExecutionRequestItem;
import org.openecomp.core.externaltesting.api.TestParameterValue;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.versioning.VersioningManager;

import java.io.FileInputStream;
import java.util.ArrayList;

public class CsarMetadataVariableResolverTest {

  @Mock
  private VersioningManager versioningManager;

  @Mock
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Mock
  private OrchestrationTemplateCandidateManager candidateManager;

  @Before
  public void init() {

  }

  @Test
  public void testResolverResolves() throws Exception {
    MockitoAnnotations.initMocks(this);
    CsarMetadataVariableResolver resolver = new CsarMetadataVariableResolver(versioningManager,
        vendorSoftwareProductManager, candidateManager);
    resolver.init();

    TestExecutionRequest req = new TestExecutionRequest();
    req.setTests(new ArrayList<>());

    TestExecutionRequestItem doesNotResolve = new TestExecutionRequestItem();
    doesNotResolve.setTestId("computeflavors");

    Assert.assertFalse("Should not resolve empty test", resolver.resolvesVariablesForRequest(doesNotResolve));


    TestExecutionRequestItem resolves = new TestExecutionRequestItem();
    resolves.setTestId("computeflavors");
    resolves.setParameterValues(new ArrayList<>());
    TestParameterValue v = new TestParameterValue();
    v.setId(CsarMetadataVariableResolver.VSP_ID);
    v.setValue("some uuid here");
    resolves.getParameterValues().add(v);

    TestParameterValue v1 = new TestParameterValue();
    v1.setId(CsarMetadataVariableResolver.VSP_VERSION);
    v1.setValue("some uuid here");
    resolves.getParameterValues().add(v1);

    TestParameterValue v2 = new TestParameterValue();
    v2.setId(CsarMetadataVariableResolver.CSAR_PREFIX + "MainServiceTemplate.yaml");
    resolves.getParameterValues().add(v2);

    Assert.assertTrue("Should resolve populated computeFlavors test", resolver.resolvesVariablesForRequest(resolves));

    resolver.resolve(resolves);

    byte[] zip = IOUtils.toByteArray(new FileInputStream("src/test/data/csar.zip"));
    resolver.processArchive(resolves, zip);
    Assert.assertNotNull("resolved yaml", resolves.getContentItems());
  }
}
