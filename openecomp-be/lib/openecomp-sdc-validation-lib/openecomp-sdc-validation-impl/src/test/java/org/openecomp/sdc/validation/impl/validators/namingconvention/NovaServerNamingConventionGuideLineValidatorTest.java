/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.validation.impl.validators.namingconvention;

import java.io.IOException;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.core.validation.types.MessageContainer;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.validation.impl.validators.NamingConventionGuideLineValidator;
import org.openecomp.sdc.validation.util.ValidationTestUtil;

/**
 * Created by TALIO on 2/28/2017.
 */
public class NovaServerNamingConventionGuideLineValidatorTest {

  NamingConventionGuideLineValidator baseValidator = new NamingConventionGuideLineValidator();
  NovaServerNamingConventionGuideLineValidator resourceValidator = new
      NovaServerNamingConventionGuideLineValidator();
  private static final String PATH = "/org/openecomp/validation/validators/guideLineValidator/novaserverValidation/";
  @Test
  public void testHeatNovaServerMetaDataValidation() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerMetaDataValidation/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerMetaDataValidation/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS1]: Missing Nova Server Metadata property, Resource ID [FSB2]");
  }

  @Test
  public void testNovaServerAvailabilityZoneName() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerAvailabilityZoneName/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerAvailabilityZoneName/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 3);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS5]: Server 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone_a], Resource ID [FSB2]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
            "WARNING: [NNS5]: Server 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone], Resource ID [FSB3]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testNovaImageAndFlavorNamesEmptyProperties() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerImageAndFlavor/negativeEmptyProperties");
    Assert.assertNotNull(messages);
    Assert.assertEquals(1, messages.size());
    Assert.assertEquals(1, messages.get("first.yaml").getErrorMessageList().size());
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS13]: Server 'image' Parameter Name not aligned with Guidelines, Parameter Name [fsb2-image], Resource ID [FSB3]. As a result, VF/VFC Profile may miss this information");
  }

    @Test
    public void testNovaImageAndFlavorNames() {
        Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerImageAndFlavor/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerImageAndFlavor/negative");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS13]: Server 'flavor' Parameter Name not aligned with Guidelines, Parameter Name [fsb2-flavor], Resource ID [FSB2]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
            "WARNING: [NNS13]: Server 'image' Parameter Name not aligned with Guidelines, Parameter Name [fsb2-image], Resource ID [FSB3]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testNovaResourceNetworkUniqueRole() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaNetworkUniqueRoleConvention/positive/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);
    messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaNetworkUniqueRoleConvention/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS12]: A resource is connected twice to the same network role, Network Role [Internal1], Resource ID [FSB2]");
  }

  @Test
  public void testNovaServerName() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerNameValidation/positive");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 0);

    messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaServerNameValidation/negative/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS10]: Server 'name' Parameter Name not aligned with Guidelines, Parameter Name [pcrf_pps_server_4], Resource ID [FSB2]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testVMNameSyncInNova() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 4);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS10]: Server 'name' Parameter Name not aligned with Guidelines, Parameter Name [CE_server_name], Resource ID [FSB2_legal_2]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
            "WARNING: [NNS13]: Server 'flavor' Parameter Name not aligned with Guidelines, Parameter Name [fsb_flavor_names], Resource ID [FSB2_legal_3]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(2).getMessage(),
            "WARNING: [NNS11]: Nova Server naming convention in image, flavor and name properties is not consistent, Resource ID [FSB2_illegal_1]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(3).getMessage(),
            "WARNING: [NNS11]: Nova Server naming convention in image, flavor and name properties is not consistent, Resource ID [FSB2_illegal_2]");
  }

  @Test
  public void testAvailabilityZoneName() throws IOException {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "availability_zone_name/input");

    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.size(), 1);

    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().size(), 2);
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS5]: Server 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone_name], Resource ID [availability_zone_illegal_name_1]. As a result, VF/VFC Profile may miss this information");
    Assert.assertEquals(
            messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
            "WARNING: [NNS5]: Server 'Availability Zone' Parameter Name not aligned with Guidelines, Parameter Name [availability_zone], Resource ID [availability_zone_illegal_name_2]. As a result, VF/VFC Profile may miss this information");
  }

  @Test
  public void testHeatNovaServerVnfIDValidation() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            "/org/openecomp/validation/validators/guideLineValidator/novaserverValidation/heatNovaServerMetaDataValidation/negativemissingvnfid/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 1);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS3]: Missing VNF_ID in Metadata property, Resource ID [FSB2]");
  }

  @Test
  public void testHeatNovaServerVfModuleValidation() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            "/org/openecomp/validation/validators/guideLineValidator/novaserverValidation/heatNovaServerMetaDataValidation/negativemisningvfmodule/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS2]: Missing VF_MODULE_ID in Metadata property, Resource id [FSB2]");
  }

  @Test
  public void testMissingParam() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaNetworkUniqueRoleConvention/missingportNetwork/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().size(), 4);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "WARNING: [NNS4]: Missing get_param in network or network_id, Resource Id [FSB2]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(1).getMessage(),
            "WARNING: [NNS6]: Missing get_param in availability_zone, Resource Id [FSB2]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(2).getMessage(),
            "WARNING: [NNS7]: Missing get_param in nova server name, Resource Id [FSB2]");
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(3).getMessage(),
            "WARNING: [NNS8]: Missing get_param in flavor, Resource Id [FSB2]");
  }

  @Test
  public void testNovaResource() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaNetworkUniqueRoleConvention/invalidresource/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.get("first.yaml").getErrorMessageList().get(0).getMessage(),
            "ERROR: [NNS14]: invalid get_resource syntax is in use - 1234 , get_resource function should get the resource id of the referenced resource");
  }

  @Test
  public void testEnvFileContent() {
    Map<String, MessageContainer> messages = new ValidationTestUtil().testValidator(baseValidator,
            resourceValidator, HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
            PATH + "heatNovaNetworkUniqueRoleConvention/input/");
    Assert.assertNotNull(messages);
    Assert.assertEquals(messages.get("hot-nimbus-pps_v1.0.yaml").getErrorMessageList().get(1).getMessage(),
            "WARNING: [NNS9]: Server 'Name' Parameter Name not aligned with Guidelines, Parameter Name [{key=value}], Resource ID [server_1]. As a result, VF/VFC Profile may miss this information");
  }
}
