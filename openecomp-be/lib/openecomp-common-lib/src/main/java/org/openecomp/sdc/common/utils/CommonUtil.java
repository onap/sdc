/*
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.common.utils;

import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class CommonUtil {

    private CommonUtil() {
        // prevent instantiation
    }

    /**
     * Reads the files from the zip AND validates zip is flat (no folders).
     *
     * @param type             the onboarding type
     * @param uploadedFileData zip file bytes
     * @return FileContentHandler if input is valid and has no folders
     * @throws IOException when the zip could not be read
     */
    public static FileContentHandler validateAndUploadFileContent(final OnboardingTypesEnum type, final byte[] uploadedFileData) throws IOException {
        final Pair<FileContentHandler, List<String>> pair;
        try {
            pair = getFileContentMapFromOrchestrationCandidateZip(uploadedFileData);
        } catch (final ZipException e) {
            throw new IOException(e);
        }
        if (isFileOriginFromZip(type.toString())) {
            validateNoFolders(pair.getRight());
        }
        return pair.getLeft();
    }

    /**
     * Extracts the zip in memory and build a pair of {@link FileContentHandler} and the zip folder list. The {@link FileContentHandler} will only
     * contain the files, not the folders.
     *
     * @param uploadFileData the zip file to extract
     * @return a pair of {@link FileContentHandler} only with the zip files and a list of the zip folders.
     * @throws ZipException when there was a problem during the zip reading
     */
    public static Pair<FileContentHandler, List<String>> getFileContentMapFromOrchestrationCandidateZip(byte[] uploadFileData) throws ZipException {
        final Map<String, byte[]> zipFileMap = ZipUtils.readZip(uploadFileData, true);
        final List<String> folderList = new ArrayList<>();
        final FileContentHandler mapFileContent = new FileContentHandler();
        zipFileMap.forEach((key, value) -> {
            if (value == null) {
                folderList.add(key);
            } else {
                mapFileContent.addFile(key, value);
            }
        });
        return new ImmutablePair<>(mapFileContent, folderList);
    }

    /**
     * Extracts the zip in memory and build the {@link FileContentHandler}.
     *
     * @param zipFile the zip file to extract
     * @return The {@link FileContentHandler} based on the zip content
     * @throws ZipException when there was a problem during the zip reading
     */
    public static FileContentHandler getZipContent(final byte[] zipFile) throws ZipException {
        final Map<String, byte[]> zipFileMap = ZipUtils.readZip(zipFile, true);
        final FileContentHandler fileContentHandler = new FileContentHandler();
        zipFileMap.forEach((key, value) -> {
            if (value == null) {
                fileContentHandler.addFolder(key);
            } else {
                fileContentHandler.addFile(key, value);
            }
        });
        return fileContentHandler;
    }

    private static void validateNoFolders(List<String> folderList) {
        if (CollectionUtils.isNotEmpty(folderList)) {
            throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
                .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage()).withCategory(ErrorCategory.APPLICATION).build());
        }
    }

    private static boolean validateFilesExtensions(Set<String> allowedExtensions, FileContentHandler files) {
        for (String fileName : files.getFileList()) {
            if (!allowedExtensions.contains(FilenameUtils.getExtension(fileName))) {
                return false;
            }
        }
        return true;
    }

    public static boolean validateAllFilesYml(FileContentHandler files) {
        Set<String> allowedExtensions = new HashSet<>(Arrays.asList("yml", "yaml"));
        return validateFilesExtensions(allowedExtensions, files);
    }

    public static boolean isFileOriginFromZip(String fileOrigin) {
        return Objects.nonNull(fileOrigin) && fileOrigin.equalsIgnoreCase(OnboardingTypesEnum.ZIP.toString());
    }

    public static <T> Optional<T> createObjectUsingSetters(Object objectCandidate, Class<T> classToCreate) throws Exception {
        return org.onap.sdc.tosca.services.CommonUtil.createObjectUsingSetters(objectCandidate, classToCreate);
    }

    public static Map<String, Object> getObjectAsMap(Object obj) {
        return org.onap.sdc.tosca.services.CommonUtil.getObjectAsMap(obj);
    }

    public static <K, V> boolean isMultimapEmpty(Multimap<K, V> obj) {
        return Objects.isNull(obj) || obj.isEmpty();
    }
}
