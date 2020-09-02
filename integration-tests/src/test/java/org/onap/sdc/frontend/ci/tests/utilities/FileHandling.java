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

package org.onap.sdc.frontend.ci.tests.utilities;

import com.aventstack.extentreports.Status;
import com.clearspring.analytics.util.Pair;
import org.apache.commons.io.FileUtils;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.utils.general.OnboardingUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.onap.sdc.frontend.ci.tests.execute.setup.ExtentTestActions;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.testng.AssertJUnit.assertTrue;

public class FileHandling {

    //	------------------yaml parser methods----------------------------
    public static Map<?, ?> parseYamlFile(String filePath) throws Exception {
        Yaml yaml = new Yaml();
        File file = new File(filePath);
        InputStream inputStream = new FileInputStream(file);
        Map<?, ?> map = new HashMap<>();
        map = (Map<?, ?>) yaml.load(inputStream);
        return map;
    }

    /**
     * The method return map fetched objects by pattern from yaml file
     *
     * @param yamlFile
     * @param pattern
     * @return
     * @throws Exception
     */
    public static Map<String, Object> parseYamlFileToMapByPattern(File yamlFile, String pattern) throws Exception {
        Map<?, ?> yamlFileToMap = FileHandling.parseYamlFile(yamlFile.toString());
        Map<String, Object> objectMap = getObjectMapByPattern(yamlFileToMap, pattern);
        return objectMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getObjectMapByPattern(Map<?, ?> parseUpdetedEnvFile, String pattern) {
        Map<String, Object> objectMap = null;

        Object objectUpdetedEnvFile = parseUpdetedEnvFile.get(pattern);
        if (objectUpdetedEnvFile instanceof HashMap) {
            objectMap = (Map<String, Object>) objectUpdetedEnvFile;
        }
        return objectMap;
    }


    public static Map<String, DataTypeDefinition> parseDataTypesYaml(String filePath) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, DataTypeDefinition> dataTypesMap = (Map<String, DataTypeDefinition>) parseYamlFile(filePath);
        return dataTypesMap;
    }
//	-------------------------------------------------------------------------------------------------


    /**
     * @param folder, folder name under "Files" folder
     * @return path to given folder from perspective of working directory or sdc-vnfs repository
     */
    public static String getFilePath(String folder) {
        String filepath = System.getProperty("filePath");
        boolean isFilePathEmptyOrNull = (filepath == null || filepath.isEmpty());

        // return folder from perspective of working directory ( in general for nightly run from Linux, should already contain "Files" directory )
        return FileHandling.getBasePath() + "src/test/resources/Files" + File.separator + folder + File.separator;
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

    public static String getCiFilesPath() {
        return getBasePath() + "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "ci";
    }

    public static String getConfFilesPath() {
        return getCiFilesPath() + File.separator + "conf" + File.separator;
    }

    public static String getTestSuitesFilesPath() {
        return getCiFilesPath() + File.separator + "testSuites" + File.separator;
    }

    public static String getVnfRepositoryPath() {
        return getFilePath("VNFs");
    }

    public static String getUpdateVSPVnfRepositoryPath() {
        return getFilePath("UpdateVSP");
    }

    public static File getConfigFile(String configFileName) throws Exception {
        File configFile = new File(FileHandling.getBasePath() + File.separator + "conf" + File.separator + configFileName);
        if (!configFile.exists()) {
            configFile = new File(FileHandling.getConfFilesPath() + configFileName);
        }
        return configFile;
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

    public static List<String> filterFileNamesListFromFolder(String filepath, String extension) {
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

                filenames.removeAll(OnboardingUtils.excludeXnfList);

                return filenames;
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

    public static List<String> getZipFileNamesFromFolder(String filepath) {
        return filterFileNamesListFromFolder(filepath, ".zip");
    }

    public static int countFilesInZipFile(String[] artifactsArr, String reqExtension) {
        int fileCounter = 0;
        for (String artifact : artifactsArr) {
            String extensionFile = artifact.substring(artifact.lastIndexOf(".") + 1, artifact.length());
            if (extensionFile.equals(reqExtension)) {
                fileCounter++;
            }
        }
        return fileCounter;
    }


    /**
     * @return last modified file name from default directory
     * @throws Exception
     */
    public static synchronized File getLastModifiedFileNameFromDir() throws Exception {
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
            assertTrue("File not found under directory " + dirPath, false);
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

    public static void createDirectory(String directoryPath) {
        File directory = new File(String.valueOf(directoryPath));
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }


    /**
     * The method append data to existing file, if file not exists - create it
     *
     * @param pathToFile
     * @param text
     * @param leftSpaceCount
     * @throws IOException
     */
    public static synchronized void writeToFile(File pathToFile, Object text, Integer leftSpaceCount) throws IOException {

        BufferedWriter bw = null;
        FileWriter fw = null;
        if (!pathToFile.exists()) {
            createEmptyFile(pathToFile);
        }
        try {
            fw = new FileWriter(pathToFile, true);
            bw = new BufferedWriter(fw);
            StringBuilder sb = new StringBuilder();
            if (leftSpaceCount > 0) {
                for (int i = 0; i < leftSpaceCount; i++) {
                    sb.append(" ");
                }
            }
            bw.write(sb.toString() + text);
            bw.newLine();
            bw.close();
            fw.close();
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Unable to write to flie " + pathToFile);
        }
    }

    public static synchronized void writeToFile(File pathToFile, Map<String, Pair<String, Object>> dataMap, Integer leftSpaceCount) throws IOException {

        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            if (!pathToFile.exists()) {
                createEmptyFile(pathToFile);
            }
            fw = new FileWriter(pathToFile, true);
            bw = new BufferedWriter(fw);
            StringBuilder sb = new StringBuilder();
            if (leftSpaceCount > 0) {
                for (int i = 0; i < leftSpaceCount; i++) {
                    sb.append(" ");
                }
            }
            for (Map.Entry<String, Pair<String, Object>> entry : dataMap.entrySet()) {
                Object record = ArtifactUIUtils.getFormatedData(entry.getKey(), entry.getValue().right);
                bw.write(sb.toString() + record);
                bw.newLine();
            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Unable to write to flie " + pathToFile);
        }
    }

    public static void deleteLastDowloadedFiles(List<File> files) throws IOException {
        for (File file : files) {
            File fileToDelete = new File(Config.instance().getDownloadAutomationFolder() + file.getName());
            fileToDelete.delete();
        }
    }

    public static void cleanCurrentDownloadDir() throws IOException {
        try {
            ExtentTestActions.log(Status.INFO, "Cleaning directory " + SetupCDTest.getWindowTest().getDownloadDirectory());
            System.gc();
            FileUtils.cleanDirectory(new File(SetupCDTest.getWindowTest().getDownloadDirectory()));
        } catch (Exception e) {

        }
    }

    public static boolean isFileDownloaded(String downloadPath, String fileName) {
        File dir = new File(downloadPath);
        File[] dir_contents = dir.listFiles();
        return Arrays.stream(dir_contents).anyMatch(file -> file.getName().equals(fileName));
    }

    public static String getMD5OfFile(File file) throws IOException {
        String content = FileUtils.readFileToString(file);
        String md5 = GeneralUtility.calculateMD5Base64EncodedByString(content);
        return md5;
    }

    public static File createEmptyFile(String fileToCreate) {
        File file = new File(fileToCreate);
        try {
            if (file.exists()) {
                deleteFile(file);
            }
            file.createNewFile();
            SetupCDTest.getExtendTest().log(Status.INFO, "Create file " + fileToCreate);
        } catch (IOException e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Failed to create file " + fileToCreate);
            e.printStackTrace();
        }
        return file;
    }

    public static File createEmptyFile(File fileToCreate) {
        try {
            if (fileToCreate.exists()) {
                deleteFile(fileToCreate);
            }
            fileToCreate.createNewFile();
            SetupCDTest.getExtendTest().log(Status.INFO, "Create file " + fileToCreate);
        } catch (IOException e) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Failed to create file " + fileToCreate);
            e.printStackTrace();
        }
        return fileToCreate;
    }

    public static void deleteFile(File file) {

        try {
            if (file.exists()) {
                file.deleteOnExit();
                SetupCDTest.getExtendTest().log(Status.INFO, "File " + file.getName() + "has been deleted");
            } else {
                SetupCDTest.getExtendTest().log(Status.INFO, "Failed to delete file " + file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * get file list from directory by extension array
     *
     * @param directory
     * @param okFileExtensions
     * @return
     */
    public static List<File> getHeatAndHeatEnvArtifactsFromZip(File directory, String[] okFileExtensions) {

        List<File> fileList = new ArrayList<>();
        File[] files = directory.listFiles();

        for (String extension : okFileExtensions) {
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(extension)) {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    private static final int BUFFER_SIZE = 4096;

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
//         iterates over entries in the zip file
        while (entry != null) {
            String entryName;
            if (System.getProperty("os.name").contains("Windows")) {
                entryName = entry.getName().replace("/", "\\" + File.separator);
            } else {
                entryName = entry.getName();
            }
            String filePath = destDirectory + entryName;
            String currPath = destDirectory;
            String[] dirs = entryName.split("\\" + File.separator);
            String currToken;
            for (int i = 0; i < dirs.length; ++i) {
                currToken = dirs[i];
                if (!entry.isDirectory() && i == dirs.length - 1) {
                    extractFile(zipIn, filePath);
                } else {
                    if (currPath.endsWith(File.separator)) {
                        currPath = currPath + currToken;
                    } else {
                        currPath = currPath + File.separator + currToken;
                    }
//                     if the entry is a directory, make the directory
                    File dir = new File(currPath);
                    dir.mkdir();
                }
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    public static int getFileCountFromDefaulDownloadDirectory() {
        return new File(SetupCDTest.getWindowTest().getDownloadDirectory()).listFiles().length;
    }


    public static String getKeyByValueFromPropertyFormatFile(String fullPath, String key) {
        Properties prop = new Properties();
        InputStream input = null;
        String value = null;
        try {
            input = new FileInputStream(fullPath);
            prop.load(input);
            value = (prop.getProperty(key));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return value.replaceAll("\"", "");
    }
}
