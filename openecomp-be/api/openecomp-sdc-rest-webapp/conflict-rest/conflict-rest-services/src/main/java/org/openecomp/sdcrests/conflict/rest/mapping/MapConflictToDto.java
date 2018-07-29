package org.openecomp.sdcrests.conflict.rest.mapping;

import org.openecomp.conflicts.types.Conflict;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdcrests.conflict.types.ConflictDto;
import org.openecomp.sdcrests.mapping.EchoMapMapping;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.*;
import org.openecomp.sdcrests.vendorlicense.types.*;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.*;
import org.openecomp.sdcrests.vsp.rest.mapping.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class MapConflictToDto extends MappingBase<Conflict, ConflictDto> {

  @Override
  public void doMapping(Conflict source, ConflictDto target) {
    target.setId(source.getId());
    target.setType(source.getType());
    target.setName(source.getName());

    Map.Entry<MappingBase, Class> mapperToTargetClass = getMapper(source.getType());
    target.setTheirs(map(mapperToTargetClass, source.getTheirs()));
    target.setYours(map(mapperToTargetClass, source.getYours()));
  }

  private Map map(Map.Entry<MappingBase, Class> mapperToTargetClass, Object object) {
    return JsonUtil
        .json2Object(JsonUtil.object2Json(
            mapperToTargetClass.getKey().applyMapping(object, mapperToTargetClass.getValue())),
            Map.class);
  }

  private Map.Entry<MappingBase, Class> getMapper(ElementType type) {
    switch (type) {
      case VendorLicenseModel:
        return new AbstractMap.SimpleEntry<>(new MapVendorLicenseModelEntityToDto(),
            VendorLicenseModelEntityDto.class);
      case LicenseAgreement:
        return new AbstractMap.SimpleEntry<>(
            new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto(),
            LicenseAgreementDescriptorDto.class);
      case FeatureGroup:
        return new AbstractMap.SimpleEntry<>(new MapFeatureGroupEntityToFeatureGroupDescriptorDto(),
            FeatureGroupDescriptorDto.class);
      case LicenseKeyGroup:
        return new AbstractMap.SimpleEntry<>(
            new MapLicenseKeyGroupEntityToLicenseKeyGroupEntityDto(), LicenseKeyGroupEntityDto
            .class);
      case EntitlementPool:
        return new AbstractMap.SimpleEntry<>(
            new MapEntitlementPoolEntityToEntitlementPoolEntityDto(), EntitlementPoolEntityDto
            .class);
      case Limit:
        return new AbstractMap.SimpleEntry<>(new MapLimitEntityToLimitDto(), LimitEntityDto.class);
      case VendorSoftwareProduct:
        return new AbstractMap.SimpleEntry<>(new MapVspDetailsToDto(), VspDetailsDto.class);
      case Network:
        return new AbstractMap.SimpleEntry<>(new MapNetworkEntityToNetworkDto(), NetworkDto.class);
      case Component:
        return new AbstractMap.SimpleEntry<>(new MapComponentEntityToComponentDto(),
            ComponentDto.class);
      case ComponentDependencies:
        return new AbstractMap.SimpleEntry<>(new MapComponentDependencyModelEntityToDto(),
            ComponentDependencyModel.class);
      case Nic:
        return new AbstractMap.SimpleEntry<>(new MapNicEntityToNicDto(), NicDto.class);
      case Process:
        return new AbstractMap.SimpleEntry<>(new MapProcessEntityToProcessEntityDto(),
            ProcessEntityDto.class);
      case DeploymentFlavor:
        return new AbstractMap.SimpleEntry<>(new MapDeploymentFlavorEntityToDeploymentFlavorDto(),
            DeploymentFlavorDto.class);
      case Compute:
        return new AbstractMap.SimpleEntry<>(new MapComputeEntityToVspComputeDto(),
            VspComputeDto.class);
      case Image:
        return new AbstractMap.SimpleEntry<>(new MapImageEntityToImageDto(), ImageDto.class);
      case VSPQuestionnaire:
      case NicQuestionnaire:
      case ComponentQuestionnaire:
      case ImageQuestionnaire:
      case ComputeQuestionnaire:
        return new AbstractMap.SimpleEntry<>(new MapQuestionnaireToQuestionnaireDto(),
            QuestionnaireDto.class);
      case SNMP_POLL:
      case SNMP_TRAP:
      case VES_EVENTS:
      case OrchestrationTemplateCandidate:
        return new AbstractMap.SimpleEntry<>(new MapFilesDataStructureToDto(),
            FileDataStructureDto.class);
      case ServiceModel:
      case NetworkPackage:
        return new AbstractMap.SimpleEntry<>(new EchoMapMapping(), HashMap.class);
      case itemVersion:
        return new AbstractMap.SimpleEntry<>(new EchoMapMapping(), HashMap.class);
    }
    throw new RuntimeException("Get conflict does not support element type: " + type.name());
  }
}
