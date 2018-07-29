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

package org.openecomp.sdc.vendorsoftwareproduct.tree;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by SHALOMB on 6/8/2016.
 */
public abstract class TreeBaseTest {

    HeatTreeManager initHeatTreeManager(String inputDirectory) throws URISyntaxException, IOException {

        URL url = Thread.currentThread().getContextClassLoader().getResource(inputDirectory);
        if (url == null) {
            throw new FileNotFoundException("Directory " + inputDirectory + " not found in classpath");
        }

        File inputDir = new File(url.toURI());

        File[] files = inputDir.listFiles();
        if (files == null) {
            throw new IllegalArgumentException("Directory " + inputDirectory + " does not contain files");
        }

        HeatTreeManager heatTreeManager = new HeatTreeManager();
        for (File inputFile : files) {

            String path = inputDirectory.replace("/", File.separator) + File.separator + inputFile.getName();
            try (InputStream inputStream = FileUtils.loadFileToInputStream(path)) {
                heatTreeManager.addFile(inputFile.getName(), inputStream);
            }
        }

        return heatTreeManager;
    }
}