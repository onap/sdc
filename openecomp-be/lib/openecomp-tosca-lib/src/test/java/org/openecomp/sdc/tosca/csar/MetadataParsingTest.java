package org.openecomp.sdc.tosca.csar;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ETSI_ENTRY_MANIFEST;

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
      assertNull(onboardingToscaMetadata.getMetaEntries().get(TOSCA_META_ENTRY_DEFINITIONS));
    }
  }

  @Test
  public void testValidMetadataFile() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/metadata/Validtosca.meta")) {
      ToscaMetadata onboardingToscaMetadata = OnboardingToscaMetadata.parseToscaMetadataFile(is);
      assertEquals(onboardingToscaMetadata.getMetaEntries().get(TOSCA_META_ENTRY_DEFINITIONS), "Definitions/MainServiceTemplate.yaml");
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
      assertEquals(onboardingToscaMetadata.getMetaEntries().get(TOSCA_META_ENTRY_DEFINITIONS), "Definitions/MainServiceTemplate.yaml");
      assertEquals(onboardingToscaMetadata.getMetaEntries().get(TOSCA_META_ETSI_ENTRY_MANIFEST), "MainServiceTemplate.mf");
      assertEquals(onboardingToscaMetadata.getMetaEntries().get(TOSCA_META_ETSI_ENTRY_CHANGE_LOG), "change.log");
    }

  }
}
