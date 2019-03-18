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
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.externaltesting.api.VtpTestExecutionRequest;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.versioning.VersioningManager;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.FileInputStream;
import java.util.*;

public class CsarMetadataVariableResolverTest {

  @Mock
  private VersioningManager versioningManager;

  @Mock
  private VendorSoftwareProductManager vendorSoftwareProductManager;

  @Mock
  private OrchestrationTemplateCandidateManager candidateManager;

  @Test
  public void testResolverResolves() throws Exception {
    MockitoAnnotations.initMocks(this);
    CsarMetadataVariableResolver resolver = new CsarMetadataVariableResolver(versioningManager,
        vendorSoftwareProductManager, candidateManager);
    resolver.init();

    VtpTestExecutionRequest doesNotResolve = new VtpTestExecutionRequest();
    Assert.assertFalse("should not resolve empty request", resolver.resolvesVariablesForRequest(doesNotResolve));

    doesNotResolve.setParameters(new HashMap<>());
    Assert.assertFalse("should not resolve empty parameters", resolver.resolvesVariablesForRequest(doesNotResolve));



    VtpTestExecutionRequest resolves = new VtpTestExecutionRequest();
    resolves.setParameters(new HashMap<>());
    resolves.getParameters().put(CsarMetadataVariableResolver.VSP_VERSION, "1.0");
    resolves.getParameters().put(CsarMetadataVariableResolver.VSP_ID, "vspid");
    resolves.getParameters().put(CsarMetadataVariableResolver.CSAR_PREFIX + "MainServiceTemplate.yaml", "");
    Assert.assertTrue("should resolve", resolver.resolvesVariablesForRequest(resolves));

    MultiValueMap<String,Object> fakeRequestBody = new LinkedMultiValueMap<>();

    try {
      resolver.resolve(resolves, fakeRequestBody);
    }
    catch (ExternalTestingException e) {
      // exception expected.
    }

    // test the metadata extraction on a know CSAR zip.
    byte[] zip = IOUtils.toByteArray(new FileInputStream("src/test/data/csar.zip"));
    resolver.processArchive(resolves, fakeRequestBody, zip);
    Assert.assertTrue("body contains file", fakeRequestBody.containsKey("file"));
    LinkedList ll = (LinkedList)fakeRequestBody.get("file");
    Assert.assertEquals("body contains one file", 1, ll.size());
    ByteArrayResource res = (ByteArrayResource)ll.get(0);
    Assert.assertEquals("file should have matching name", "MainServiceTemplate.yaml", res.getFilename());

  }
}
