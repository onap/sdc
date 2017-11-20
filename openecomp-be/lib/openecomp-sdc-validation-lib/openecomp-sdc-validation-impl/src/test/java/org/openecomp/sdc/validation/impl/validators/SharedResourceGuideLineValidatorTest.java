package org.openecomp.sdc.validation.impl.validators;

import org.openecomp.sdc.validation.Validator;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Created by TALIO on 2/15/2017.
 */
public class SharedResourceGuideLineValidatorTest {

  private static final String RESOURCE_PATH = "/org/openecomp/validation/validators/guideLineValidator";
  Validator validator = new SharedResourceGuideLineValidator();

  @Test
  public void testBaseHeatExposeNetwork() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeNetwork/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeNetwork/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG1]: Resource is not defined as output and thus cannot be Shared, Resource ID [SecurityGroup_expose]");
  }

  @Test
  public void testParseException(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeNetwork/parseException/");
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "ERROR: [SRG5]: Invalid HEAT format problem - [while scanning for the next token\n" +
            "found character '\\t(TAB)' that cannot start any token. (Do not use \\t(TAB) for indentation)\n" +
            " in 'reader', line 5, column 1:\n" +
            "    \t\t\tresources:\n" +
            "    ^\n" +
            "]");
  }

  @Test
  public void testInvalidGetResource(){
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/" +
            "baseHeatDoesNotExposeNetworkInvalidGetResource");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG1]: Resource is not defined as output and thus cannot be Shared, Resource ID [net_expose]");

  }

  @Test
  public void testBaseHeatExposeNetworkAndVolume() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeNetworkAndVolume/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeNetworkAndVolume/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG1]: Resource is not defined as output and thus cannot be Shared, Resource ID [volume_expose]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: [SRG1]: Resource is not defined as output and thus cannot be Shared, Resource ID [net_expose]");
  }

  @Test
  public void testBaseHeatExposeServerGroup() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeServerGroup/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeServerGroup/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG1]: Resource is not defined as output and thus cannot be Shared, Resource ID [ServerGroup_expose]");
  }

  @Test
  public void testBaseHeatExposeSecurityGroup() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeSecurityGroup/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeSecurityGroup/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG1]: Resource is not defined as output and thus cannot be Shared, Resource ID [SecurityGroup_expose]");
  }

  @Test
  public void testBaseHeatExposeVolume() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeVolume/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatDoesNotExposeVolume/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG1]: Resource is not defined as output and thus cannot be Shared, Resource ID [volume_expose]");
  }

  @Test
  public void testHeatVolumeExpose() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/heatVolumeExpose/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/heatVolumeExpose/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("firstVol.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("firstVol.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG2]: Volume is not defined as output and thus cannot be attached volume_expose");

  }

  @Test
  public void testResourceIsExposedByCallingGetResourceNotFromOutput() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatExposeResourceUsingGetResource/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


    messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/baseHeatExposeResourceUsingGetResource/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("base_virc.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("base_virc.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG1]: Resource is not defined as output and thus cannot be Shared, Resource ID [virc_RSG]");
  }

  @Test
  public void testMissingBaseHeat() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/missingBaseHeat/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG3]: Missing Base HEAT. Pay attention that without Base HEAT, there will be no shared resources");
  }

  @Test
  public void testMultiBaseHeat() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        RESOURCE_PATH + "/multiBaseHeat/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().get(0).getMessage(),
        "WARNING: [SRG4]: Multi Base HEAT. Expected only one. Files [second.yaml,first.yaml].");
  }
}
