package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailTranslationHelper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiria
 * @since August 09, 2016.
 */
public class ResourceTranslationContrailServiceTemplateImplTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testStaticGetComputeNodeTypeId() throws Exception {
    Resource serviceTemplate = new Resource();
    serviceTemplate.setProperties(new HashMap<>());
    serviceTemplate.getProperties().put("image_name", "aaaa");
    String computeNodeTypeId =
        new ContrailTranslationHelper().getComputeNodeTypeId("123", serviceTemplate);
    Assert.assertEquals("org.openecomp.resource.vfc.nodes.heat.compute_123", computeNodeTypeId);
  }

  @Test
  public void testNamingConventionGetComputeNodeTypeId() throws Exception {
    Resource serviceTemplate = new Resource();
    serviceTemplate.setProperties(new HashMap<>());
    Map image = new HashMap<>();
    image.put("get_param", "bbb_image_name");
    serviceTemplate.getProperties().put("image_name", image);
    String computeNodeTypeId =
        new ContrailTranslationHelper().getComputeNodeTypeId("123", serviceTemplate);
    Assert.assertEquals(computeNodeTypeId, "org.openecomp.resource.vfc.nodes.heat.bbb");
  }

  @Test
  public void testNoNamingConventionGetComputeNodeTypeId() throws Exception {
    Resource serviceTemplate = new Resource();
    serviceTemplate.setProperties(new HashMap<>());
    Map image = new HashMap<>();
    image.put("get_file", "bbb_image");
    serviceTemplate.getProperties().put("image_name", image);
    String computeNodeTypeId =
        new ContrailTranslationHelper().getComputeNodeTypeId("123", serviceTemplate);
    Assert.assertEquals(computeNodeTypeId, "org.openecomp.resource.vfc.nodes.heat.compute_123");
  }
}