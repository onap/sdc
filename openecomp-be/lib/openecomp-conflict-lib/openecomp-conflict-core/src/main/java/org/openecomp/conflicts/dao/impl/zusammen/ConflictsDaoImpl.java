package org.openecomp.conflicts.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.Resolution;
import org.openecomp.conflicts.dao.ConflictsDao;
import org.openecomp.conflicts.types.Conflict;
import org.openecomp.conflicts.types.ConflictResolution;
import org.openecomp.conflicts.types.ItemVersionConflict;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToEntitlementPoolConvertor;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToFeatureGroupConvertor;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToLicenseAgreementConvertor;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToLicenseKeyGroupConvertor;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToLimitConvertor;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToVLMGeneralConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToComponentConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToComponentDependencyModelConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToCompositionEntityConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToComputeConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToImageConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToMonitoringUploadMapConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToNetworkConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToNicConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToOrchestrationTemplateCandidateMapConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToServiceModelMapConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToVSPGeneralConvertor;
import org.openecomp.sdc.versioning.dao.types.Version;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class ConflictsDaoImpl implements ConflictsDao {
  private final ZusammenAdaptor zusammenAdaptor;

  public ConflictsDaoImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public boolean isConflicted(String itemId, Version version) {
    com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict itemVersionConflict =
        zusammenAdaptor
            .getVersionConflict(createSessionContext(), new Id(itemId), new Id(version.getId()));
    return !(itemVersionConflict == null
        || (itemVersionConflict.getVersionDataConflict() == null
        && itemVersionConflict.getElementConflictInfos().isEmpty()));
  }

  @Override
  public ItemVersionConflict getConflict(String itemId, Version version) {
    return new ItemVersionConflictConvertorFromZusammen().convert(itemId, version,
        zusammenAdaptor
            .getVersionConflict(createSessionContext(), new Id(itemId), new Id(version.getId())));
  }

  @Override
  public Conflict getConflict(String itemId, Version version, String conflictId) {
    return zusammenAdaptor.getElementConflict(createSessionContext(),
        new ElementContext(new Id(itemId), new Id(version.getId())), new Id(conflictId))
        .map(elementConflict -> convertElementConflict(conflictId, elementConflict))
        .orElse(null);
  }

  @Override
  public void resolveConflict(String itemId, Version version, String conflictId,
                              ConflictResolution conflictResolution) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(new Id(itemId), new Id(version.getId()));

    // TODO: 7/31/2017 when 'OTHER' resolution will be supported - populate zusammen element with it
    zusammenAdaptor.resolveElementConflict(context, elementContext,
        buildElement(new Id(conflictId), null),
        Resolution.valueOf(conflictResolution.getResolution().name()));
  }

  private Conflict convertElementConflict(String conflictId, ElementConflict elementConflict) {
    Element element = elementConflict.getLocalElement() == null
        ? elementConflict.getRemoteElement()
        : elementConflict.getLocalElement();
    ElementType elementType = ElementConvertor.getElementType(element);

    Conflict conflict =
        new Conflict(conflictId, elementType, ElementConvertor.getElementName(element));
    ElementConvertor convertor = getConvertor(elementType);
    if (elementConflict.getLocalElement() != null) {
      conflict.setYours(convertor.convert(elementConflict.getLocalElement()));
    }
    if (elementConflict.getRemoteElement() != null) {
      conflict.setTheirs(convertor.convert(elementConflict.getRemoteElement()));
    }
    return conflict;
  }

  private ElementConvertor getConvertor(ElementType type) {
    switch (type) {
      case VendorSoftwareProduct:
        return new ElementToVSPGeneralConvertor();
      case Process:
        return new ElementToProcessConvertor();
      case Nic:
        return new ElementToNicConvertor();
      case Network:
        return new ElementToNetworkConvertor();
      case SNMP_POLL:
      case SNMP_TRAP:
      case VES_EVENTS:
        return new ElementToMonitoringUploadMapConvertor();
      case Image:
        return new ElementToImageConvertor();
      case Compute:
        return new ElementToComputeConvertor();
      case Component:
        return new ElementToComponentConvertor();
      case ComponentDependencies:
        return new ElementToComponentDependencyModelConvertor();
      case VendorLicenseModel:
        return new ElementToVLMGeneralConvertor();
      case LicenseAgreement:
        return new ElementToLicenseAgreementConvertor();
      case FeatureGroup:
        return new ElementToFeatureGroupConvertor();
      case LicenseKeyGroup:
        return new ElementToLicenseKeyGroupConvertor();
      case EntitlementPool:
        return new ElementToEntitlementPoolConvertor();
      case Limit:
        return new ElementToLimitConvertor();
      case OrchestrationTemplateCandidate:
        return new ElementToOrchestrationTemplateCandidateMapConvertor();
      case ServiceModel:
        return new ElementToServiceModelMapConvertor();
      case VSPQuestionnaire:
      case ImageQuestionnaire:
      case ComponentQuestionnaire:
      case ComputeQuestionnaire:
      case NicQuestionnaire:
        return new ElementToCompositionEntityConvertor();
      default:
        return new EchoConvertor();
    }
  }

  public static class EchoConvertor extends ElementConvertor {

    @Override
    public Object convert(Element element) {
      return element;
    }

    @Override
    public Object convert(Item item) {
      return item;
    }

    @Override
    public Object convert(ElementInfo elementInfo) {
      return elementInfo;
    }

    @Override
    public Object convert(ItemVersion itemVersion) {
      return null;
    }
  }
}
