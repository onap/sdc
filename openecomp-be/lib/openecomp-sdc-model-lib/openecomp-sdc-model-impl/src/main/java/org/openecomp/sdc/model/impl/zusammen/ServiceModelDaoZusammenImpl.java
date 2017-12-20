package org.openecomp.sdc.model.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.apache.commons.io.IOUtils;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.errors.RetrieveServiceTemplateFromDbErrorBuilder;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;

public class ServiceModelDaoZusammenImpl
    implements ServiceModelDao<ToscaServiceModel, ServiceElement> {
  private static final String BASE_PROPERTY = "base";
  private static final Logger logger = LoggerFactory.getLogger(ServiceModelDaoZusammenImpl.class);

  protected ZusammenAdaptor zusammenAdaptor;
  protected ElementType elementType;

  public ServiceModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
    this.elementType = ElementType.ServiceModel;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public ToscaServiceModel getServiceModel(String vspId, Version version) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<ElementInfo> serviceModel = getServiceModelElementInfo(context, elementContext);
    if (!serviceModel.isPresent()) {
      return null;
    }

    Id serviceModelElementId = serviceModel.get().getId();
    Map<String, ServiceTemplate> serviceTemplates =
        getTemplates(context, elementContext, serviceModelElementId);
    if (serviceTemplates == null) {
      return null;
    }

    FileContentHandler artifacts = getArtifacts(context, elementContext, serviceModelElementId);
    String entryDefinitionServiceTemplate =
        serviceModel.get().getInfo().getProperty(BASE_PROPERTY);

    return new ToscaServiceModel(artifacts, serviceTemplates, entryDefinitionServiceTemplate);
  }

  @Override
  public void storeServiceModel(String vspId, Version version, ToscaServiceModel serviceModel) {
    logger.info("Storing service model for VendorSoftwareProduct id -> {}", vspId);

    ZusammenElement templatesElement = buildStructuralElement(ElementType.Templates, Action.UPDATE);
    serviceModel.getServiceTemplates().entrySet().forEach(entry -> templatesElement.addSubElement(
        buildServiceTemplateElement(entry.getKey(), entry.getValue(),
            serviceModel.getEntryDefinitionServiceTemplate(), Action.CREATE)));

    ZusammenElement artifactsElement = buildStructuralElement(ElementType.Artifacts, Action.UPDATE);
    if (Objects.nonNull(serviceModel.getArtifactFiles())) {
      serviceModel.getArtifactFiles().getFiles().entrySet()
          .forEach(entry -> artifactsElement.addSubElement(
              buildArtifactElement(entry.getKey(), entry.getValue(), Action.CREATE)));
    }

    ZusammenElement serviceModelElement = buildStructuralElement(elementType, Action.UPDATE);
    serviceModelElement.getInfo()
        .addProperty(BASE_PROPERTY, serviceModel.getEntryDefinitionServiceTemplate());

    serviceModelElement.addSubElement(templatesElement);
    serviceModelElement.addSubElement(artifactsElement);

    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
    vspModel.addSubElement(serviceModelElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, vspModel, "Store service model");

    logger
        .info("Finished storing {} for VendorSoftwareProduct id -> {}", elementType.name(), vspId);
  }

  @Override
  public ServiceElement getServiceModelInfo(String vspId, Version version, String name) {
    return null;
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    logger.info("Started deleting content of Templates and Artifacts of {} of vsp {} version {}",
        elementType.name(), vspId, version.getId());

    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<ElementInfo> serviceModel = getServiceModelElementInfo(context, elementContext);
    if (!serviceModel.isPresent()) {
      logger.info("{} of vsp {} version {} does not exist - nothing to delete", elementType.name(),
          vspId, version.getId());
      return;
    }

    ZusammenElement serviceModelElement = buildElement(serviceModel.get().getId(), Action.IGNORE);
    for (Id serviceModelSubElementId :
        serviceModel.get().getSubElements().stream()
            .map(ElementInfo::getId)
            .collect(Collectors.toSet())) {
      ElementInfo serviceModelSubElementInfo =
          zusammenAdaptor.getElementInfo(context, elementContext, serviceModelSubElementId)
              .orElseThrow(() -> new IllegalStateException(String.format(
                  "Element %s declared as sub element of element %s (%s) does not exist",
                  serviceModelSubElementId.getValue(),
                  serviceModel.get().getId().getValue(),
                  elementType.name())));

      if (ElementType.Templates.name().equals(serviceModelSubElementInfo.getInfo().getName())
          || ElementType.Artifacts.name().equals(serviceModelSubElementInfo.getInfo().getName())) {
        ZusammenElement serviceModelSubElement =
            buildElement(serviceModelSubElementId, Action.IGNORE);
        serviceModelSubElement.setSubElements(serviceModelSubElementInfo.getSubElements().stream()
            .map(elementInfo -> buildElement(elementInfo.getId(), Action.DELETE))
            .collect(Collectors.toSet()));
        serviceModelElement.addSubElement(serviceModelSubElement);
      }
    }

    zusammenAdaptor.saveElement(context, elementContext, serviceModelElement,
        String.format("Delete content of Templates and Artifacts of %s", elementType.name()));

    logger.info("Finished deleting content of Templates and Artifacts of {} of vsp {} version {}",
        elementType.name(), vspId, version.getId());
  }

  private Optional<ElementInfo> getServiceModelElementInfo(SessionContext context,
                                                           ElementContext elementContext) {
    Collection<ElementInfo> vspModelSubs = zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.VspModel.name());

    return vspModelSubs.stream()
        .filter(elementInfo -> elementInfo.getInfo() != null
            && elementType.name().equals(elementInfo.getInfo().getName()))
        .findFirst();
  }

  private Map<String, ServiceTemplate> getTemplates(SessionContext context,
                                                    ElementContext elementContext,
                                                    Id serviceModelElementId) {
    Optional<ElementInfo> templatesElementInfo = zusammenAdaptor.getElementInfoByName(
        context, elementContext, serviceModelElementId, ElementType.Templates.name());

    if (templatesElementInfo.isPresent()) {
      Collection<Element> elements = zusammenAdaptor.listElementData(context, elementContext,
          templatesElementInfo.get().getId());

      return elements.stream().collect(Collectors.toMap(
          element -> element.getInfo().getName(),
          this::elementToServiceTemplate));
    }
    return null;
  }

  private FileContentHandler getArtifacts(SessionContext context, ElementContext elementContext,
                                          Id serviceModelElementId) {
    Optional<ElementInfo> artifactsElement = zusammenAdaptor.getElementInfoByName(
        context, elementContext, serviceModelElementId, ElementType.Artifacts.name());

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

  private Element buildServiceTemplateElement(String name, ServiceTemplate serviceTemplate,
                                              String entryDefinitionServiceTemplate,
                                              Action action) {
    ZusammenElement zusammenElement = buildElement(null, action);
    Info info = new Info();
    info.setName(name);
    info.setDescription(serviceTemplate.getDescription());
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.ServiceTemplate.name());
    info.addProperty(BASE_PROPERTY, entryDefinitionServiceTemplate);
    String yaml = new ToscaExtensionYamlUtil().objectToYaml(serviceTemplate);
    zusammenElement.setData(new ByteArrayInputStream(yaml.getBytes()));
    zusammenElement.setInfo(info);
    return zusammenElement;
  }

  protected ZusammenElement buildArtifactElement(String name, byte[] artifact, Action action) {
    ZusammenElement artifactElement = buildElement(null, action);
    Info info = new Info();
    info.setName(name);
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.Artifact.name());
    artifactElement.setInfo(info);
    artifactElement.setData(new ByteArrayInputStream(artifact));

    return artifactElement;
  }


  private ServiceTemplate elementToServiceTemplate(Element element) {
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
}
