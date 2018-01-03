package org.openecomp.sdc.validation.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class UploadValidationManagerImplTest {

  private static final String RESOURCE_PATH ="/org/openecomp/sdc/validation/impl";
  private static final String RESOURCE_TYPE="heat";

  @Test
  public void testValidateFileWithValidFile() throws IOException {
    String TEST_FILE="/HEAT_VALID.zip";
    UploadValidationManager fileManager=new UploadValidationManagerImpl();
    ValidationFileResponse fileResponse=fileManager.validateFile(RESOURCE_TYPE,
        new FileInputStream(fileResourcePath(RESOURCE_PATH
            + TEST_FILE)));
    Assert.assertEquals(fileResponse.getValidationData().getImportStructure().getHeat()
        .isEmpty(),false);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testValidateFileWithEmptyFolder() throws IOException {
    String TEST_FILE="/HEAT_WITH_EMPTY_FOLDER.zip";
    UploadValidationManager fileManager=new UploadValidationManagerImpl();
    fileManager.validateFile(RESOURCE_TYPE,new FileInputStream(fileResourcePath(RESOURCE_PATH
        + TEST_FILE)));
  }

  @Test
  public void testValidateFileWithFilesInFolder() throws IOException {
    String TEST_FILE="/HEAT_WITH_FOLDER.zip";
    UploadValidationManager fileManager=new UploadValidationManagerImpl();
    ValidationFileResponse fileResponse=fileManager.validateFile(RESOURCE_TYPE,
        new FileInputStream(fileResourcePath(RESOURCE_PATH
            + TEST_FILE)));
    Assert.assertEquals(fileResponse.getValidationData().getImportStructure().
        getHeat().isEmpty(),false);
  }

  private String fileResourcePath(String path){
    URL url = UploadValidationManagerImplTest.class.getResource(path);
    return new File(url.getFile()).getAbsolutePath();
  }
}
