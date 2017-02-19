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

package org.openecomp.sdc.heat.services.tree;

import org.openecomp.core.utilities.file.FileContentHandler;

/**
 * The type Heat tree manager util.
 */
public class HeatTreeManagerUtil {
  /**
   * Init heat tree manager heat tree manager.
   *
   * @param fileContentMap the file content map
   * @return the heat tree manager
   */
  public static HeatTreeManager initHeatTreeManager(FileContentHandler fileContentMap) {

    HeatTreeManager heatTreeManager = new HeatTreeManager();
    fileContentMap.getFileList().stream().forEach(
        fileName -> heatTreeManager.addFile(fileName, fileContentMap.getFileContent(fileName)));

    return heatTreeManager;
  }
}
