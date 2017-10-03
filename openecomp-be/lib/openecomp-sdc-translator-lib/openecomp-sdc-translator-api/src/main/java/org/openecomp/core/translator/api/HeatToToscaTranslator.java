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

package org.openecomp.core.translator.api;


import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;




public interface HeatToToscaTranslator {

  void addManifest(String name, byte[] content);

  void addFile(String name, byte[] content);

  void addFile(String name, InputStream content);

  // return Map, key - file name which has error
  //             value - the error code
  Map<String, List<ErrorMessage>> validate();

  TranslatorOutput translate() throws IOException;

  void addExternalArtifacts(String name, byte[] content);

  void addExternalArtifacts(String name, InputStream content);

}
