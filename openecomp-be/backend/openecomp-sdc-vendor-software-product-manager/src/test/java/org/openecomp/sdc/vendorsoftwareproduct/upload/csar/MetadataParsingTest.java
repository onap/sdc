package org.openecomp.sdc.vendorsoftwareproduct.upload.csar;

import org.junit.Test;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.OnboardingToscaMetadata;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class MetadataParsingTest {

  @Test
  public void testNoEntryDefinitions() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/metadata/Invalidtosca.meta")) {
      OnboardingToscaMetadata onboardingToscaMetadata = new OnboardingToscaMetadata(is);
      assertEquals(onboardingToscaMetadata.getEntryDefinitionsPath(), null);
    }
  }

  @Test
  public void testValidMetadataFile() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/metadata/Validtosca.meta")) {
      OnboardingToscaMetadata onboardingToscaMetadata = new OnboardingToscaMetadata(is);
      assertEquals(onboardingToscaMetadata.getEntryDefinitionsPath(), "Definitions/MainServiceTemplate.yaml");
    }

  }
}
