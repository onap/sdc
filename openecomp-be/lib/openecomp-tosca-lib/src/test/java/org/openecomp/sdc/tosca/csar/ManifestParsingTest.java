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

package org.openecomp.sdc.tosca.csar;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.common.errors.Messages;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class ManifestParsingTest {

  private Manifest manifest;

  @Before
  public void setUp(){
    manifest = new SOL004ManifestOnboarding();
  }

  @Test
  public void testSuccessfulParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/ValidTosca.mf")) {
      manifest.parse(is);
      assertTrue(manifest.isValid());
      assertEquals(manifest.getMetadata().size(), 4);
      assertEquals(manifest.getSources().size(), 5);
    }
  }

  @Test
  public void testNoMetadataParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca1.mf")) {
      manifest.parse(is);
      assertFalse(manifest.isValid());
      assertTrue(manifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_INVALID_LINE.getErrorMessage().substring(0, 10))));
    }
  }

  @Test
  public void testBrokenMDParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca2.mf")) {
     manifest.parse(is);
      assertFalse(manifest.isValid());
      assertTrue(manifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_INVALID_LINE.getErrorMessage().substring(0, 10))));
    }
  }

  @Test
  public void testNoMetaParsing() throws IOException {
    try (InputStream is = getClass()
        .getResourceAsStream("/vspmanager.csar/manifest/InvalidTosca4.mf")) {
      manifest.parse(is);
      assertFalse(manifest.isValid());
      assertTrue(manifest.getErrors().stream().anyMatch(error -> error
          .contains(Messages.MANIFEST_NO_METADATA.getErrorMessage().substring(0, 10))));
    }
  }

  @Test
  public void testSuccessfulNonManoParsing() throws IOException {
    try (InputStream is = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/ValidNonManoTosca.mf")) {
      manifest.parse(is);
      assertTrue(manifest.isValid());
      assertEquals(manifest.getMetadata().size(), 4);
      assertEquals(manifest.getSources().size(), 5);
      assertEquals(manifest.getNonManoSources().size(), 2);
    }
  }

  @Test
  public void testFailfulNonManoParsing() throws IOException {
    try (InputStream is = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/InValidNonManoTosca.mf")) {
      manifest.parse(is);
      assertFalse(manifest.isValid());
    }
  }

  @Test
  public void testFailfulNonManoParsingWithGarbadge() throws IOException {
    try (InputStream is = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/InvalidTocsaNonManoGarbadgeAtEnd.mf")) {
      manifest.parse(is);
      assertFalse(manifest.isValid());
    }
  }

  @Test
  public void testParseManifestWithNoFile() throws IOException {
    try (InputStream is = getClass()
            .getResourceAsStream("/vspmanager.csar/manifest/SOME_WRONG_FILE")) {
      manifest.parse(is);
      assertFalse(manifest.isValid());
    }
  }
}
