/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.common.zip;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.common.zip.exception.ZipSlipException;

class ZipUtilsTest {

    private static final String ZIP_SLIP_LINUX_ZIP = "zip-slip/zip-slip-linux.zip";
    private static final String ZIP_SLIP_WINDOWS_ZIP = "zip-slip/zip-slip-windows.zip";
    private static final ClassLoader CLASS_LOADER = ZipUtilsTest.class.getClassLoader();

    @Test
    void testZipSlipInRead() {
        final byte[] windowsZipBytes;
        final byte[] linuxZipBytes;
        try {
            final InputStream linuxZipAsStream = CLASS_LOADER.getResourceAsStream(ZIP_SLIP_LINUX_ZIP);
            final InputStream windowsZipAsStream = CLASS_LOADER.getResourceAsStream(ZIP_SLIP_WINDOWS_ZIP);
            if (linuxZipAsStream == null || windowsZipAsStream == null) {
                fail("Could not load the zip slip files");
            }
            linuxZipBytes = IOUtils.toByteArray(linuxZipAsStream);
            windowsZipBytes = IOUtils.toByteArray(windowsZipAsStream);
        } catch (final IOException e) {
            fail("Could not load the required zip slip files", e);
            return;
        }
        try {
            ZipUtils.readZip(linuxZipBytes, true);
            fail("Zip slip should be detected");
        } catch (final ZipException ex) {
            assertThat("Expected ZipSlipException", ex, is(instanceOf(ZipSlipException.class)));
        }
        try {
            ZipUtils.readZip(windowsZipBytes, true);
            fail("Zip slip should be detected");
        } catch (final ZipException ex) {
            assertThat("Expected ZipSlipException", ex, is(instanceOf(ZipSlipException.class)));
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void testZipSlipInUnzipLinux() throws IOException {
        final Path tempDirectoryLinux = Files.createTempDirectory("zipSlipLinux" + System.currentTimeMillis());
        try {
            final Path linuxZipPath;
            try {
                linuxZipPath = Paths.get(CLASS_LOADER.getResource(ZIP_SLIP_LINUX_ZIP).toURI());
            } catch (final URISyntaxException e) {
                fail("Could not load the required zip slip files", e);
                return;
            }
            try {
                ZipUtils.unzip(linuxZipPath, tempDirectoryLinux);
                fail("Zip slip should be detected");
            } catch (final ZipException ex) {
                assertThat("At least one of the zip files should throw ZipSlipException", ex, is(instanceOf(ZipSlipException.class)));
            }
        } finally {
            FileUtils.deleteDirectory(tempDirectoryLinux.toFile());
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testZipSlipInUnzipWindows() throws IOException {
        final Path tempDirectoryWindows = Files.createTempDirectory("zipSlipWindows" + System.currentTimeMillis());
        try {
            final Path windowsZipPath;
            try {
                windowsZipPath = Paths.get(CLASS_LOADER.getResource(ZIP_SLIP_WINDOWS_ZIP).toURI());
            } catch (final URISyntaxException e) {
                fail("Could not load the required zip slip files", e);
                return;
            }
            try {
                ZipUtils.unzip(windowsZipPath, tempDirectoryWindows);
                fail("Zip slip should be detected");
            } catch (final ZipException ex) {
                assertThat("At least one of the zip files should throw ZipSlipException", ex, is(instanceOf(ZipSlipException.class)));
            }
        } finally {
            FileUtils.deleteDirectory(tempDirectoryWindows.toFile());
        }
    }

    @Test
    void testUnzipAndZip() throws IOException, ZipException {
        final Path unzipTempPath = Files.createTempDirectory("testUnzip").toRealPath();
        final Path zipTempPath = Files.createTempDirectory("testZip").toRealPath();
        final Path testZipPath;
        try {
            try {
                testZipPath = Paths.get(CLASS_LOADER.getResource("zip/extract-test.zip").toURI());
                ZipUtils.unzip(testZipPath, unzipTempPath);
            } catch (final URISyntaxException e) {
                fail("Could not load the required zip file", e);
                return;
            }
            final Set<Path> expectedPaths = new HashSet<>();
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "rootFile1.txt"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "rootFileNoExtension"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "EmptyFolder"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "SingleLvlFolder"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "SingleLvlFolder", "singleLvlFolderFile.txt"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "SingleLvlFolder", "singleLvlFolderFileNoExtension"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "TwoLvlFolder"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "TwoLvlFolder", "twoLvlFolderFile.txt"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "TwoLvlFolder", "twoLvlFolderFileNoExtension"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "TwoLvlFolder", "SingleLvlFolder"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "TwoLvlFolder", "SingleLvlFolder", "singleLvlFolderFile.txt"));
            expectedPaths.add(Paths.get(unzipTempPath.toString(), "TwoLvlFolder", "SingleLvlFolder", "singleLvlFolderFileNoExtension"));
            final AtomicLong actualPathCount = new AtomicLong(0);
            try (final Stream<Path> stream = Files.walk(unzipTempPath)) {
                stream.filter(path -> !unzipTempPath.equals(path)).forEach(actualPath -> {
                    actualPathCount.getAndIncrement();
                    assertThat("Unzipped file should be in the expected list", actualPath, isIn(expectedPaths));
                });
            }
            assertThat("The number of unzipped files should be as expected", actualPathCount.get(), is((long) expectedPaths.size()));
            final Path zipFilePath = zipTempPath.resolve("testzip.zip");
            ZipUtils.createZipFromPath(unzipTempPath, zipFilePath);
            final Map<String, byte[]> fileMap = ZipUtils.readZip(zipFilePath.toFile(), true);
            //matching the folder pattern of the readZip
            final Set<String> expectedPathStringSet = expectedPaths.stream().map(path -> {
                final Path relativePath = unzipTempPath.relativize(path);
                return path.toFile().isDirectory() ? relativePath.toString() + File.separator : relativePath.toString();
            }).collect(Collectors.toSet());
            assertThat("The number of zipped files should be as expected", fileMap, aMapWithSize(expectedPathStringSet.size()));
            fileMap.keySet().forEach(s -> {
                assertThat("File in zip package should be in the expected list", s, isIn(expectedPathStringSet));
            });
        } finally {
            FileUtils.deleteDirectory(unzipTempPath.toFile());
            FileUtils.deleteDirectory(zipTempPath.toFile());
        }
    }
}
