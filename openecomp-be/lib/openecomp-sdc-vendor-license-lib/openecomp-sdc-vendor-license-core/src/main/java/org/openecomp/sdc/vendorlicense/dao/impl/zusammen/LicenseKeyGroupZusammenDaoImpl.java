package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToLicenseKeyGroupConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.types.ElementPropertyName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;


public class LicenseKeyGroupZusammenDaoImpl implements LicenseKeyGroupDao {
  private ZusammenAdaptor zusammenAdaptor;

  public LicenseKeyGroupZusammenDaoImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    //no need
  }

  @Override
  public void create(LicenseKeyGroupEntity licenseKeyGroup) {
    ZusammenElement licenseKeyGroupElement =
        buildLicenseKeyGroupElement(licenseKeyGroup, Action.CREATE);

    ZusammenElement limitsElement = buildStructuralElement(ElementType.Limits, Action.CREATE);
    licenseKeyGroupElement.addSubElement(limitsElement);

    ZusammenElement lkgsElement =
        buildStructuralElement(ElementType.LicenseKeyGroups, Action.IGNORE);
    lkgsElement.addSubElement(licenseKeyGroupElement);

    SessionContext context = createSessionContext();
    Element lkgsSavedElement = zusammenAdaptor.saveElement(context,
        new ElementContext(licenseKeyGroup.getVendorLicenseModelId(),
            licenseKeyGroup.getVersion().getId()), lkgsElement, "Create license Key Group");

    licenseKeyGroup
        .setId(lkgsSavedElement.getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void update(LicenseKeyGroupEntity licenseKeyGroup) {
    ZusammenElement licenseKeyGroupElement =
        buildLicenseKeyGroupElement(licenseKeyGroup, Action.UPDATE);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().getId());

    Optional<ElementInfo> lkgFromDb = zusammenAdaptor.getElementInfo(context, elementContext,
        new Id(licenseKeyGroup.getId()));

    if (lkgFromDb.isPresent()) {

      if (licenseKeyGroupElement.getRelations() == null) {
        licenseKeyGroupElement.setRelations(new ArrayList<>());
      }

      if (lkgFromDb.get().getRelations() != null && lkgFromDb.get().getRelations().size() > 0) {
        licenseKeyGroupElement.getRelations().addAll(lkgFromDb.get().getRelations());
      }
    }
    zusammenAdaptor.saveElement(context, elementContext, licenseKeyGroupElement,
        String.format("Update license key group with id %s", licenseKeyGroup.getId()));
  }

  @Override
  public LicenseKeyGroupEntity get(LicenseKeyGroupEntity licenseKeyGroup) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().getId());
    ElementToLicenseKeyGroupConvertor convertor = new ElementToLicenseKeyGroupConvertor();
    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseKeyGroup.getId()))
        .map(elementInfo -> {
          LicenseKeyGroupEntity entity = convertor.convert(elementInfo);
          entity.setVendorLicenseModelId(licenseKeyGroup.getVendorLicenseModelId());
          entity.setVersion(licenseKeyGroup.getVersion());
          return entity;
        })
        .orElse(null);
  }

  @Override
  public void delete(LicenseKeyGroupEntity licenseKeyGroup) {
    ZusammenElement zusammenElement = buildElement(new Id(licenseKeyGroup.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().getId());

    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete license key group. id:" + licenseKeyGroup.getId() + ".");
  }

  @Override
  public Collection<LicenseKeyGroupEntity> list(LicenseKeyGroupEntity licenseKeyGroup) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().getId());
    ElementToLicenseKeyGroupConvertor convertor = new ElementToLicenseKeyGroupConvertor();
    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.LicenseKeyGroups.name())
        .stream().map(elementInfo -> {
          LicenseKeyGroupEntity entity = convertor.convert(elementInfo);
          entity.setVendorLicenseModelId(licenseKeyGroup.getVendorLicenseModelId());
          entity.setVersion(licenseKeyGroup.getVersion());
          return entity;
        })
        .collect(Collectors.toList());
  }

  @Override
  public long count(LicenseKeyGroupEntity licenseKeyGroup) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().getId());

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.LicenseKeyGroups.name())
        .size();
  }

  @Override
  public void deleteAll(LicenseKeyGroupEntity licenseKeyGroup) {
    //not supported
  }

  @Override
  public void removeReferencingFeatureGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                            String featureGroupId) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().getId());

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseKeyGroup.getId()));

    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      zusammenElement.setRelations(elementInfo.get().getRelations().stream()
          .filter(relation -> !featureGroupId
              .equals(relation.getEdge2().getElementId().getValue()))
          .collect(Collectors.toList()));

      zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
          "remove referencing feature group");
    }
  }

  @Override
  public void addReferencingFeatureGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                         String featureGroupId) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseKeyGroup.getVendorLicenseModelId(),
        licenseKeyGroup.getVersion().getId());

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseKeyGroup.getId()));

    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      if (zusammenElement.getRelations() == null) {
        zusammenElement.setRelations(new ArrayList<>());
      }
      zusammenElement.getRelations().add(VlmZusammenUtil
          .createRelation(RelationType.LicenseKeyGroupToReferencingFeatureGroup,
              featureGroupId));
      zusammenAdaptor
          .saveElement(context, elementContext, zusammenElement, "add referencing feature group");
    }
  }

  private ZusammenElement buildLicenseKeyGroupElement(LicenseKeyGroupEntity licenseKeyGroup,
                                                      Action action) {
    ZusammenElement lkgElement =
        buildElement(licenseKeyGroup.getId() == null ? null : new Id(licenseKeyGroup.getId()),
            action);
    Info info = new Info();
    info.setName(licenseKeyGroup.getName());
    info.setDescription(licenseKeyGroup.getDescription());
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.LicenseKeyGroup);
    info.addProperty("version_uuid", licenseKeyGroup.getVersionUuId());
    info.addProperty("LicenseKeyType", licenseKeyGroup.getType());
    info.addProperty("operational_scope", licenseKeyGroup.getOperationalScope());
    info.addProperty("startDate", licenseKeyGroup.getStartDate());
    info.addProperty("expiryDate", licenseKeyGroup.getExpiryDate());
    info.addProperty("thresholdValue", licenseKeyGroup.getThresholdValue());
    info.addProperty("thresholdUnits", licenseKeyGroup.getThresholdUnits());
    info.addProperty("increments", licenseKeyGroup.getIncrements());
    lkgElement.setInfo(info);

    if (licenseKeyGroup.getReferencingFeatureGroups() != null
        && licenseKeyGroup.getReferencingFeatureGroups().size() > 0) {
      lkgElement.setRelations(licenseKeyGroup.getReferencingFeatureGroups().stream()
          .map(rel -> VlmZusammenUtil
              .createRelation(RelationType.LicenseKeyGroupToReferencingFeatureGroup, rel))
          .collect(Collectors.toList()));
    }
    return lkgElement;
  }


}
