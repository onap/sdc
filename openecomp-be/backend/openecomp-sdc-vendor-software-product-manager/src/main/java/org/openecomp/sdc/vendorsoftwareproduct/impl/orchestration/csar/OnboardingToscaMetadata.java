/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import java.io.InputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import java.util.List;

import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;
import static org.openecomp.sdc.tosca.csar.CSARConstants.SEPERATOR_MF_ATTRIBUTE;

public class OnboardingToscaMetadata {

  private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingToscaMetadata.class);
  private String entryDefinitionsPath;

  public OnboardingToscaMetadata(InputStream is) throws IOException {
      parseToscaMetadataFile(is);
  }

  private void parseToscaMetadataFile(InputStream st) throws IOException {

    try {
      List<String> metadataLines = IOUtils.readLines(st,"utf-8");

      for (String line : metadataLines) {
        line = line.trim();
        if (line.startsWith(TOSCA_META_ENTRY_DEFINITIONS + SEPERATOR_MF_ATTRIBUTE)) {

          entryDefinitionsPath = line.replaceAll(TOSCA_META_ENTRY_DEFINITIONS + SEPERATOR_MF_ATTRIBUTE, "")
              .trim();
          break;

        }
      }

    } catch (IOException e) {
      LOGGER.error(e.getMessage(), e);
      throw new IOException("Invalid TOSCA Metadata file", e);

    }

  }

  public String getEntryDefinitionsPath() {
    return entryDefinitionsPath;
  }
}

