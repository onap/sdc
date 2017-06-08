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

package org.openecomp.sdc.vendorsoftwareproduct.tree;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by SHALOMB on 6/8/2016.
 */
public class TreeBaseTest {

  String INPUT_DIR;


  HeatTreeManager initHeatTreeManager() {
    HeatTreeManager heatTreeManager = new HeatTreeManager();

    URL url = Thread.currentThread().getContextClassLoader().getResource(INPUT_DIR);

    File inputDir = null;
    try {
      inputDir = new File(url.toURI());
    } catch (URISyntaxException exception) {
      exception.printStackTrace();
    }
    File[] files = inputDir.listFiles();
    for (File inputFile : files) {
      try {
        heatTreeManager.addFile(inputFile.getName(), FileUtils.loadFileToInputStream(
            INPUT_DIR.replace("/", File.separator) + File.separator + inputFile.getName()));
      } catch (Exception e) {
        e.printStackTrace();
        throw e;
      }
    }
    return heatTreeManager;
  }
}
