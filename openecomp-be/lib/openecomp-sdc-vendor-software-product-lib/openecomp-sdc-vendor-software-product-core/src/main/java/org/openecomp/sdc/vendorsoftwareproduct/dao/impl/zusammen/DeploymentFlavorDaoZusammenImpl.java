package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToDeploymentFlavorConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class DeploymentFlavorDaoZusammenImpl implements DeploymentFlavorDao {

  private ZusammenAdaptor zusammenAdaptor;

  public DeploymentFlavorDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
  }

  @Override
  public Collection<DeploymentFlavorEntity> list(DeploymentFlavorEntity deploymentFlavor) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(deploymentFlavor.getVspId(), deploymentFlavor.getVersion().getId());

    return listDeploymentFlavor(zusammenAdaptor, context, elementContext,
        deploymentFlavor.getVspId(),
        deploymentFlavor.getVersion());
  }

  private static Collection<DeploymentFlavorEntity> listDeploymentFlavor(
      ZusammenAdaptor zusammenAdaptor,
      SessionContext context,
      ElementContext elementContext,
      String vspId, Version version) {
    ElementToDeploymentFlavorConvertor convertor = new ElementToDeploymentFlavorConvertor();
    return zusammenAdaptor
        .listElementsByName(context, elementContext, null,
            ElementType.DeploymentFlavors.name())
        .stream().map(elementInfo -> {
          DeploymentFlavorEntity entity = convertor.convert(
              elementInfo);
          entity.setVspId(vspId);
          entity.setVersion(version);
          return entity;
        })
        .collect(Collectors.toList());
  }


  @Override
  public void create(DeploymentFlavorEntity deploymentFlavor) {
    ZusammenElement deploymentFlavorElement = deploymentFlavorToZusammen(deploymentFlavor,
        Action.CREATE);
    ZusammenElement deploymentFlavorElements =
        buildStructuralElement(ElementType.DeploymentFlavors, Action.IGNORE);
    deploymentFlavorElements.getSubElements().add(deploymentFlavorElement);

    SessionContext context = createSessionContext();
    Element savedElement = zusammenAdaptor.saveElement(context,
        new ElementContext(deploymentFlavor.getVspId(), deploymentFlavor.getVersion().getId()),
        deploymentFlavorElements, "Create deloymentFlavor");
    deploymentFlavor.setId(savedElement.getSubElements().iterator().next().getElementId()
        .getValue());
  }

  @Override
  public void update(DeploymentFlavorEntity deploymentFlavor) {
    ZusammenElement deploymentFlavorElement = deploymentFlavorToZusammen(deploymentFlavor,
        Action.UPDATE);

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context,
        new ElementContext(deploymentFlavor.getVspId(), deploymentFlavor.getVersion().getId()),
        deploymentFlavorElement, String.format("Update deloymentFlavor with id %s",
            deploymentFlavor.getId()));
  }

  @Override
  public DeploymentFlavorEntity get(DeploymentFlavorEntity deploymentFlavor) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(deploymentFlavor.getVspId(), deploymentFlavor.getVersion().getId());

    Optional<Element> element =
        zusammenAdaptor.getElement(context, elementContext, deploymentFlavor.getId());

    if (element.isPresent()) {
      ElementToDeploymentFlavorConvertor convertor = new ElementToDeploymentFlavorConvertor();
      DeploymentFlavorEntity entity = convertor.convert(element.get());
      deploymentFlavor.setCompositionData(new String(FileUtils.toByteArray(element.get()
          .getData())));
      entity.setVspId(deploymentFlavor.getVspId());
      entity.setVersion(deploymentFlavor.getVersion());
      return entity;
    }
    return null;
  }

  @Override
  public void delete(DeploymentFlavorEntity deploymentFlavor) {
    ZusammenElement componentElement =
        buildElement(new Id(deploymentFlavor.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context,
        new ElementContext(deploymentFlavor.getVspId(), deploymentFlavor.getVersion().getId()),
        componentElement, String.format("Delete deloymentFlavor with id %s",
            deploymentFlavor.getId()));
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<Element> optionalElement = zusammenAdaptor.getElementByName(context,
        elementContext, null, ElementType.DeploymentFlavors.name());

    if (optionalElement.isPresent()) {
      Element deploymentFlavorsElement = optionalElement.get();
      Collection<Element> deploymentFlavors = deploymentFlavorsElement.getSubElements();

      deploymentFlavors.forEach(deplymentFlavor -> {
        ZusammenElement deplymentFlavorZusammenElement =
            buildElement(deplymentFlavor.getElementId(), Action.DELETE);
        zusammenAdaptor.saveElement(context,
            elementContext, deplymentFlavorZusammenElement, " Delete Deplyment Flavor with id "
                + deplymentFlavor.getElementId());
      });
    }
  }

  /*private ZusammenElement deplymentFlavorQuestionnaireToZusammen(String questionnaireData,
                                                           Action action) {
    ZusammenElement questionnaireElement =
        VspbuildStructuralElement(ElementType.Questionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }*/

  private ZusammenElement deploymentFlavorToZusammen(DeploymentFlavorEntity deploymentFlavor,
                                                     Action action) {
    ZusammenElement deploymentFlavorElement =
        buildElement(deploymentFlavor.getId() == null ? null : new Id(deploymentFlavor.getId()),
            action);
    Info info = new Info();
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.DeploymentFlavor);
    info.addProperty(ElementPropertyName.compositionData.name(), deploymentFlavor
        .getCompositionData());
    deploymentFlavorElement.setInfo(info);
    deploymentFlavorElement.setData(new ByteArrayInputStream(deploymentFlavor.getCompositionData()
        .getBytes()));
    return deploymentFlavorElement;
  }

}
