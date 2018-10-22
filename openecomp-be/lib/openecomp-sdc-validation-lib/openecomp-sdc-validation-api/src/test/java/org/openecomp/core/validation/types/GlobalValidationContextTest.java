package org.openecomp.core.validation.types;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public class GlobalValidationContextTest {

  private static GlobalValidationContext globalValidationContext;
  private static String filename = "testName";
  private static String yaml1 = "one.yaml";
  private static String yaml2 = "two.yaml";
  private static String text1 = "one.txt";
  private static String content = "testContent";
  private static String message = "The file is corrupted";

  @Before
  public void setUp() {
    globalValidationContext = new GlobalValidationContext();
  }

  @Test
  public void testAddMessageCode() {
    ErrorMessageCode error = new ErrorMessageCode("Error");
    globalValidationContext.setMessageCode(error);

    Assert.assertEquals(error, globalValidationContext.getMessageCode());
  }

  @Test
  public void testAddFileContext() {
    globalValidationContext.addFileContext(filename, content.getBytes());
    Map<String, FileValidationContext> fileContextMap = globalValidationContext.getFileContextMap();

    Assert.assertTrue(MapUtils.isNotEmpty(fileContextMap));
    Assert.assertTrue(fileContextMap.containsKey(filename));
  }

  @Test
  public void testGetContextMessageContainers() {
    globalValidationContext.addMessage(filename, ErrorLevel.ERROR, message);

    Map<String, MessageContainer> messageContainers =
        globalValidationContext.getContextMessageContainers();

    testIfFileHasMessageContainer(messageContainers);
  }

  @Test
  public void testAddMessage() {
    globalValidationContext.addMessage(filename, ErrorLevel.ERROR, message);

    Map<String, MessageContainer> messageContainers =
        globalValidationContext.getContextMessageContainers();

    testIfFileHasMessageContainer(messageContainers);
    testIfFileHasErrorMessage(messageContainers, 1);
  }

  @Test
  public void testGetFiles() {
    byte[] bytes = content.getBytes();
    globalValidationContext.addFileContext(yaml1, bytes);
    globalValidationContext.addFileContext(yaml2, bytes);
    globalValidationContext.addFileContext(text1, bytes);

    testGetFilesByFileType((fileName, globalValidationContext) -> fileName.endsWith(".yaml"),
        2, Arrays.asList(yaml1, yaml2));
    testGetFilesByFileType((fileName, globalValidationContext) -> fileName.endsWith(".txt"),
        1, Collections.singletonList(text1));


  }

  private void testGetFilesByFileType(BiPredicate<String, GlobalValidationContext> func,
                                      int expectedFilesNumberToFind,
                                      List<String> expectedFileNames) {
    Collection<String> files = globalValidationContext.files(func);
    Assert.assertTrue(CollectionUtils.isNotEmpty(files));
    Assert.assertEquals(files.size(), expectedFilesNumberToFind);
    expectedFileNames.forEach(filename -> Assert.assertTrue(files.contains(filename)));
  }

  private void testIfFileHasMessageContainer(Map<String, MessageContainer> messageContainers) {
    Assert.assertNotNull(messageContainers);
    Assert.assertTrue(messageContainers.containsKey(filename));
  }

  private void testIfFileHasErrorMessage(Map<String, MessageContainer> messageContainers,
                                         int expectedErrorsNumber) {
    MessageContainer messageContainer = messageContainers.get(filename);
    Assert.assertEquals(messageContainer.getErrorMessageList().size(), expectedErrorsNumber);

    ErrorMessage errorMessage =
        new ErrorMessage(ErrorLevel.ERROR, ErrorLevel.ERROR.toString() + ": " + message);
    Assert.assertTrue(messageContainer.getErrorMessageList().contains(errorMessage));
  }
}
