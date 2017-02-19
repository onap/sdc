package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.interfaces.Validator;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.core.validation.types.MessageContainer;
import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public abstract class ValidatorBaseTest {

  private static GlobalValidationContext createGlobalContextFromPath(String path) {
    GlobalValidationContext globalValidationContext = new GlobalValidationContext();
    Map<String, byte[]> contentMap = getContentMapByPath(path);
    if (contentMap == null) {
      return null;
    }
    contentMap.entrySet().stream()
        .forEach(entry -> globalValidationContext.addFileContext(entry.getKey(), entry.getValue()));

    return globalValidationContext;
  }


  // New test base implementation

  private static Map<String, byte[]> getContentMapByPath(String path) {
    Map<String, byte[]> contentMap = new HashMap<>();
    byte[] fileContent;
    FileInputStream fis;
    URL url = ValidatorBaseTest.class.getResource(path);
    File pathFile = new File(url.getFile());
    File[] files;
    if (pathFile.isDirectory()) {
      files = pathFile.listFiles();
    } else {
      files = new File[]{pathFile};
    }

    if (files == null || files.length == 0) {
      return null;
    }

    for (File file : files) {
      try {
        fis = new FileInputStream(file);
        fileContent = FileUtils.toByteArray(fis);
        contentMap.put(file.getName(), fileContent);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return contentMap;
  }

  public abstract Map<String, MessageContainer> runValidation(String path);

  protected Map<String, MessageContainer> testValidator(Validator validator, String path) {

    GlobalValidationContext globalValidationContext = createGlobalContextFromPath(path);
    validator.validate(globalValidationContext);

    assert globalValidationContext != null;
    return globalValidationContext.getContextMessageContainers();


  }

  protected void validateErrorMessage(String actualMessage, String expected, String... params) {
    Assert.assertEquals(actualMessage.replace("\n", "").replace("\r", ""),
        ErrorMessagesFormatBuilder.getErrorWithParameters(expected, params).replace("\n", "")
            .replace("\r", ""));

  }


}
