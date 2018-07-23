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

package org.openecomp.sdc.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

public class CommonUtil {
  private static final String DEFAULT = "default";
  private static final String _DEFAULT = "_default";

  private CommonUtil() {
    // prevent instantiation
  }

  public static FileContentHandler validateAndUploadFileContent(OnboardingTypesEnum type,
                                                                byte[] uploadedFileData)
      throws IOException {
    return getFileContentMapFromOrchestrationCandidateZipAndValidateNoFolders(type,
        uploadedFileData);
  }

  /**
   * Gets files out of the zip AND validates zip is flat (no folders)
   *
   * @param uploadFileData zip file
   * @return FileContentHandler if input is valid and has no folders
   */
  private static FileContentHandler getFileContentMapFromOrchestrationCandidateZipAndValidateNoFolders(
      OnboardingTypesEnum type, byte[] uploadFileData)
      throws IOException {
    Pair<FileContentHandler, List<String>> pair =
        getFileContentMapFromOrchestrationCandidateZip(uploadFileData);

    if (isFileOriginFromZip(type.toString())) {
      validateNoFolders(pair.getRight());
    }

    return pair.getLeft();
  }

  public static Pair<FileContentHandler, List<String>> getFileContentMapFromOrchestrationCandidateZip(
      byte[] uploadFileData)
      throws IOException {
    ZipEntry zipEntry;
    List<String> folderList = new ArrayList<>();
    FileContentHandler mapFileContent = new FileContentHandler();
    try (ByteArrayInputStream in = new ByteArrayInputStream(uploadFileData);
         ZipInputStream inputZipStream = new ZipInputStream(in)) {
      byte[] fileByteContent;
      String currentEntryName;

      while ((zipEntry = inputZipStream.getNextEntry()) != null) {
        assertEntryNotVulnerable(zipEntry);
        currentEntryName = zipEntry.getName();
        fileByteContent = FileUtils.toByteArray(inputZipStream);

        int index = lastIndexFileSeparatorIndex(currentEntryName);
        if (index != -1) {
          folderList.add(currentEntryName);
        }
        if (isFile(currentEntryName)) {
          mapFileContent.addFile(currentEntryName, fileByteContent);
        }
      }

    } catch (RuntimeException exception) {
      throw new IOException(exception);
    }

    return new ImmutablePair<>(mapFileContent, folderList);
  }

  private static void assertEntryNotVulnerable(ZipEntry entry) throws ZipException {
    if (entry.getName().contains("../")) {
      throw new ZipException("Path traversal attempt discovered.");
    }
  }

  private static boolean isFile(String currentEntryName) {
    return !(currentEntryName.endsWith("\\") || currentEntryName.endsWith("/"));
  }

  private static void validateNoFolders(List<String> folderList) {
    if (CollectionUtils.isNotEmpty(folderList)) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder())
          .withMessage(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
          .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
          .withCategory(ErrorCategory.APPLICATION).build());
    }
  }

  private static int lastIndexFileSeparatorIndex(String filePath) {
    int length = filePath.length() - 1;

    for (int i = length; i >= 0; i--) {
      char currChar = filePath.charAt(i);
      if (currChar == '/' || currChar == File.separatorChar || currChar == File.pathSeparatorChar) {
        return i;
      }
    }
    // if we've reached to the start of the string and didn't find file separator - return -1
    return -1;
  }

  private static boolean validateFilesExtensions(Set<String> allowedExtensions, FileContentHandler
      files) {
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
    return Objects.nonNull(fileOrigin)
        && fileOrigin.equalsIgnoreCase(OnboardingTypesEnum.ZIP.toString());
  }

  public static Set<String> getClassFieldNames(Class<? extends Object> classType) {
    Set<String> fieldNames = new HashSet<>();
    Arrays.stream(classType.getDeclaredFields()).forEach(field -> fieldNames.add(field.getName()));

    return fieldNames;
  }

  public static <T> Optional<T> createObjectUsingSetters(Object objectCandidate,
                                                         Class<T> classToCreate)
      throws Exception {
    if (Objects.isNull(objectCandidate)) {
      return Optional.empty();
    }

    Map<String, Object> objectAsMap = getObjectAsMap(objectCandidate);
    T result = classToCreate.newInstance();

    Field[] declaredFields = classToCreate.getDeclaredFields();
    for( Field field : declaredFields){
      if(isComplexClass(field)){
        Optional<?> objectUsingSetters =
            createObjectUsingSetters(objectAsMap.get(field.getName()), field.getType());
        if( objectUsingSetters.isPresent()){
          objectAsMap.remove(field.getName());
          objectAsMap.put(field.getName(), objectUsingSetters.get());
        }
      }
    }
    BeanUtils.populate(result, objectAsMap);

    return Optional.of(result);
  }

  private static boolean isComplexClass(Field field) {
    return !field.getType().equals(Map.class)
        && !field.getType().equals(String.class)
        && !field.getType().equals(Integer.class)
        && !field.getType().equals(Float.class)
        && !field.getType().equals(Double.class)
        && !field.getType().equals(Set.class)
        && !field.getType().equals(Object.class)
        && !field.getType().equals(List.class);
  }

  public static Map<String, Object> getObjectAsMap(Object obj) {
    Map<String, Object> objectAsMap = obj instanceof Map ? (Map<String, Object>) obj
        : new ObjectMapper().convertValue(obj, Map.class);

    if (objectAsMap.containsKey(DEFAULT)) {
      Object defaultValue = objectAsMap.get(DEFAULT);
      objectAsMap.remove(DEFAULT);
      objectAsMap.put(_DEFAULT, defaultValue);
    }
    return objectAsMap;
  }

    public static <K, V> boolean isMultimapEmpty(Multimap<K, V> obj) {
        return Objects.isNull(obj) || obj.isEmpty();
    }
}
