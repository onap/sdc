package org.openecomp.sdc.model.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import org.apache.commons.io.IOUtils;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.errors.RetrieveServiceTemplateFromDbErrorBuilder;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.core.converter.datatypes.Constants;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServiceModelDaoZusammenImpl
    implements ServiceModelDao<ToscaServiceModel, ServiceElement> {
  private static final Logger logger = LoggerFactory.getLogger(ServiceModelDaoZusammenImpl.class);

  protected ZusammenAdaptor zusammenAdaptor;
  protected String name;

  public ServiceModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
    this.name = StructureElement.ServiceModel.name();
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public ToscaServiceModel getServiceModel(String vspId, Version version) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId, getFirstVersionId(context, itemId),
        version.getStatus() == VersionStatus.Locked ? null : version.toString());

    Optional<ElementInfo> serviceModelElement = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, name);

    if (serviceModelElement.isPresent()) {
      String entryDefinitionServiceTemplate =
          serviceModelElement.get().getInfo().getProperty("base");
      Id serviceModelElementId = serviceModelElement.get().getId();
      Map<String, ServiceTemplate> serviceTemplates =
          getTemplates(context, elementContext, serviceModelElementId);
      if (serviceTemplates == null) {
        return null;
      }
      FileContentHandler artifacts = getArtifacts(context, elementContext, serviceModelElementId);


      return new ToscaServiceModel(
          artifacts, serviceTemplates, entryDefinitionServiceTemplate);
    } else {
      return null;
    }
  }

  protected Map<String, ServiceTemplate> getTemplates(SessionContext context,
                                                      ElementContext elementContext,
                                                      Id serviceModelElementId) {
    Optional<ElementInfo> templatesElementInfo = zusammenAdaptor.getElementInfoByName(
        context, elementContext, serviceModelElementId, StructureElement.Templates.name());

    if (templatesElementInfo.isPresent()) {
      Collection<Element> elements = zusammenAdaptor.listElementData(context, elementContext,
          templatesElementInfo.get().getId());

      return elements.stream().collect(Collectors.toMap(
          element -> element.getInfo().getName(),
          this::elementToServiceTemplate));
    }
    return null;
  }

  protected FileContentHandler getArtifacts(SessionContext context, ElementContext elementContext,
                                            Id serviceModelElementId) {
    Optional<ElementInfo> artifactsElement = zusammenAdaptor.getElementInfoByName(
        context, elementContext, serviceModelElementId, StructureElement.Artifacts.name());

    if (artifactsElement.isPresent()) {

      Collection<Element> elements = zusammenAdaptor.listElementData(context, elementContext,
          artifactsElement.get().getId());
      FileContentHandler fileContentHandler = new FileContentHandler();
      elements.forEach(element -> fileContentHandler.addFile(element.getInfo().getName(),
          element.getData()));
      return fileContentHandler;
    }

    return null;
  }

  @Override
  public void storeServiceModel(String vspId, Version version, ToscaServiceModel serviceModel) {
    logger.info("Storing service model for vsp id -> " + vspId);

    ZusammenElement templatesElement =
        buildStructuralElement(StructureElement.Templates.name(), null);
    serviceModel.getServiceTemplates().entrySet().forEach(entry -> templatesElement.addSubElement(
        buildServiceTemplateElement(entry.getKey(), entry.getValue(),
            serviceModel.getEntryDefinitionServiceTemplate(), Action.CREATE)));

    ZusammenElement artifactsElement =
        buildStructuralElement(StructureElement.Artifacts.name(), Action.UPDATE);
    if (Objects.nonNull(serviceModel.getArtifactFiles())) {
      serviceModel.getArtifactFiles().getFiles().entrySet().forEach(entry -> artifactsElement
          .addSubElement(buildArtifactElement(entry.getKey(), entry.getValue(), Action.CREATE)));
    }

    ZusammenElement serviceModelElement = buildStructuralElement(name, Action.UPDATE);
    serviceModelElement.getInfo()
        .addProperty("base", serviceModel.getEntryDefinitionServiceTemplate());

    serviceModelElement.addSubElement(templatesElement);
    serviceModelElement.addSubElement(artifactsElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId, getFirstVersionId(context, itemId));
    zusammenAdaptor
        .saveElement(context, elementContext, serviceModelElement, "Store service model");

    logger.info("Finished storing service model for vsp id -> " + vspId);
  }

  @Override
  public ServiceElement getServiceModelInfo(String vspId, Version version, String name) {
    return null;
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    logger.info("started deleting service model for vsp id -> " + vspId);
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId, getFirstVersionId(context, itemId));

    ZusammenElement zusammenElement = ZusammenUtil.buildStructuralElement(name, Action.DELETE);
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement, "delete:" + name + ".");
    logger.info("Finished deleting service model for vsp id -> " + vspId);
  }

  protected ZusammenElement buildArtifactElement(String name, byte[] artifact, Action action) {
    ZusammenElement artifactElement = new ZusammenElement();
    artifactElement.setAction(action);
    Info info = new Info();
    info.setName(name);
    info.addProperty("type", ElementType.Artifact.name());
    artifactElement.setInfo(info);
    artifactElement.setData(new ByteArrayInputStream(artifact));

    return artifactElement;
  }

  private ServiceTemplate elementToServiceTemplate(Element element){

    try {
      String yamlContent = IOUtils.toString(element.getData());
      return new ToscaExtensionYamlUtil().
          yamlToObject(yamlContent, ServiceTemplate.class);
    }catch (Exception e){
      throw new CoreException(
          new RetrieveServiceTemplateFromDbErrorBuilder(
              element.getInfo().getName(), e.getMessage()).build());
    }
  }

  private Element buildServiceTemplateElement(String name, ServiceTemplate serviceTemplate,
                                              String entryDefinitionServiceTemplate,
                                              Action action) {
    ZusammenElement zusammenElement = new ZusammenElement();
    zusammenElement.setAction(action);
    Info info = new Info();
    info.setName(name);
    info.setDescription(serviceTemplate.getDescription());
    info.addProperty("type", ElementType.Servicetemplate.name());
    info.addProperty("base", entryDefinitionServiceTemplate);
    String yaml = new ToscaExtensionYamlUtil().objectToYaml(serviceTemplate);
    zusammenElement.setData(new ByteArrayInputStream(yaml.getBytes()));
    zusammenElement.setInfo(info);
    return zusammenElement;
  }

  protected Id getFirstVersionId(SessionContext context, Id vspId) {
    Optional<ItemVersion> itemVersionOptional = zusammenAdaptor.getFirstVersion(context, vspId);
    ItemVersion itemVersion = itemVersionOptional.orElseThrow(() ->
        new RuntimeException(String.format("Vsp %s does not contain any version.", vspId))); //todo
    return itemVersion.getId();
  }

  protected ZusammenElement buildStructuralElement(String structureElement, Action action) {
    return ZusammenUtil.buildStructuralElement(structureElement, action);
  }
}
