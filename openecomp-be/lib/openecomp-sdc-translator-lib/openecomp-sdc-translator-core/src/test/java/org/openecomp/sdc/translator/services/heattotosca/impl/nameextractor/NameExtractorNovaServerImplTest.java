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

import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationNovaServerImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author SHIRIA
 * @since December 20, 2016.
 */
public class NameExtractorNovaServerImplTest {
  @Test
  public void shouldReturnNamePrefixIfPropertyNameMatchWithIndex() throws Exception {
    Map<String, Object> propertiesMap = new HashMap();
    propertiesMap.put("a", "sss");
    HashMap imageMap = new HashMap();
    String name = "avi_test_name_1";
    imageMap.put("get_param", name);
    propertiesMap.put(Constants.NAME_PROPERTY_NAME, imageMap);
    Resource resource = new Resource();
    resource.setProperties(propertiesMap);
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType =
        new NameExtractorNovaServerImpl().extractNodeTypeName(resource, "Ignore", "Ignore");
    assertTrue(localNodeType.equals("org.openecomp.resource.vfc.nodes.heat.avi_test"));
  }

  @Test
  public void shouldReturnNamePrefixIfPropertyNameMatchWithListObjectInGetParamVal()
      throws Exception {
    Map<String, Object> propertiesMap = new HashMap();
    propertiesMap.put("a", "sss");
    HashMap imageMap = new HashMap();
    List val = Arrays.asList("virc_vm_names", "2");
    imageMap.put("get_param", val);
    propertiesMap.put(Constants.NAME_PROPERTY_NAME, imageMap);
    Resource resource = new Resource();
    resource.setProperties(propertiesMap);
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType =
        new NameExtractorNovaServerImpl().extractNodeTypeName(resource, "Ignore", "Ignore");
    assertTrue(localNodeType.equals("org.openecomp.resource.vfc.nodes.heat.virc_vm"));
  }

  @Test
  public void shouldReturnNamePrefixIfPropertyNameMatchWithListObjectInGetParamValAndGetParamAsGetParamVal()
      throws Exception {
    Map<String, Object> propertiesMap = new HashMap();
    propertiesMap.put("a", "sss");
    HashMap nameMap = new HashMap();
    HashMap nameValMap = new HashMap();
    nameValMap.put("get_param", "anyParam");
    List val = Arrays.asList("virc_vm_names", nameValMap);
    nameMap.put("get_param", val);
    propertiesMap.put(Constants.NAME_PROPERTY_NAME, nameMap);
    Resource resource = new Resource();
    resource.setProperties(propertiesMap);
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType =
        new NameExtractorNovaServerImpl().extractNodeTypeName(resource, "Ignore", "Ignore");
    assertTrue(localNodeType.equals("org.openecomp.resource.vfc.nodes.heat.virc_vm"));
  }


  @Test
  public void shouldReturnNamePrefixIfPropertyNameMatchWithoutIndex() throws Exception {
    Map<String, Object> propertiesMap = new HashMap();
    propertiesMap.put("a", "sss");
    HashMap imageMap = new HashMap();
    String name = "avi_test_names";
    imageMap.put("get_param", name);
    propertiesMap.put(Constants.NAME_PROPERTY_NAME, imageMap);
    Resource resource = new Resource();
    resource.setProperties(propertiesMap);
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType =
        new NameExtractorNovaServerImpl().extractNodeTypeName(resource, "Ignore", "Ignore");
    assertTrue(localNodeType.equals("org.openecomp.resource.vfc.nodes.heat.avi_test"));
  }

  @Test
  public void shouldReturnPrefixByPropertyOrder() throws Exception {
    Map<String, Object> propertiesMap = new HashMap();
    propertiesMap.put("a", "sss");
    HashMap imageMap = new HashMap();
    String name = "avi_test1_namesw";
    imageMap.put("get_param", name);
    propertiesMap.put(Constants.NAME_PROPERTY_NAME, imageMap);
    String flavor = "avi_test2_flavor_name";
    imageMap = new HashMap();
    imageMap.put("get_param", flavor);
    propertiesMap.put("flavor", imageMap);
    Resource resource = new Resource();
    resource.setProperties(propertiesMap);
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType =
        new NameExtractorNovaServerImpl().extractNodeTypeName(resource, "Ignore", "Ignore");
    assertTrue(localNodeType.equals("org.openecomp.resource.vfc.nodes.heat.avi_test2"));
  }

  @Test
  public void shouldReturnEmptyIfPropertiesAreNotAsNamingConvention() throws Exception {
    Map<String, Object> propertiesMap = new HashMap();
    propertiesMap.put("a", "sss");
    HashMap imageMap = new HashMap();
    String name = "avi_test_namesw";
    imageMap.put("get_param", name);
    propertiesMap.put(Constants.NAME_PROPERTY_NAME, imageMap);
    Resource resource = new Resource();
    resource.setProperties(propertiesMap);
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType =
        new NameExtractorNovaServerImpl()
            .extractNodeTypeName(resource, "this.is.test.resource", "this.is.test.resource");
    assertTrue(localNodeType.equals("org.openecomp.resource.vfc.nodes.heat.this_is_test_resource"));
  }

}
