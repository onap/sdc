package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MibDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.MibEntity;
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
public class MibDaoZusammenImpl implements MibDao {

  private static final String ARTIFACT_NAME = "name";

  private ZusammenAdaptor zusammenAdaptor;

  public MibDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public Optional<MibEntity> getByType(MibEntity mibEntity) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(mibEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(mibEntity.getVersion()));

    Optional<Element> mibsElement =
        zusammenAdaptor.getElementByName(context, elementContext, new Id(mibEntity
            .getComponentId()), StructureElement.Mibs.toString());
    if (mibsElement.isPresent()) {
      Optional<Element> mibElement = zusammenAdaptor
          .getElementByName(context, elementContext, mibsElement.get().getElementId(),
              getMibStructuralElement(mibEntity.getType()).toString());
      if (mibElement.isPresent()) {
        mibEntity.setId(mibElement.get().getElementId().getValue());
        mibEntity.setArtifactName((String) mibElement.get().getInfo().getProperties().get(ARTIFACT_NAME));
        mibEntity.setArtifact(ByteBuffer.wrap(FileUtils.toByteArray(mibElement.get().getData())));
        return Optional.of(mibEntity);
      }
    }

    return Optional.empty();
  }

  @Override
  public void create(MibEntity mibEntity) {
    ZusammenElement mibElement = buildMibElement(mibEntity);

    ZusammenElement mibsElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Mibs, null);

    ZusammenElement componentElement = buildComponentElement(mibEntity);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(mibEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> savedElement = zusammenAdaptor.saveElement(context, elementContext,
        VspZusammenUtil.aggregateElements(componentElement, mibsElement, mibElement),
        "Create mib");
    savedElement.ifPresent(element ->
        mibEntity.setId(element.getSubElements().iterator().next()
            .getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void delete(MibEntity mibEntity) {
    ZusammenElement mibElement = new ZusammenElement();
    mibElement.setElementId(new Id(mibEntity.getId()));
    mibElement.setAction(Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(mibEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context, elementContext, mibElement,
        String.format("Delete mib with id %s", mibEntity.getId()));
  }

  @Override
  public Collection<MibEntity> list(MibEntity mib) {
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
  public void deleteAll(MibEntity mibEntity) {
    ZusammenElement mibsElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Mibs, Action.DELETE);

    ZusammenElement componentElement = buildComponentElement(mibEntity);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(mibEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context, elementContext,
        VspZusammenUtil.aggregateElements(componentElement, mibsElement), "Delete mibs");
  }

  private ZusammenElement buildComponentElement(MibEntity mibEntity) {
    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setElementId(new Id(mibEntity.getComponentId()));
    componentElement.setAction(Action.IGNORE);
    return componentElement;
  }

  private ZusammenElement buildMibElement(MibEntity mib) {
    ZusammenElement mibElement = VspZusammenUtil
        .buildStructuralElement(getMibStructuralElement(mib.getType()), Action.UPDATE);
    mibElement.getInfo().getProperties().put(ARTIFACT_NAME, mib.getArtifactName());
    mibElement.setData(new ByteArrayInputStream(mib.getArtifact().array()));
    return mibElement;
  }

  private MibEntity mapElementInfoToMib(String vspId, Version version, String componentId,
                                        ElementInfo elementInfo) {
    MibEntity mib = new MibEntity(vspId, version, componentId, elementInfo.getId().getValue());
    mib.setArtifactName((String) elementInfo.getInfo().getProperties().get(ARTIFACT_NAME));
    mib.setType(ArtifactType.valueOf(elementInfo.getInfo().getName()));
    return mib;
  }

  private StructureElement getMibStructuralElement(ArtifactType type) {
    switch (type) {
      case SNMP_POLL:
        return StructureElement.SNMP_POLL;
      case SNMP_TRAP:
        return StructureElement.SNMP_TRAP;
      default:
        throw new IllegalArgumentException();
    }
  }
}
