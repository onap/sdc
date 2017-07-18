package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Avrahamg.
 * @since March 21, 2017
 */
public class ComponentArtifactDaoZusammenImpl implements ComponentArtifactDao {

  private static final String ARTIFACT_NAME = "name";

  private ZusammenAdaptor zusammenAdaptor;

  public ComponentArtifactDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public Optional<ComponentMonitoringUploadEntity> getByType(
      ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(componentMonitoringUploadEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(componentMonitoringUploadEntity.getVersion()));
    Optional<Element> mibsElement =
        zusammenAdaptor.getElementByName(context, elementContext, new Id(
            componentMonitoringUploadEntity
            .getComponentId()), StructureElement.Mibs.toString());
    if (mibsElement.isPresent()) {
      Optional<Element> monitoringElement = zusammenAdaptor
          .getElementByName(context, elementContext, mibsElement.get().getElementId(),
              getMonitoringStructuralElement(componentMonitoringUploadEntity.getType())
                  .toString());
      if (monitoringElement.isPresent()) {
        componentMonitoringUploadEntity.setId(monitoringElement.get().getElementId().getValue());
        componentMonitoringUploadEntity
            .setArtifactName(
                (String) monitoringElement.get().getInfo().getProperties().get(ARTIFACT_NAME));
        componentMonitoringUploadEntity
            .setArtifact(ByteBuffer.wrap(FileUtils.toByteArray(monitoringElement.get().getData())));
        return Optional.of(componentMonitoringUploadEntity);
      }
    }

    return Optional.empty();
  }

  @Override
  public void create(ComponentMonitoringUploadEntity entity) {
    ZusammenElement mibElement = buildMibElement(entity);

    ZusammenElement mibsElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Mibs, null);

    ZusammenElement componentElement = buildComponentElement(entity);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> savedElement = zusammenAdaptor.saveElement(context, elementContext,
        VspZusammenUtil.aggregateElements(componentElement, mibsElement, mibElement),
        "Create monitoring upload");
    savedElement.ifPresent(element ->
        entity.setId(element.getSubElements().iterator().next()
            .getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void delete(ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    ZusammenElement mibElement = new ZusammenElement();
    mibElement.setElementId(new Id(componentMonitoringUploadEntity.getId()));
    mibElement.setAction(Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(componentMonitoringUploadEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context, elementContext, mibElement,
        String.format("Delete mib with id %s", componentMonitoringUploadEntity.getId()));
  }

  @Override
  public Collection<ComponentMonitoringUploadEntity> list(ComponentMonitoringUploadEntity mib) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(mib.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(mib.getVersion()));

    return zusammenAdaptor.listElementsByName(context, elementContext, new Id(mib.getComponentId()),
        StructureElement.Mibs.toString()).stream()
        .map(elementInfo ->
            mapElementInfoToMib(mib.getVspId(), mib.getVersion(), mib.getComponentId(),
                elementInfo))
        .collect(Collectors.toList());
  }

  @Override
  public void deleteAll(ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    ZusammenElement mibsElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Mibs, Action.DELETE);

    ZusammenElement componentElement = buildComponentElement(componentMonitoringUploadEntity);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(componentMonitoringUploadEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context, elementContext,
        VspZusammenUtil.aggregateElements(componentElement, mibsElement), "Delete mibs");
  }

  @Override
  public Collection<ComponentMonitoringUploadEntity> listArtifacts(
      ComponentMonitoringUploadEntity monitoringUploadEntity) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(monitoringUploadEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(monitoringUploadEntity.getVersion()));

    final Optional<Element> elementByName =
        zusammenAdaptor.getElementByName(context, elementContext,
            new Id(monitoringUploadEntity.getComponentId()
        ), StructureElement.Mibs.name());

    if(!elementByName.isPresent())
      return null;
    else {
      final Id elementId = elementByName.get().getElementId();
      return zusammenAdaptor.listElementData(context, elementContext, elementId).stream()
          .map(element ->
              buildMibEntity(element, monitoringUploadEntity)
          ).collect(Collectors.toList());
    }
  }

  private ComponentMonitoringUploadEntity buildMibEntity(Element element,
                                                         ComponentMonitoringUploadEntity monitoringUploadEntity) {
    final String componentId = monitoringUploadEntity.getComponentId();
    ComponentMonitoringUploadEntity
        createdMib = new ComponentMonitoringUploadEntity(monitoringUploadEntity.getVspId(),
        monitoringUploadEntity.getVersion(),
        componentId,
        null);
    createdMib.setArtifactName((String) element.getInfo().getProperties().get(ARTIFACT_NAME));
    createdMib.setArtifact(ByteBuffer.wrap(FileUtils.toByteArray(element.getData())));
    createdMib.setType(MonitoringUploadType.valueOf(element.getInfo().getName()));
    return createdMib;
  }

  private ZusammenElement buildComponentElement(
      ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setElementId(new Id(componentMonitoringUploadEntity.getComponentId()));
    componentElement.setAction(Action.IGNORE);
    return componentElement;
  }

  private ZusammenElement buildMibElement(ComponentMonitoringUploadEntity monitoringUploadEntity) {
    ZusammenElement monitoringElement = VspZusammenUtil
        .buildStructuralElement(getMonitoringStructuralElement(monitoringUploadEntity.getType()),
            Action.UPDATE);
    monitoringElement.getInfo().getProperties()
        .put(ARTIFACT_NAME, monitoringUploadEntity.getArtifactName());
    monitoringElement
        .setData(new ByteArrayInputStream(monitoringUploadEntity.getArtifact().array()));
    return monitoringElement;
  }

  private ComponentMonitoringUploadEntity mapElementInfoToMib(String vspId, Version version,
                                                              String componentId,
                                                              ElementInfo elementInfo) {
    ComponentMonitoringUploadEntity
        monitoringUploadEntity = new ComponentMonitoringUploadEntity(vspId, version, componentId,
        elementInfo.getId().getValue());
    monitoringUploadEntity
        .setArtifactName((String) elementInfo.getInfo().getProperties().get(ARTIFACT_NAME));
    monitoringUploadEntity.setType(MonitoringUploadType.valueOf(elementInfo.getInfo().getName()));
    return monitoringUploadEntity;
  }

  private StructureElement getMonitoringStructuralElement(MonitoringUploadType type)
      throws IllegalArgumentException {
    switch (type) {
      case SNMP_POLL:
        return StructureElement.SNMP_POLL;
      case SNMP_TRAP:
        return StructureElement.SNMP_TRAP;
      case VES_EVENTS:
        return StructureElement.VES_EVENTS;
      default:
        throw new IllegalArgumentException();
    }
  }
}
