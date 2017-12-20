package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToComponentMonitoringUploadConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

/**
 * @author Avrahamg.
 * @since March 21, 2017
 */
public class ComponentArtifactDaoZusammenImpl implements ComponentArtifactDao {

  private static final String ARTIFACT_NAME = "artifactName";

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
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(componentMonitoringUploadEntity.getVspId(),
            componentMonitoringUploadEntity.getVersion().getId());

    Optional<Element> mibsElement =
        zusammenAdaptor.getElementByName(context, elementContext,
            new Id(componentMonitoringUploadEntity.getComponentId()), ElementType.Mibs.toString());
    if (mibsElement.isPresent()) {
      Optional<Element> monitoringElement = zusammenAdaptor
          .getElementByName(context, elementContext, mibsElement.get().getElementId(),
              getMonitoringStructuralElement(componentMonitoringUploadEntity.getType()).toString());
      if (monitoringElement.isPresent()) {
        ComponentMonitoringUploadEntity entity =
            new ElementToComponentMonitoringUploadConvertor().convert(monitoringElement.get());
        entity.setVspId(componentMonitoringUploadEntity.getVspId());
        entity.setVersion(componentMonitoringUploadEntity.getVersion());
        entity.setComponentId(componentMonitoringUploadEntity.getComponentId());
        return Optional.of(entity);
      }
    }

    return Optional.empty();
  }

  @Override
  public void create(ComponentMonitoringUploadEntity entity) {
    ZusammenElement mibElement = buildMibElement(entity);

    ZusammenElement mibsElement = buildStructuralElement(ElementType.Mibs, null);

    ZusammenElement componentElement = buildElement(new Id(entity.getComponentId()), Action.IGNORE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(entity.getVspId(), entity.getVersion().getId());

    Element savedElement = zusammenAdaptor.saveElement(context, elementContext,
        VspZusammenUtil.aggregateElements(componentElement, mibsElement, mibElement),
        "Create monitoring upload");
    entity.setId(savedElement.getSubElements().iterator().next()
        .getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void delete(ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {

    ZusammenElement mibElement = buildMibElementStructure(componentMonitoringUploadEntity);
    mibElement.setElementId(new Id(componentMonitoringUploadEntity.getId()));
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(componentMonitoringUploadEntity.getVspId(),
            componentMonitoringUploadEntity.getVersion().getId());

    zusammenAdaptor.saveElement(context, elementContext, mibElement,
        String.format("Delete mib with id %s", componentMonitoringUploadEntity.getId()));
  }



  @Override
  public Collection<ComponentMonitoringUploadEntity> list(
      ComponentMonitoringUploadEntity mibEntity) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(mibEntity.getVspId(), mibEntity.getVersion().getId());
    ElementToComponentMonitoringUploadConvertor
        convertor = new ElementToComponentMonitoringUploadConvertor();
    return zusammenAdaptor
        .listElementsByName(context, elementContext, new Id(mibEntity.getComponentId()),
            ElementType.Mibs.toString()).stream()
        .map(elementInfo -> convertor.convert(elementInfo))
        .map(mib -> {
          mib.setVspId(mibEntity.getVspId());
          mib.setVersion(mibEntity.getVersion());
          mib.setComponentId(mibEntity.getComponentId());
          return mib;
        })
        .collect(Collectors.toList());
  }

  @Override
  public void deleteAll(ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    /*ZusammenElement mibsElement =
        buildStructuralElement(ElementType.Mibs, Action.DELETE);

    ZusammenElement componentElement = buildComponentElement(componentMonitoringUploadEntity);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(componentMonitoringUploadEntity.getVspId(),
            componentMonitoringUploadEntity.getVersion().getId());

    zusammenAdaptor.saveElement(context, elementContext,
        VspaggregateElements(componentElement, mibsElement), "Delete mibs");

*/
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(componentMonitoringUploadEntity.getVspId(),
            componentMonitoringUploadEntity.getVersion().getId());

    Optional<Element> optionalElement = zusammenAdaptor.getElementByName(context,
        elementContext, new Id(componentMonitoringUploadEntity.getComponentId()), ElementType.Mibs
            .name());

    if (optionalElement.isPresent()) {
      Element mibsElement = optionalElement.get();
      Collection<Element> mibs = mibsElement.getSubElements();

      mibs.forEach(mib -> {
        ZusammenElement mibZusammenElement = buildElement(mib.getElementId(), Action.DELETE);
        zusammenAdaptor.saveElement(context, elementContext, mibZusammenElement,
            "Delete mib with id " + mib.getElementId());
      });
    }
  }

  @Override
  public Collection<ComponentMonitoringUploadEntity> listArtifacts(
      ComponentMonitoringUploadEntity monitoringUploadEntity) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(monitoringUploadEntity.getVspId(),
        monitoringUploadEntity.getVersion().getId());

    final Optional<Element> elementByName =
        zusammenAdaptor.getElementByName(context, elementContext,
            new Id(monitoringUploadEntity.getComponentId()), ElementType.Mibs.name());

    if (!elementByName.isPresent()) {
      return null;
    } else {
      final Id elementId = elementByName.get().getElementId();
      return zusammenAdaptor.listElementData(context, elementContext, elementId).stream()
          .map(element -> buildMibEntity(element, monitoringUploadEntity))
          .collect(Collectors.toList());
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

  private ZusammenElement buildMibElement(
      ComponentMonitoringUploadEntity monitoringUploadEntity) {

    ZusammenElement monitoringElement = buildMibElementStructure(monitoringUploadEntity);
    monitoringElement.getInfo().getProperties()
        .put(ARTIFACT_NAME, monitoringUploadEntity.getArtifactName());
    monitoringElement
        .setData(new ByteArrayInputStream(monitoringUploadEntity.getArtifact().array()));
    return monitoringElement;
  }

  private ZusammenElement buildMibElementStructure(
      ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
    ZusammenElement monitoringElement =
        buildStructuralElement(getMonitoringStructuralElement(componentMonitoringUploadEntity.getType()),
            Action.UPDATE);
    return monitoringElement;
  }

  private ElementType getMonitoringStructuralElement(MonitoringUploadType type)
      throws IllegalArgumentException {
    switch (type) {
      case SNMP_POLL:
        return ElementType.SNMP_POLL;
      case SNMP_TRAP:
        return ElementType.SNMP_TRAP;
      case VES_EVENTS:
        return ElementType.VES_EVENTS;
      default:
        throw new IllegalArgumentException();
    }
  }
}
