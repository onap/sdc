package org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.sdc.vendorlicense.dao.impl.zusammen.VlmZusammenUtil.toInteger;


public class ElementToLicenseKeyGroupConvertor extends ElementConvertor {
  @Override
  public LicenseKeyGroupEntity convert(Element element) {
    if (element == null) {
      return null;
    }
    return mapElementToLicenseKeyGroupEntity(element);

  }

  @Override
  public LicenseKeyGroupEntity convert(ElementInfo elementInfo) {
    if (elementInfo == null) {
      return null;
    }
    return mapElementInfoToLicenseKeyGroupEntity(elementInfo);

  }

  private LicenseKeyGroupEntity mapElementToLicenseKeyGroupEntity(
      Element element) {
    LicenseKeyGroupEntity licenseKeyGroup =
        new LicenseKeyGroupEntity();
    licenseKeyGroup.setId(element.getElementId().getValue());
    mapInfoToLicenseKeyGroup(licenseKeyGroup, element.getInfo());
    mapRelationsToLicenseKeyGroup(licenseKeyGroup, element.getRelations());
    return licenseKeyGroup;
  }


  private LicenseKeyGroupEntity mapElementInfoToLicenseKeyGroupEntity(ElementInfo elementInfo) {
    LicenseKeyGroupEntity licenseKeyGroup =
        new LicenseKeyGroupEntity();
    licenseKeyGroup.setId(elementInfo.getId().getValue());

    mapInfoToLicenseKeyGroup(licenseKeyGroup, elementInfo.getInfo());
    mapRelationsToLicenseKeyGroup(licenseKeyGroup, elementInfo.getRelations());
    return licenseKeyGroup;
  }

  private void mapInfoToLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup, Info info) {
    licenseKeyGroup.setName(info.getName());
    licenseKeyGroup.setDescription(info.getDescription());
    licenseKeyGroup.setVersionUuId(info.getProperty("version_uuid"));
    licenseKeyGroup.setType(LicenseKeyType.valueOf(info.getProperty("LicenseKeyType")));
    licenseKeyGroup.setOperationalScope(
        getOperationalScopeMultiChoiceOrOther(info.getProperty("operational_scope")));
    licenseKeyGroup.setStartDate(info.getProperty("startDate"));
    licenseKeyGroup.setExpiryDate(info.getProperty("expiryDate"));

    String thresholdUnit = info.getProperty("thresholdUnits");
    licenseKeyGroup
        .setThresholdUnits(thresholdUnit == null ? null : ThresholdUnit.valueOf(thresholdUnit));

    licenseKeyGroup.setThresholdValue(toInteger(info.getProperty("thresholdValue")));
    licenseKeyGroup.setIncrements(info.getProperty("increments"));
  }

  private void mapRelationsToLicenseKeyGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                             Collection<Relation> relations) {
    if (relations != null && relations.size() > 0) {
      licenseKeyGroup
          .setReferencingFeatureGroups((relations.stream().map(relation -> relation
              .getEdge2().getElementId().getValue()).collect(Collectors.toSet())));
    }
  }

  private MultiChoiceOrOther<OperationalScope> getOperationalScopeMultiChoiceOrOther(
      Map<String, Object> operationalScope) {
    if (operationalScope == null || operationalScope.isEmpty()) {
      return null;
    }

    Set<OperationalScope> choices = new HashSet<>();
    ((List<String>) operationalScope.get("choices")).
        forEach(choice -> choices.add(OperationalScope.valueOf(choice)));

    Object other = operationalScope.get("other");
    return new MultiChoiceOrOther<>(choices, other == null ? null : (String) other);
  }
}
