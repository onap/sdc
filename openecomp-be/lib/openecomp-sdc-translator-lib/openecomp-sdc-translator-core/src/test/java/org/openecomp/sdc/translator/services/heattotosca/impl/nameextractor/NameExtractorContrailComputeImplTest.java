/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.translator.services.heattotosca.impl.nameextractor;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.model.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author SHIRIA
 * @since December 21, 2016.
 */
public class NameExtractorContrailComputeImplTest{


    @Test
    public void testStaticGetComputeNodeTypeId() throws Exception {
      Resource serviceTemplateResource = new Resource();
      serviceTemplateResource.setProperties(new HashMap<>());
      serviceTemplateResource.getProperties().put("image_name", "aaaa");
      String computeNodeTypeId =
          new NameExtractorContrailComputeImpl().extractNodeTypeName(serviceTemplateResource, "123", "123");
      Assert.assertEquals("org.openecomp.resource.vfc.nodes.heat.compute_123", computeNodeTypeId);
    }

    @Test
    public void testComputeNodeTypeIdIsTakingFlavorNameIfImageNameIsNotAsNamingConvention()
        throws Exception {
      Resource serviceTemplateResource = new Resource();
      serviceTemplateResource.setProperties(new HashMap<>());
      serviceTemplateResource.getProperties().put("image_name", "aaaa");
      Map flavor = new HashMap<>();
      flavor.put("get_param", "bbb_flavor_name");
      serviceTemplateResource.getProperties().put("flavor", flavor);
      String computeNodeTypeId =
          new NameExtractorContrailComputeImpl().extractNodeTypeName(serviceTemplateResource, "123", "123");
      Assert.assertEquals("org.openecomp.resource.vfc.nodes.heat.bbb", computeNodeTypeId);
    }

    @Test
    public void testStaticGetComputeNodeTypeIdByFlavor() throws Exception {
      Resource serviceTemplateResource = new Resource();
      serviceTemplateResource.setProperties(new HashMap<>());
      serviceTemplateResource.getProperties().put("image_name", "aaaa");
      serviceTemplateResource.getProperties().put("flavor", "aaaa_flavor_name");
      String computeNodeTypeId =
          new NameExtractorContrailComputeImpl().extractNodeTypeName(serviceTemplateResource, "123", "123");
      Assert.assertEquals("org.openecomp.resource.vfc.nodes.heat.compute_123", computeNodeTypeId);
    }


    @Test
    public void testNamingConventionGetComputeNodeTypeId() throws Exception {
      Resource serviceTemplateResource = new Resource();
      serviceTemplateResource.setProperties(new HashMap<>());
      Map image = new HashMap<>();
      image.put("get_param", "bbb_image_name");
      serviceTemplateResource.getProperties().put("image_name", image);
      String computeNodeTypeId =
          new NameExtractorContrailComputeImpl().extractNodeTypeName(serviceTemplateResource, "123", "123");
      Assert.assertEquals(computeNodeTypeId, "org.openecomp.resource.vfc.nodes.heat.bbb");
    }

    @Test
    public void testNoNamingConventionGetComputeNodeTypeId() throws Exception {
      Resource serviceTemplateResource = new Resource();
      serviceTemplateResource.setProperties(new HashMap<>());
      Map image = new HashMap<>();
      image.put("get_file", "bbb_image");
      serviceTemplateResource.getProperties().put("image_name", image);
      String computeNodeTypeId =
          new NameExtractorContrailComputeImpl().extractNodeTypeName(serviceTemplateResource, "123", "123");
      Assert.assertEquals(computeNodeTypeId, "org.openecomp.resource.vfc.nodes.heat.compute_123");
    }

}
