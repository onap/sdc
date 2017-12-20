package org.openecomp.sdc.vendorsoftwareproduct.upload.csar;

import org.junit.Test;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.OnboardingManifest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ManifestParsingTest {

  @Test
  public void testSuccessfulParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/ValidTosca.mf")) {
      OnboardingManifest onboardingManifest = new OnboardingManifest(is);
      assertTrue(onboardingManifest.isValid());
      assertEquals(onboardingManifest.getMetadata().size(), 4);
      assertEquals(onboardingManifest.getSources().size(), 5);
    }
  }

  @Test
  public void testNoMetadataParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca1.mf")) {
      OnboardingManifest onboardingManifest = new OnboardingManifest(is);
      assertFalse(onboardingManifest.isValid());
      assertTrue(onboardingManifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_INVALID_LINE.getErrorMessage().substring(0, 10))));
    }
  }

  @Test
  public void testBrokenMDParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca2.mf")) {
      OnboardingManifest onboardingManifest = new OnboardingManifest(is);
      assertFalse(onboardingManifest.isValid());
      assertTrue(onboardingManifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_INVALID_LINE.getErrorMessage().substring(0, 10))));
    }
  }

  @Test
  public void testNoMetaParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca4.mf")) {
      OnboardingManifest onboardingManifest = new OnboardingManifest(is);
      assertFalse(onboardingManifest.isValid());
      assertTrue(onboardingManifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_NO_METADATA.getErrorMessage().substring(0, 10))));
    }
  }


}
