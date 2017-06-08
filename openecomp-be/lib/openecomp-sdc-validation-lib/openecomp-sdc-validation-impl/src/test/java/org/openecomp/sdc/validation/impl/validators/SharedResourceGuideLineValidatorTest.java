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

  Validator validator = new SharedResourceGuideLineValidator();

  @Test
  public void testBaseHeatExposeNetwork() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeNetwork/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeNetwork/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Resource is not defined as output and thus cannot be Shared, Resource ID [SecurityGroup_expose]");
  }

  @Test
  public void testBaseHeatExposeNetworkAndVolume() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeNetworkAndVolume/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeNetworkAndVolume/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Resource is not defined as output and thus cannot be Shared, Resource ID [volume_expose]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: Resource is not defined as output and thus cannot be Shared, Resource ID [net_expose]");
  }

  @Test
  public void testBaseHeatExposeServerGroup() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeServerGroup/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeServerGroup/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Resource is not defined as output and thus cannot be Shared, Resource ID [ServerGroup_expose]");
  }

  @Test
  public void testBaseHeatExposeSecurityGroup() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeSecurityGroup/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeSecurityGroup/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Resource is not defined as output and thus cannot be Shared, Resource ID [SecurityGroup_expose]");
  }

  @Test
  public void testBaseHeatExposeVolume() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeVolume/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatDoesNotExposeVolume/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Resource is not defined as output and thus cannot be Shared, Resource ID [volume_expose]");
  }

  @Test
  public void testHeatVolumeExpose() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/heatVolumeExpose/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/heatVolumeExpose/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("firstVol.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("firstVol.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Volume is not defined as output and thus cannot be attached volume_expose");

  }

  @Test
  public void testResourceIsExposedByCallingGetResourceNotFromOutput() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatExposeResourceUsingGetResource/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);


    messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/baseHeatExposeResourceUsingGetResource/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("base_virc.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("base_virc.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Resource is not defined as output and thus cannot be Shared, Resource ID [virc_RSG]");
  }

  @Test
  public void testMissingBaseHeat() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/missingBaseHeat/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().get(0).getMessage(),
        "WARNING: Missing Base HEAT. Pay attention that without Base HEAT, there will be no shared resources");
  }

  @Test
  public void testMultiBaseHeat() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(validator,
        "/org/openecomp/validation/validators/attGuideLineValidator/multiBaseHeat/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("MANIFEST.json").getErrorMessageList().get(0).getMessage(),
        "WARNING: Multi Base HEAT. Expected only one. Files [second.yaml,first.yaml].");
  }
}
