package org.openecomp.sdc.translator.services.heattotosca.helper.impl;

import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.impl.ResourceTranslationNovaServerImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * @author Avrahamg
 * @since August 04, 2016
 */
public class NameExtractorServiceImplTest {
  @Test
  public void shouldReturnNamePrefixIfPropertyNameMatchWithIndex() throws Exception {
    Map<String, Object> propertiesMap = new HashMap();
    propertiesMap.put("a", "sss");
    HashMap imageMap = new HashMap();
    String name = "avi_test_name_1";
    imageMap.put("get_param", name);
    propertiesMap.put(Constants.NAME_PROPERTY_NAME, imageMap);
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType = resourceTranslationNovaServer
        .createLocalNodeType(new ServiceTemplate(), propertiesMap, "Ignore");
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
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType = resourceTranslationNovaServer
        .createLocalNodeType(new ServiceTemplate(), propertiesMap, "Ignore");
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
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType = resourceTranslationNovaServer
        .createLocalNodeType(new ServiceTemplate(), propertiesMap, "Ignore");
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
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType = resourceTranslationNovaServer
        .createLocalNodeType(new ServiceTemplate(), propertiesMap, "Ignore");
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
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType = resourceTranslationNovaServer
        .createLocalNodeType(new ServiceTemplate(), propertiesMap, "Ignore");
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
    ResourceTranslationNovaServerImpl resourceTranslationNovaServer =
        new ResourceTranslationNovaServerImpl();
    String localNodeType = resourceTranslationNovaServer
        .createLocalNodeType(new ServiceTemplate(), propertiesMap, "this.is.test.resource");
    assertTrue(localNodeType.equals("org.openecomp.resource.vfc.nodes.heat.this_is_test_resource"));
  }
}