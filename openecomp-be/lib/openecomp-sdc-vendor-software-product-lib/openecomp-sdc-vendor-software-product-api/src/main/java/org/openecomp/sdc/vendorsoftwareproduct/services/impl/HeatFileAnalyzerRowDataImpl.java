/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl;

import org.openecomp.sdc.vendorsoftwareproduct.services.HeatFileAnalyzer;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.AnalyzedZipHeatFiles;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class HeatFileAnalyzerRowDataImpl implements HeatFileAnalyzer {
  private static final String HEAT_IDENTIFIER_REGEX = "^heat_template_version:.*";
  private static final String HEAT_RESOURCES_REGEX = "^resources:\\s*$";
  private static final String HEAT_PARAMETERS_REGEX = "^parameters:\\s*$";
  private static final String HEAT_CONDITIONS_REGEX = "^conditions:\\s*$";
  private static final String HEAT_OUTPUTS_REGEX = "^outputs:\\s*$";
  private static final String HEAT_PARAMETER_GROUP_REGEX = "^parameter_groups:\\s*$";
  private static final String HEAT_DESCRIPTION_REGEX = "^description:\\s*$";
  //allowing spaces at start followed by 'type:' + spaces + any characters + ('.yml' or '.yaml')+
  // spaces
  private static final String HEAT_NESTED_RESOURCE_REGEX = "\\s*type:\\s*\\S*.(yml|yaml)\\s*$";


  private static final String IDENTIFIER = "IDENTIFIER";
  private static final String RESOURCES = "RESOURCES";
  private static final String PARAMETERS = "PARAMETERS";
  private static final String CONDITIONS = "CONDITIONS";
  private static final String OUTPUTS = "OUTPUTS";
  private static final String PARAMETER_GROUP = "PARAMETER_GROUP";
  private static final String DESCRIPTION = "DESCRIPTION";
  private static final String NESTED_PATTERN = "NESTED_PATTERN";

  private final Map<String, Pattern> patterns;

  public HeatFileAnalyzerRowDataImpl() {
    patterns = new HashMap<>();
    patterns.put(IDENTIFIER, Pattern.compile(HEAT_IDENTIFIER_REGEX));
    patterns.put(RESOURCES, Pattern.compile(HEAT_RESOURCES_REGEX));
    patterns.put(PARAMETERS, Pattern.compile(HEAT_PARAMETERS_REGEX));
    patterns.put(CONDITIONS, Pattern.compile(HEAT_CONDITIONS_REGEX));
    patterns.put(OUTPUTS, Pattern.compile(HEAT_OUTPUTS_REGEX));
    patterns.put(PARAMETER_GROUP, Pattern.compile(HEAT_PARAMETER_GROUP_REGEX));
    patterns.put(DESCRIPTION, Pattern.compile(HEAT_DESCRIPTION_REGEX));
    patterns.put(NESTED_PATTERN, Pattern.compile(HEAT_NESTED_RESOURCE_REGEX));
  }

  @Override
  public AnalyzedZipHeatFiles analyzeFilesNotEligibleForModulesFromFileAnalyzer(Map<String, byte[]> files)
      throws IOException {
    AnalyzedZipHeatFiles analyzedZipHeatFiles = new AnalyzedZipHeatFiles();

    for (Map.Entry<String, byte[]> fileData : files.entrySet()) {
      String fileName = fileData.getKey();
      if (!HeatFileAnalyzer.isYamlFile(fileName)) {
        analyzedZipHeatFiles.addOtherNonModuleFile(fileName);
        continue;
      }

      boolean foundHeatIdentifier = false;
      try (InputStream is = new ByteArrayInputStream(fileData.getValue());
           BufferedReader bfReader = new BufferedReader(new InputStreamReader(is))) {

        String line;
        boolean isResourcesSection = false;
        Set<String> nestedFilesNames = new HashSet<>();
        while ((line = bfReader.readLine()) != null) {
          if (!foundHeatIdentifier && isMatch(patterns.get(IDENTIFIER), line)) {
            foundHeatIdentifier = true;
            analyzedZipHeatFiles.addModuleFile(fileName);
            if (isResourcesSection) // it means the identifier is located after the resources
            // section
            {
              break;
            }
          } else if (isMatch(patterns.get(RESOURCES), line)) {
            isResourcesSection = true;
          } else if (isResourceSectionEnd(line, isResourcesSection)) {
            if (foundHeatIdentifier) {
              break;
            }
          } else if (isResourcesSection) {
            Optional<String> optionalNestedFileName = fetchNestedFileName(line);
            optionalNestedFileName
                .ifPresent(nestedFilesNames::add);
          }
        }
        analyzedZipHeatFiles.addNestedFiles(fetchFileNamesToReturn(nestedFilesNames,
            foundHeatIdentifier));
      }
    }

    return analyzedZipHeatFiles;
  }

  private Optional<String> fetchNestedFileName(String line) {
    if (isMatch(patterns.get(NESTED_PATTERN), line)) {
      String trimmedLine = line.trim();
      String nestedFileName = trimmedLine
          .substring(trimmedLine.indexOf("type:") + "type:".length(), trimmedLine.length())
          .trim();
      return Optional.of(nestedFileName);
    }
    return Optional.empty();
  }

  private Set<String> fetchFileNamesToReturn(Set<String> filesNamesToReturn,
                                             boolean foundHeatIdentifier) {
    if (!foundHeatIdentifier) {
      return new HashSet<>();
    } else {
      return filesNamesToReturn;
    }
  }

  private boolean isResourceSectionEnd(String line, boolean isResourcesSection) {
    return isResourcesSection && isStartOfNonResourcesHeatSection(line);
  }

  private boolean isStartOfNonResourcesHeatSection(String line) {
    return isMatch(patterns.get(PARAMETERS), line) ||
        isMatch(patterns.get(CONDITIONS), line) ||
        isMatch(patterns.get(OUTPUTS), line) ||
        isMatch(patterns.get(PARAMETER_GROUP), line) ||
        isMatch(patterns.get(DESCRIPTION), line);
  }

  private boolean isMatch(Pattern pattern, String line) {
    return pattern.matcher(line).matches();
  }

}
