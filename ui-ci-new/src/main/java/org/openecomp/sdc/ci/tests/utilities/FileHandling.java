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

package org.openecomp.sdc.ci.tests.utilities;

import static org.testng.AssertJUnit.fail;

import com.aventstack.extentreports.Status;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;

public class FileHandling {

    /**
     * @param folder, folder name under "Files" folder
     * @return path to given folder from perspective of working directory or sdc-vnfs repository
     */
    public static String getFilePath(String folder) {
        String filepath = System.getProperty("filePath");
        boolean isFilePathEmptyOrNull = (filepath == null || filepath.isEmpty());

        // return folder from perspective of sdc-vnfs repository
        if (isFilePathEmptyOrNull && (System.getProperty("os.name").contains("Windows") || System.getProperty("os.name").contains("Mac"))) {
            return FileHandling.getResourcesFilesPath() + folder + File.separator;
        }

        // return folder from perspective of working directory ( in general for nightly run from Linux, should already contain "Files" directory )
        return FileHandling.getBasePath() + "Files" + File.separator + folder + File.separator;
    }

    public static String getBasePath() {
        return System.getProperty("user.dir") + File.separator;
    }

    public static String getSdcVnfsPath() {
        String vnfsPath = System.getProperty("vnfs.path");
        if (vnfsPath != null && !vnfsPath.isEmpty()) {
            return vnfsPath;
        }
        return getBasePath() + Paths.get("..", "..", "sdc-vnfs").toString();
    }

    public static String getResourcesFilesPath() {
        return getSdcVnfsPath() + File.separator + "ui-tests" + File.separator + "Files" + File.separator;
    }

    public static String getVnfRepositoryPath() {
        return getFilePath("VNFs");
    }

    public static Object[] filterFileNamesFromFolder(String filepath, String extension) {
        try {
            File dir = new File(filepath);
            List<String> filenames = new ArrayList<String>();

            FilenameFilter extensionFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(extension);
                }
            };

            if (dir.isDirectory()) {
                for (File file : dir.listFiles(extensionFilter)) {
                    filenames.add(file.getName());
                }
                return filenames.toArray();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getArtifactsFromZip(String filepath, String zipFilename) {
        try {
            ZipFile zipFile = new ZipFile(filepath + File.separator + zipFilename);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            List<String> artifactNames = new ArrayList<String>();

            while (entries.hasMoreElements()) {
                ZipEntry nextElement = entries.nextElement();
                if (!nextElement.isDirectory()) {
                    if (!nextElement.getName().equals("MANIFEST.json")) {
                        String name = nextElement.getName();
                        artifactNames.add(name);
                    }
                }
            }
            zipFile.close();
            // convert list to array
            return artifactNames.toArray(new String[0]);
        } catch (ZipException zipEx) {
            System.err.println("Error in zip file named : " + zipFilename);
            zipEx.printStackTrace();
        } catch (IOException e) {
            System.err.println("Unhandled exception : ");
            e.printStackTrace();
        }

        return null;

    }

    /**
     * @return last modified file name from default directory
     */
    public static synchronized File getLastModifiedFileNameFromDir() {
        return getLastModifiedFileNameFromDir(SetupCDTest.getWindowTest().getDownloadDirectory());
    }

    /**
     * @param dirPath
     * @return last modified file name from dirPath directory
     */
    public static synchronized File getLastModifiedFileNameFromDir(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null) {
            fail("File not found under directory " + dirPath);
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (files[i].isDirectory()) {
                continue;
            }
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    public static void deleteDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        if (dir.exists()) {
            try {
                FileUtils.cleanDirectory(dir);
            } catch (IllegalArgumentException e) {
                System.out.println("Failed to clean " + dir);
            } catch (IOException e) {
                System.out.println("Failed to clean " + dir);
            }
        }
    }

    public static void cleanCurrentDownloadDir() {
        try {
            ExtentTestActions.log(Status.INFO, "Cleaning directory " + SetupCDTest.getWindowTest().getDownloadDirectory());
            System.gc();
            FileUtils.cleanDirectory(new File(SetupCDTest.getWindowTest().getDownloadDirectory()));
        } catch (Exception e) {

        }
    }

}
