package org.openecomp.sdc.validation.impl.validators.namingconvention;

import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.impl.validators.HeatResourceValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * Created by TALIO on 2/28/2017.
 */
public class ContrailServiceInstanceNamingConventionValidatorTest {

  HeatResourceValidator baseValidator = new HeatResourceValidator();
  ContrailServiceInstanceNamingConventionValidator resourceValidator = new
      ContrailServiceInstanceNamingConventionValidator();

  @Test
  public void testContrailServiceInstanceAvailabilityZoneNotAlignedWithNamingConvention() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_INSTANCE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatcontrailserviceinstanceavailabilityzone/notaligned");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
        "WARNING: [NSI1]: Service Instance 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone_1a], Resource ID [service_instance_1]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testContrailServiceInstanceAvailabilityZoneAlignedWithNamingConvention() {
    Map<String, MessageContainer> messages = ValidationTestUtil.testValidator(baseValidator,
        resourceValidator, HeatResourcesTypes.CONTRAIL_SERVICE_INSTANCE.getHeatResource(),
        "/org/openecomp/validation/validators/guideLineValidator/heatcontrailserviceinstanceavailabilityzone/aligned");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
  }
}
