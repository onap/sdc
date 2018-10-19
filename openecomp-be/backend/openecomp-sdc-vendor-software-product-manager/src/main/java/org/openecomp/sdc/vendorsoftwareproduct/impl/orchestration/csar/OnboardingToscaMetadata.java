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

import com.google.common.collect.ImmutableList;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.SEPERATOR_MF_ATTRIBUTE;

public class OnboardingToscaMetadata {

  private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingToscaMetadata.class);
  private String entryDefinitionsPath;

  public OnboardingToscaMetadata(InputStream is) {
    parseToscaMetadataFile(is);
  }

  private void parseToscaMetadataFile(InputStream st) {

    try {
      ImmutableList<String> metadataLines = readAllLines(st);

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
      throw new RuntimeException("Invalid TOSCA Metadata file", e);

    }

  }

  private ImmutableList<String> readAllLines(InputStream is) throws IOException {
    ImmutableList.Builder<String> builder = ImmutableList.<String>builder();
    try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8.newDecoder());
        BufferedReader bufferedReader = new BufferedReader(reader);) {
      for (; ; ) {
        String line = bufferedReader.readLine();
        if (line == null) {
          break;
        }
        builder.add(line);
      }
    }
    return builder.build();
  }

  public String getEntryDefinitionsPath() {
    return entryDefinitionsPath;
  }
}

