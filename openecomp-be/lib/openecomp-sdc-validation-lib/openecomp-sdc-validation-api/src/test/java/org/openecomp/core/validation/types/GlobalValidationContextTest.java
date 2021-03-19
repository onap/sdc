/*
 * Copyright © 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.core.validation.types;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import org.junit.jupiter.api.Test;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GlobalValidationContextTest {
  private static String filename = "testName";
  private static String yaml1 = "one.yaml";
  private static String yaml2 = "two.yaml";
  private static String text1 = "one.txt";
  private static String content = "testContent";
  private static String message = "The file is corrupted";

  @Test
  public void testAddMessageCode() {
    GlobalValidationContext globalValidationContext = new GlobalValidationContext();
    ErrorMessageCode error = new ErrorMessageCode("Error");
    globalValidationContext.setMessageCode(error);

    assertEquals(error, globalValidationContext.getMessageCode());
  }

  @Test
  public void testAddFileContext() {
    GlobalValidationContext globalValidationContext = new GlobalValidationContext();
    globalValidationContext.addFileContext(filename, content.getBytes());
    Map<String, FileValidationContext> fileContextMap = globalValidationContext.getFileContextMap();

    assertTrue(MapUtils.isNotEmpty(fileContextMap));
    assertTrue(fileContextMap.containsKey(filename));
  }

  @Test
  public void testGetContextMessageContainers() {
    GlobalValidationContext globalValidationContext = new GlobalValidationContext();
    globalValidationContext.addMessage(filename, ErrorLevel.ERROR, message);

    Map<String, MessageContainer> messageContainers =
        globalValidationContext.getContextMessageContainers();

    testIfFileHasMessageContainer(messageContainers);
  }

  @Test
  public void testAddMessage() {
    GlobalValidationContext globalValidationContext = new GlobalValidationContext();
    globalValidationContext.addMessage(filename, ErrorLevel.ERROR, message);

    Map<String, MessageContainer> messageContainers =
        globalValidationContext.getContextMessageContainers();

    testIfFileHasMessageContainer(messageContainers);
    testIfFileHasErrorMessage(messageContainers, 1);
  }

  @Test
  public void testGetFileContent() {
    GlobalValidationContext globalValidationContext = new GlobalValidationContext();

    assertTrue(globalValidationContext.getFileContent(yaml1).isEmpty());

    byte[] bytes = content.getBytes();
    globalValidationContext.addFileContext(yaml1, bytes);

    assertTrue(globalValidationContext.getFileContent(yaml1).get() instanceof InputStream);
  }

  @Test
  public void testGetFiles() {
    GlobalValidationContext globalValidationContext = new GlobalValidationContext();
    byte[] bytes = content.getBytes();
    globalValidationContext.addFileContext(yaml1, bytes);
    globalValidationContext.addFileContext(yaml2, bytes);
    globalValidationContext.addFileContext(text1, bytes);

    testGetFilesByFileType((fileName, globalContext) -> fileName.endsWith(".yaml"),
        2, Arrays.asList(yaml1, yaml2), globalValidationContext);
    testGetFilesByFileType((fileName, globalContext) -> fileName.endsWith(".txt"),
        1, Collections.singletonList(text1), globalValidationContext);

    assertEquals(3, globalValidationContext.getFiles().size());
  }

  private void testGetFilesByFileType(BiPredicate<String, GlobalValidationContext> func,
                                      int expectedFilesNumberToFind,
                                      List<String> expectedFileNames,
                                      GlobalValidationContext globalValidationContext) {
    Collection<String> files = globalValidationContext.files(func);
    assertTrue(CollectionUtils.isNotEmpty(files));
    assertEquals(files.size(), expectedFilesNumberToFind);
    expectedFileNames.forEach(filename -> assertTrue(files.contains(filename)));
  }

  private void testIfFileHasMessageContainer(Map<String, MessageContainer> messageContainers) {
    assertNotNull(messageContainers);
    assertTrue(messageContainers.containsKey(filename));
  }

  private void testIfFileHasErrorMessage(Map<String, MessageContainer> messageContainers,
                                         int expectedErrorsNumber) {
    MessageContainer messageContainer = messageContainers.get(filename);
    assertEquals(messageContainer.getErrorMessageList().size(), expectedErrorsNumber);

    ErrorMessage errorMessage =
        new ErrorMessage(ErrorLevel.ERROR, ErrorLevel.ERROR.toString() + ": " + message);
    assertTrue(messageContainer.getErrorMessageList().contains(errorMessage));
  }
}
