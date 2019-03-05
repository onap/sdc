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

package org.openecomp.sdc.tosca.csar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import java.io.InputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.tosca.csar.CSARConstants.SEPERATOR_MF_ATTRIBUTE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.TOSCA_META_ENTRY_DEFINITIONS;

public class OnboardingToscaMetadata implements ToscaMetadata{

  private Map<String, String> metaEntries;
  private List<ErrorMessage> errors;

  private OnboardingToscaMetadata(){
    metaEntries = new HashMap<>();
    errors = new ArrayList<>();
  }

  /**
   * Method parses input stream of meta file, only block_0 is parsed, the rest of metadata ignored
   * @param st meta file input stream
   * @return OnboardingToscaMetadata instance
   * @throws IOException
   */
  public static ToscaMetadata parseToscaMetadataFile(InputStream st) throws IOException {
    OnboardingToscaMetadata meta = new OnboardingToscaMetadata();
    List<String> metadataLines = IOUtils.readLines(st, "utf-8");
    for (String line : metadataLines) {
      line = line.trim();
      if (line.isEmpty()) {
        return meta;
      }
      String[] entry = line.split(SEPERATOR_MF_ATTRIBUTE);
      //No empty keys allowed, no empty values allowed
      if (entry.length < 2 || entry[0].isEmpty()) {
        meta.errors.add(new ErrorMessage(ErrorLevel.ERROR, getErrorWithParameters(
                Messages.METADATA_INVALID_ENTRY_DEFINITIONS.getErrorMessage(), line)));
        //want to get all error lines in meta file block_0, no breaking loop
      } else {
        meta.metaEntries.put(entry[0].trim(), entry[1].trim());
      }
    }

    if (!meta.metaEntries.containsKey(TOSCA_META_ENTRY_DEFINITIONS)) {
      meta.errors.add(new ErrorMessage(ErrorLevel.ERROR, getErrorWithParameters(
              Messages.METADATA_NO_ENTRY_DEFINITIONS.getErrorMessage())));
    }
    return meta;
  }

  @Override  public boolean isValid(){
    return errors.isEmpty();
  }

  @Override
  public List<ErrorMessage> getErrors() {
    return  ImmutableList.copyOf(errors);
  }


  @Override
  public Map<String, String> getMetaEntries() {
    if (!isValid()){
      return Collections.emptyMap();
    }
    return ImmutableMap.copyOf(metaEntries);
  }
}

