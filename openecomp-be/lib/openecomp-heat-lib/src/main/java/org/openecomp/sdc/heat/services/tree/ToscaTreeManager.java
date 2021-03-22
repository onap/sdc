/*
 * Copyright © 2017-2018 European Support Limited
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
 **/
package org.openecomp.sdc.heat.services.tree;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;

/**
 * The type Tosca tree manager.
 */
public class ToscaTreeManager {

    private FileContentHandler csarContentMap = new FileContentHandler();
    private HeatStructureTree tree = new HeatStructureTree();
    private Map<String, HeatStructureTree> fileTreeRef = new HashMap<>();

    /**
     * Add file.
     *
     * @param fileName the file name
     * @param content  the content
     */
    public void addFile(String fileName, byte[] content) {
        if (!fileName.equals(SdcCommon.CSAR_MANIFEST_NAME)) {
            csarContentMap.addFile(fileName, content);
        }
    }

    /**
     * Create tree.
     */
    public void createTree() {
        for (Map.Entry<String, byte[]> fileEntry : csarContentMap.getFiles().entrySet()) {
            String[] splitFilename = getFullFileNameAsArray(fileEntry.getKey());
            addFileToTree(splitFilename, 0, splitFilename[0], tree);
        }
    }

    private void addFileToTree(String[] splitFilename, int startIndex, String fullFileName, HeatStructureTree parent) {
        fileTreeRef.putIfAbsent(fullFileName, new HeatStructureTree());
        HeatStructureTree heatStructureTree = fileTreeRef.get(fullFileName);
        heatStructureTree.setFileName(splitFilename[startIndex]);
        if (startIndex < splitFilename.length - 1) {
            addFileToTree(splitFilename, startIndex + 1, getFullFileName(fullFileName, splitFilename[startIndex + 1]), heatStructureTree);
        }
        parent.addHeatStructureTreeToNestedHeatList(heatStructureTree);
    }

    /**
     * Add errors.
     *
     * @param validationErrors the validation errors
     */
    public void addErrors(Map<String, List<ErrorMessage>> validationErrors) {
        validationErrors.entrySet().stream().filter(entry -> Objects.nonNull(fileTreeRef.get(entry.getKey())))
            .forEach(entry -> entry.getValue().forEach(error -> fileTreeRef.get(entry.getKey()).addErrorToErrorsList(error)));
    }

    private String getFullFileName(String parentFullName, String fileName) {
        return parentFullName + File.separator + fileName;
    }

    private String[] getFullFileNameAsArray(String filename) {
        if (filename.contains("/")) {
            return filename.split("/");
        }
        return filename.split(Pattern.quote(File.separator));
    }

    /**
     * Gets tree.
     *
     * @return the tree
     */
    public HeatStructureTree getTree() {
        return tree;
    }
}
