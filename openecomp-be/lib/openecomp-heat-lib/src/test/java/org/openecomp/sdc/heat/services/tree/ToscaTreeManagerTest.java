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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;

public class ToscaTreeManagerTest {

    private static final String BASE_DIR = "/mock/toscaTree/";
    private static final String IN = "in";
    private static final String OUT = "out";
    private static final String EXPECTED_TREE_FILE = "/expectedTree.json";
    private ToscaTreeManager toscaTreeManager = new ToscaTreeManager();

    @Test
    public void testTreeWithDiffFileNames() throws IOException {
        String inputDirectory = BASE_DIR + "diffFileNames/" + IN;
        String outputFileName = BASE_DIR + "diffFileNames/" + OUT + EXPECTED_TREE_FILE;

        testTreeManager(inputDirectory, outputFileName);
    }

    @Test
    public void testDirectoriesWithSimilarNameUnderDifferentRoots() throws IOException {
        String inputDirectory = BASE_DIR + "similarDirectoryName/" + IN;
        String outputFileName = BASE_DIR + "similarDirectoryName/" + OUT + EXPECTED_TREE_FILE;

        testTreeManager(inputDirectory, outputFileName);
    }

    @Test
    public void testTwoFilesUnderSameDirectory() throws IOException {
        String inputDirectory = BASE_DIR + "twoFilesUnderSameDirectory/" + IN;
        String outputFileName = BASE_DIR + "twoFilesUnderSameDirectory/" + OUT + EXPECTED_TREE_FILE;

        testTreeManager(inputDirectory, outputFileName);
    }

    private void testTreeManager(String inputDirectory, String outputFileName) throws IOException {
        initTreeManager(inputDirectory);
        toscaTreeManager.createTree();
        HeatStructureTree tree = toscaTreeManager.getTree();

        validateToscaTree(outputFileName, tree);
    }

    private void validateToscaTree(String outputFileName, HeatStructureTree tree) throws IOException {
        File expectedTreeFile = new File(this.getClass().getResource(outputFileName).getFile());

        String expectedTree;
        try (FileInputStream fis = new FileInputStream(expectedTreeFile)) {
            expectedTree = new String(FileUtils.toByteArray(fis));
        }
        Assert.assertNotNull(expectedTree);
        expectedTree = expectedTree.trim().replace("\r", "");
        String actualTree = JsonUtil.object2Json(tree);
        Assert.assertEquals(expectedTree, actualTree);
    }

    private void initTreeManager(String inputDir) {
        String fileName = inputDir.replace("/", File.separator);
        File directory = new File(this.getClass().getResource(inputDir).getFile());

        addFilesToTreeManager(fileName, directory.listFiles());
    }

    private void addFilesToTreeManager(String baseDir, File[] listFiles) {
        for (File file : listFiles) {
            if (file.isDirectory()) {
                addFilesToTreeManager(baseDir, file.listFiles());
            } else {
                toscaTreeManager.addFile(getFileNameWithoutTestDirectory(baseDir, file.getPath()), new byte[2]);
            }
        }
    }

    private String getFileNameWithoutTestDirectory(String baseDir, String fileName) {
        return fileName.split(Pattern.quote(baseDir) + Pattern.quote(File.separator))[1];
    }
}
