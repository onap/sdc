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

package org.openecomp.sdc.tosca.csar;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.ToscaMetaEntry.ETSI_ENTRY_MANIFEST;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;



public class MetadataParsingTest {

  @Test
  public void testNoEntryDefinitions() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/metadata/Invalidtosca.meta")) {
      ToscaMetadata onboardingToscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(is);
      assertFalse(onboardingToscaMetadata.isValid());
      assertNull(onboardingToscaMetadata.getMetaEntries().get(ENTRY_DEFINITIONS.getName()));
    }
  }

  @Test
  public void testValidMetadataFile() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/metadata/Validtosca.meta")) {
      ToscaMetadata onboardingToscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(is);
      assertEquals("Definitions/MainServiceTemplate.yaml", onboardingToscaMetadata.getMetaEntries().get(
          ENTRY_DEFINITIONS.getName()));
    }

  }

  @Test
  public void testInvalidMetadataFileEmptyKey() throws IOException {
    try (InputStream is = getClass()
            .getResourceAsStream("/vspmanager.csar/metadata/InvalidtoscaEmptyKey.meta")) {
      ToscaMetadata onboardingToscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(is);
      assertFalse(onboardingToscaMetadata.isValid());
    }
  }

  @Test
  public void testInvalidMetadataFileEmptyValue() throws IOException {
    try (InputStream is = getClass()
            .getResourceAsStream("/vspmanager.csar/metadata/InvalidtoscaEmptyValue.meta")) {
      ToscaMetadata onboardingToscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(is);
      assertFalse(onboardingToscaMetadata.isValid());
    }
  }

  @Test
  public void testValidETSIMetadataFile() throws IOException {
    try (InputStream is = getClass()
            .getResourceAsStream("/vspmanager.csar/metadata/ValidETSItosca.meta")) {
      ToscaMetadata onboardingToscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(is);
      assertEquals("Definitions/MainServiceTemplate.yaml", onboardingToscaMetadata.getMetaEntries().get(
          ENTRY_DEFINITIONS.getName()));
      assertEquals("MainServiceTemplate.mf", onboardingToscaMetadata.getMetaEntries().get(ETSI_ENTRY_MANIFEST.getName()));
      assertEquals("change.log", onboardingToscaMetadata.getMetaEntries().get(ETSI_ENTRY_CHANGE_LOG.getName()));
    }

  }
}
