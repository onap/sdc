package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(deploymentFlavor.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(deploymentFlavor.getVersion()));

    return listDeploymentFlavor(zusammenAdaptor, context, elementContext, deploymentFlavor.getVspId(),
        deploymentFlavor.getVersion());
  }

  static Collection<DeploymentFlavorEntity> listDeploymentFlavor(ZusammenAdaptor zusammenAdaptor,
                                                    SessionContext context,
                                                    ElementContext elementContext,
                                                    String vspId, Version version) {
    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.DeploymentFlavors.name())
        .stream().map(elementInfo -> mapElementInfoToComponent(vspId, version, elementInfo))
        .collect(Collectors.toList());
  }

  private static DeploymentFlavorEntity mapElementInfoToComponent(String vspId, Version version,
                                                           ElementInfo elementInfo) {
    DeploymentFlavorEntity deploymentFlavorEntity =
        new DeploymentFlavorEntity(vspId, version, elementInfo.getId().getValue());
    deploymentFlavorEntity.setCompositionData(
        elementInfo.getInfo().getProperty(ElementPropertyName.compositionData.name()));
    return deploymentFlavorEntity;
  }

  @Override
  public void create(DeploymentFlavorEntity deploymentFlavor) {
    ZusammenElement deploymentFlavorElement = deploymentFlavorToZusammen(deploymentFlavor,
        Action.CREATE);
    ZusammenElement deploymentFlavorElements =
        VspZusammenUtil.buildStructuralElement(StructureElement.DeploymentFlavors, null);
    deploymentFlavorElements.getSubElements().add(deploymentFlavorElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(deploymentFlavor.getVspId());
    Optional<Element> savedElement = zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        deploymentFlavorElements, "Create deloymentFlavor");
    savedElement.ifPresent(element ->
        deploymentFlavor.setId(element.getSubElements().iterator().next().getElementId()
            .getValue()));
  }

  @Override
  public void update(DeploymentFlavorEntity deploymentFlavor) {
    ZusammenElement deploymentFlavorElement = deploymentFlavorToZusammen(deploymentFlavor,
        Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(deploymentFlavor.getVspId());
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        deploymentFlavorElement, String.format("Update deloymentFlavor with id %s",
            deploymentFlavor.getId()));
  }

  @Override
  public DeploymentFlavorEntity get(DeploymentFlavorEntity deploymentFlavor) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(deploymentFlavor.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(deploymentFlavor.getVersion()));

    Optional<Element> element =
        zusammenAdaptor.getElement(context, elementContext, deploymentFlavor.getId());

    if (element.isPresent()) {
      deploymentFlavor.setCompositionData(new String(FileUtils.toByteArray(element.get()
          .getData())));
      return deploymentFlavor;
    }
    return null;
  }

  @Override
  public void delete(DeploymentFlavorEntity deploymentFlavor) {
    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setElementId(new Id(deploymentFlavor.getId()));
    componentElement.setAction(Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(deploymentFlavor.getVspId());
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        componentElement, String.format("Delete deloymentFlavor with id %s",
            deploymentFlavor.getId()));
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    ZusammenElement deploymentFlavorsElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.DeploymentFlavors, Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        deploymentFlavorsElement, "Delete all deploymentFlavors");
  }

  private ZusammenElement deploymentFlavorToZusammen(DeploymentFlavorEntity deploymentFlavor,
                                                     Action action) {
    ZusammenElement deploymentFlavorElement = buildDeploymentFlavorElement
        (deploymentFlavor, action);

    return deploymentFlavorElement;
  }

  /*private ZusammenElement deplymentFlavorQuestionnaireToZusammen(String questionnaireData,
                                                           Action action) {
    ZusammenElement questionnaireElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Questionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }*/

  private ZusammenElement buildDeploymentFlavorElement(DeploymentFlavorEntity deploymentFlavor,
                                                       Action action) {
    ZusammenElement deploymentFlavorElement = new ZusammenElement();
    deploymentFlavorElement.setAction(action);
    if (deploymentFlavor.getId() != null) {
      deploymentFlavorElement.setElementId(new Id(deploymentFlavor.getId()));
    }
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.DeploymentFlavor);
    info.addProperty(ElementPropertyName.compositionData.name(), deploymentFlavor
        .getCompositionData());
    deploymentFlavorElement.setInfo(info);
    deploymentFlavorElement.setData(new ByteArrayInputStream(deploymentFlavor.getCompositionData()
        .getBytes()));
    return deploymentFlavorElement;
  }

}
