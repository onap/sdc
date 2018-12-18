package org.openecomp.sdc.vendorsoftwareproduct.upload.csar;


import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.OnboardingToscaMetadata;
import org.testng.annotations.Test;

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
