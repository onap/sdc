package org.openecomp.sdc.validation.impl.validators.namingconvention;

import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;

import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Created by TALIO on 2/28/2017.
 */
public class ContrailServiceTemplateNamingConventionValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  ContrailServiceTemplateNamingConventionValidator resourceValidator = new
      ContrailServiceTemplateNamingConventionValidator();

  @Test
  public void testContrailServiceTemplateImageAndFlavorNamesAlignedWithNamingConventionButDifferentVmType() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatcontrailservicetemplateimageandflavor/imageandflavordifferentvmtype");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(
        messages.get(messages.keySet().iterator().next()).getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [I-1] : Service Template naming convention in Image and Flavor properties is not consistent in Resource, Resource ID service_template");
  }

  @Test
  public void testContrailServiceTemplateImageAndFlavorNamesNotAlignedWithNamingConvention() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatcontrailservicetemplateimageandflavor/notaligned");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: Service Template 'image_name' Parameter Name not aligned with Guidelines, Parameter Name [st_imaage_name], Resource ID [service_template]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
        "WARNING: Service Template 'flavor' Parameter Name not aligned with Guidelines, Parameter Name [st_flavaor_name], Resource ID [service_template]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testContrailServiceTemplateImageAndFlavorNamesAlignedWithNamingConvention() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_TEMPLATE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatcontrailservicetemplateimageandflavor/aligned");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }
}
