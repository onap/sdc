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

package org.openecomp.sdc.vendorsoftwareproduct.services;

import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.AnalyzedZipHeatFiles;

import java.io.IOException;
import java.util.Map;

/**
 * @author Avrahamg
 * @since December 21, 2016
 */
public interface HeatFileAnalyzer {

  static boolean isEnvFile(String fileName) {
    return fileName.endsWith(".env");
  }

  static boolean isYamlFile(String fileName) {
    return fileName.endsWith(".yml") || fileName.endsWith(".yaml");
  }

  static boolean isYamlOrEnvFile(String fileName) {
    return isYamlFile(fileName) || isEnvFile(fileName);
  }

  AnalyzedZipHeatFiles analyzeFilesNotEligibleForModulesFromFileAnalyzer(Map<String, byte[]> files) throws IOException;
}
