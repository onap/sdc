package org.openecomp.sdc.validation.util;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.datatypes.configuration.ImplementationConfiguration;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatStructureUtil;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.base.ResourceBaseValidator;
import org.testng.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by TALIO on 2/26/2017.
 */
public class ValidationTestUtil {

  private final static Logger log = (Logger) LoggerFactory.getLogger(ValidationTestUtil.class
      .getName());

  public static GlobalValidationContext createGlobalContextFromPath(String path) {
    GlobalValidationContext globalValidationContext = new GlobalValidationContext();
    Map<String, byte[]> contentMap = getContentMapByPath(path);
    if (contentMap == null) {
      return null;
    }
    contentMap.entrySet()
        .forEach(entry -> globalValidationContext.addFileContext(entry.getKey(), entry.getValue()));

    return globalValidationContext;
  }

  private static Map<String, byte[]> getContentMapByPath(String path) {
    Map<String, byte[]> contentMap = new HashMap<>();
    URL url = ValidationTestUtil.class.getResource(path);
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

      try (FileInputStream fis = new FileInputStream(file)) {
        contentMap.put(file.getName(), FileUtils.toByteArray(fis));
      } catch (IOException e) {
        throw new RuntimeException("Failed to read file: " + file, e);
      }

    }
    return contentMap;
  }

  public static Map<String, MessageContainer> testValidator(Validator validator, String path) {

    GlobalValidationContext globalValidationContext = createGlobalContextFromPath(path);
    validator.validate(globalValidationContext);

    assert globalValidationContext != null;
    return globalValidationContext.getContextMessageContainers();


  }

  public static Map<String, MessageContainer> testValidator(ResourceBaseValidator baseValidator,
                                                            ResourceValidator resourceValidator,
                                                            String resourceTypeToValidate, String path) {

    GlobalValidationContext globalContext = createGlobalContextFromPath(path);
    ManifestContent manifestContent = ValidationUtil.checkValidationPreCondition(globalContext);
    Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(manifestContent);
    Map<String, FileData> fileEnvMap = ManifestUtil.getFileAndItsEnv(manifestContent);

    validateFiles(baseValidator, resourceValidator, globalContext, fileEnvMap, fileTypeMap,
        resourceTypeToValidate);

    assert globalContext != null;
    return globalContext.getContextMessageContainers();


  }

  private static void validateFiles(ResourceBaseValidator baseValidator,
                                    ResourceValidator resourceValidator,
                                    GlobalValidationContext globalContext,
                                    Map<String, FileData> fileEnvMap,
                                    Map<String, FileData.Type> fileTypeMap,
                                    String resourceTypeToValidate) {

    Collection<String> files = globalContext.getFiles();
    for(String fileName : files){
      if(FileData.isHeatFile(fileTypeMap.get(fileName))) {
        HeatOrchestrationTemplate heatOrchestrationTemplate =
            ValidationUtil.checkHeatOrchestrationPreCondition(fileName, globalContext);

        if (Objects.isNull(heatOrchestrationTemplate)) {
          continue;
        }

        ValidationContext validationContext = baseValidator.createValidationContext
            (fileName,
                fileEnvMap.get(fileName) == null ? null : fileEnvMap.get(fileName).getFile(),
                heatOrchestrationTemplate, globalContext);

        validateResources(fileName, resourceValidator, resourceTypeToValidate, validationContext,
            globalContext);
      }
    }
  }

  public static void validateResources(String fileName,
                                       ResourceValidator resourceValidator,
                                       String resourceTypeToValidate,
                                       ValidationContext validationContext,
                                       GlobalValidationContext globalValidationContext){

    HeatOrchestrationTemplate heatOrchestrationTemplate =
        ValidationUtil.checkHeatOrchestrationPreCondition(fileName, globalValidationContext);
    Map<String, Resource> resourcesMap = heatOrchestrationTemplate.getResources();

    if(MapUtils.isEmpty(resourcesMap)){
      return;
    }

    resourcesMap.entrySet()
        .stream()
        .filter(resourceEntry -> isResourceNeedToBeTested(resourceEntry.getValue().getType(), resourceTypeToValidate))
        .forEach(resourceEntry ->
            resourceValidator.validate
                (fileName, resourceEntry, globalValidationContext, validationContext));
  }

  private static boolean isResourceNeedToBeTested(String currResource, String resourceToTest){
    if(Objects.isNull(resourceToTest)){
      return HeatStructureUtil.isNestedResource(currResource);
    }

    return currResource.equals(resourceToTest);
  }

  public static void validateErrorMessage(String actualMessage, String expected, String... params) {
    Assert.assertEquals(actualMessage.replace("\n", "").replace("\r", ""),
        ErrorMessagesFormatBuilder.getErrorWithParameters(expected, params).replace("\n", "")
            .replace("\r", ""));

  }

  public static Validator cerateValidatorImpl(ImplementationConfiguration validatorConf) {
    Validator validator = null;
    try {
      validator =
          CommonMethods.newInstance(validatorConf.getImplementationClass(), Validator.class);
      validator.init(validatorConf.getProperties());
    } catch (IllegalArgumentException iae) {
      log.debug("",iae);
      return null;
    }
    return validator;
  }

  public static Map<String, Object> getResourceMap(String configFileName) throws IOException {
    URL mockResource = ValidationTestUtil.class.getResource(configFileName);
    StringWriter writer = new StringWriter();
    String json = IOUtils.toString(mockResource.openStream(), "UTF-8");
    return JsonUtil.json2Object(json, Map.class);
  }
}
