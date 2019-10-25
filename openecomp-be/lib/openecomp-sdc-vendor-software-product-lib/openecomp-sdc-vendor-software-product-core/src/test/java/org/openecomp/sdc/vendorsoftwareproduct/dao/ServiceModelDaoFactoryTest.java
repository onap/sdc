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

package org.openecomp.sdc.vendorsoftwareproduct.dao;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.model.impl.zusammen.ServiceModelDaoZusammenImpl;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ServiceModelDaoFactoryTest {

  private static final String vspId = CommonMethods.nextUuId();
  private static final Version version = Version.valueOf("1.0");
  private static final String baseServiceTemplateName = "baseYaml.yaml";
  private static String artifact001;
  private static final String tenant = "dox";

  @Before
  public void setUp(){
    SessionContextProviderFactory.getInstance().createInterface().create("test", tenant);
  }

  @After
  public void tearDown(){
    SessionContextProviderFactory.getInstance().createInterface().close();
  }

  @Test
  public void storeServiceModelTest() {

    ItemVersion itemVersionmock = new ItemVersion();
    itemVersionmock.setId(new Id());

    ZusammenAdaptorMock zusammenAdaptor = new ZusammenAdaptorMock();
    ServiceModelDaoZusammenImpl serviceModelDaoZusammen = new ServiceModelDaoZusammenImpl(
        zusammenAdaptor);

    zusammenAdaptor.setItemVersion(itemVersionmock);
    ToscaServiceModel model = getToscaServiceModel();
    serviceModelDaoZusammen.storeServiceModel(vspId, version, model);
  }

  @Test
  public void getServiceModelTest() {

    ItemVersion itemVersionmock = new ItemVersion();
    itemVersionmock.setId(new Id());

    ElementInfo elementInfo = new ElementInfo();
    Info info = new Info();
    info.addProperty("base", "baseElement");
    elementInfo.setInfo(info);

    ElementInfo artifactElementInfo = new ElementInfo();
    artifactElementInfo.setInfo(new Info());
    artifactElementInfo.getInfo().setName(ElementType.Artifacts.name());
    ElementInfo templateElementInfo = new ElementInfo();
    templateElementInfo.setInfo(new Info());
    templateElementInfo.getInfo().setName(ElementType.Templates.name());

    ElementInfo serviceModelElementInfo = new ElementInfo();
    serviceModelElementInfo.setInfo(new Info());
    serviceModelElementInfo.getInfo().setName(ElementType.ServiceModel.name());
    ZusammenElement element = new ZusammenElement();
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    YamlUtil yamlUtil = new YamlUtil();
    element.setData(new ByteArrayInputStream(yamlUtil.objectToYaml(serviceTemplate).getBytes()));
    info = new Info();
    info.setName("dataFileName");
    element.setInfo(info);
    ZusammenAdaptorMock zusammenAdaptor = new ZusammenAdaptorMock();
    ServiceModelDaoZusammenImpl serviceModelDaoZusammen = new ServiceModelDaoZusammenImpl(
        zusammenAdaptor);

    zusammenAdaptor.setItemVersion(itemVersionmock);
    zusammenAdaptor.addElementInfo("null" + ElementType.ServiceModel.name(), elementInfo);
    zusammenAdaptor.addElementInfo("null" + ElementType.Artifacts.name(), artifactElementInfo);
    zusammenAdaptor.addElementInfo("null" + ElementType.Templates.name(), templateElementInfo);
    zusammenAdaptor.addElementInfo("null" + ElementType.ServiceModel.name(),
        serviceModelElementInfo);
    zusammenAdaptor.addElement(element);

    ToscaServiceModel model =
        serviceModelDaoZusammen.getServiceModel(vspId, version);
    Assert.assertNotNull(model);

    setArtifact(model);
    Assert.assertEquals(artifact001,"dataFileName");

  }

  private static void setArtifact(ToscaServiceModel model) {
    artifact001 =
        (String) (model).getArtifactFiles().getFileList().toArray()[0];
  }

  private ToscaServiceModel getToscaServiceModel() {

    Map<String, ServiceTemplate> serviceTemplates = getServiceTemplates(baseServiceTemplateName);
    FileContentHandler artifacts = getArtifacts();
    return new ToscaServiceModel(artifacts, serviceTemplates, baseServiceTemplateName);
  }


  private Map<String, ServiceTemplate> getServiceTemplates(String base) {

    Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();

    serviceTemplates.put(base, getServiceTemplate());
    serviceTemplates.put("SERV1", getServiceTemplate());
    serviceTemplates.put("SERV2", getServiceTemplate());
    serviceTemplates.put("SERV3", getServiceTemplate());
    serviceTemplates.put("SERV4", getServiceTemplate());

    return serviceTemplates;
  }

  public FileContentHandler getArtifacts() {
    Map<String, byte[]> artifacts = new HashMap<>();
    artifacts.put("art1", "this is art1".getBytes());
    artifacts.put("art2", ("this is art2 desc:" + CommonMethods.nextUuId()).getBytes());
    artifacts.put("art3", ("this is art3 desc:" + CommonMethods.nextUuId()).getBytes());
    artifacts.put("art4", ("this is art4 desc:" + CommonMethods.nextUuId()).getBytes());

    FileContentHandler fileContentHandler = new FileContentHandler();
    fileContentHandler.setFiles(artifacts);
    return fileContentHandler;
  }

  private ServiceTemplate getServiceTemplate() {
    ServiceTemplate serviceTemplate = new ServiceTemplate();
    serviceTemplate.setTosca_definitions_version("version 1.0");
    serviceTemplate.setDescription(CommonMethods.nextUuId());
    return serviceTemplate;
  }

  private class ZusammenAdaptorMock implements ZusammenAdaptor {

    private ItemVersion itemVersion;
    private Map<String, ElementInfo> elementInfoMap = new HashMap<>();
    private Collection<Element> elements = new ArrayList<>();

    private void setItemVersion(ItemVersion itemVersion) {
      this.itemVersion = itemVersion;
    }

    private void addElementInfo(String key, ElementInfo elementInfo) {
      elementInfoMap.put(key, elementInfo);
    }

    private void addElement(Element element) {
      elements.add(element);
    }

    @Override
    public Collection<ItemVersion> listPublicVersions(SessionContext context, Id itemId) {
      return null;
    }

    @Override
    public ItemVersion getPublicVersion(SessionContext context, Id itemId, Id versionId) {
      return null;
    }

    @Override
    public Optional<Element> getElement(SessionContext context, ElementContext elementContext,
                                        String elementId) {
      return Optional.empty();
    }

    @Override
    public Optional<Element> getElementByName(SessionContext context,
                                              ElementContext elementContext,
                                              Id parentElementId, String elementName) {
      return Optional.empty();
    }

    @Override
    public Collection<ElementInfo> listElements(SessionContext context,
                                                ElementContext elementContext,
                                                Id parentElementId) {
      return null;
    }

    @Override
    public Collection<Element> listElementData(SessionContext context,
                                               ElementContext elementContext,
                                               Id parentElementId) {
      return elements;
    }

    @Override
    public Collection<ElementInfo> listElementsByName(SessionContext context,
                                                      ElementContext elementContext,
                                                      Id parentElementId, String elementName) {

      if (elementName.equals(ElementType.VspModel.name())) {
        return elementInfoMap.values();
      }

      return null;
    }

    @Override
    public Optional<ElementInfo> getElementInfoByName(SessionContext context,
                                                      ElementContext elementContext,
                                                      Id parentElementId, String elementName) {

      if (elementName.equals(ElementType.Templates.name())) {
        return Optional.ofNullable(elementInfoMap.get("null" + elementName));
      } else if (elementName.equals(ElementType.Artifacts.name())) {
        return Optional.ofNullable(elementInfoMap.get("null" + elementName));
      }

      return Optional.empty();
    }

    @Override
    public Element saveElement(SessionContext context, ElementContext elementContext,
                               ZusammenElement element, String message) {
      return null;
    }

    @Override
    public void resolveElementConflict(SessionContext context, ElementContext elementContext,
                                       ZusammenElement element, Resolution resolution) {

    }

    @Override
    public Collection<HealthInfo> checkHealth(SessionContext context) {
      return null;
    }

    @Override
    public Id createItem(SessionContext context, Info info) {
      return null;
    }

    @Override
    public void updateItem(SessionContext context, Id itemId, Info info) {

    }

    @Override
    public Id createVersion(SessionContext context, Id itemId, Id baseVersionId,
                            ItemVersionData itemVersionData) {
      return null;
    }

    @Override
    public void updateVersion(SessionContext context, Id itemId, Id versionId,
                              ItemVersionData itemVersionData) {

    }

    @Override
    public ItemVersion getVersion(SessionContext context, Id itemId, Id versionId) {
      return null;
    }

    @Override
    public ItemVersionStatus getVersionStatus(SessionContext context, Id itemId, Id versionId) {
      return null;
    }

    @Override
    public ItemVersionConflict getVersionConflict(SessionContext context, Id itemId, Id versionId) {
      return null;
    }

    @Override
    public void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag) {

    }

    @Override
    public void resetVersionHistory(SessionContext context, Id itemId, Id versionId,
                                    String changeRef) {


    }

    /*@Override
    public void revertVersionToRevision(SessionContext context, Id itemId, Id versionId,
                                        Id revisionId) {

    }

    @Override
    public ItemVersionRevisions listVersionRevisions(SessionContext context, Id itemId,
                                                     Id versionId) {
      return null;
    }*/

    @Override
    public void publishVersion(SessionContext context, Id itemId, Id versionId, String message) {

    }

    @Override
    public void syncVersion(SessionContext sessionContext, Id itemId, Id versionId) {

    }

    @Override
    public void forceSyncVersion(SessionContext context, Id itemId, Id versionId) {

    }

    @Override
    public void cleanVersion(SessionContext sessionContext, Id itemId, Id versionId) {

    }

    @Override
    public Optional<ElementInfo> getElementInfo(SessionContext context,
                                                ElementContext elementContext,
                                                Id elementId) {
      return Optional.empty();
    }

    @Override
    public String getVersion(SessionContext sessionContext) {
      return null;
    }

    @Override
    public void revert(SessionContext sessionContext, Id itemId, Id versionId,
                       Id revisionId) {

    }

    @Override
    public ItemVersionRevisions listRevisions(SessionContext sessionContext, Id itemId,
                                              Id versionId) {
      return null;
    }

    @Override
    public Optional<ElementConflict> getElementConflict(SessionContext context,
                                                        ElementContext elementContext,
                                                        Id id) {
      return Optional.empty();
    }

    @Override
    public Collection<Item> listItems(SessionContext context) {
      return null;
    }

    @Override
    public Item getItem(SessionContext context, Id itemId) {
      return null;
    }

    @Override
    public void deleteItem(SessionContext context, Id itemId) {

    }
  }
}
