package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.RelationType;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.StructureElement;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.VlmZusammenUtil;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ayalaben on 4/24/2017
 */
public class EntitlementPoolConvertor {

  private static Set<String> EntitlementPoolsLoaded = new HashSet<>();

  public static ElementEntityContext convertEntitlementPoolToElementContext(
      EntitlementPoolEntity entitlementPoolEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(entitlementPoolEntity.getVendorLicenseModelId(),
        entitlementPoolEntity.getVersion().toString()));
  }

  public static CollaborationElement[] convertEntitlementPoolToElement(
      EntitlementPoolEntity entitlementPoolEntity) {
//    printMessage(logger, "source EntitlementPoolEntity -> " + entitlementPoolEntity.toString());
    CollaborationElement[] elements;
    List<String> entitlementPoolNamespace = getEntitlementPoolNamespace(entitlementPoolEntity);

    int index = 0;
    String entitlememtPoolsEntityId = StructureElement.EntitlementPools.name();
    String uniqueId = entitlementPoolEntity.getVendorLicenseModelId() + "_" +
        entitlementPoolEntity.getVersion().toString();

    if (EntitlementPoolsLoaded.contains(uniqueId)) {
      elements = new CollaborationElement[1];
    } else {
      EntitlementPoolsLoaded.add(uniqueId);
      elements = new CollaborationElement[2];
      elements[index] = ElementHandler.getElementEntity(
          entitlementPoolEntity.getVendorLicenseModelId(),
          entitlementPoolEntity.getVersion().toString(),
          entitlememtPoolsEntityId, entitlementPoolNamespace,
              ElementHandler.getStructuralElementInfo(StructureElement.EntitlementPools.name()),
          null, null, null);
      index++;
    }

    entitlementPoolNamespace.add(entitlememtPoolsEntityId);

    elements[index] = ElementHandler.getElementEntity(
        entitlementPoolEntity.getVendorLicenseModelId(),
        entitlementPoolEntity.getVersion().toString(),
        entitlementPoolEntity.getId(), entitlementPoolNamespace,
        getEntitelementPoolInfo(entitlementPoolEntity),
        entitlementPoolEntity.getReferencingFeatureGroups().stream().map(rel ->
            VlmZusammenUtil
                .createRelation( RelationType.EntitlmentPoolToReferencingFeatureGroup, rel))
            .collect(Collectors.toList()), null, null);

    return elements;
  }

  private static Info getEntitelementPoolInfo(EntitlementPoolEntity entitlementPool) {

    Info info = new Info();
    info.setName(entitlementPool.getName());
    info.setDescription(entitlementPool.getDescription());
    info.addProperty("thresholdValue", entitlementPool.getThresholdValue());
    info.addProperty("threshold_unit", entitlementPool.getThresholdUnit());
    info.addProperty("entitlement_metric", entitlementPool.getEntitlementMetric());
    info.addProperty("increments", entitlementPool.getIncrements());
    info.addProperty("aggregation_func", entitlementPool.getAggregationFunction());
    info.addProperty("operational_scope", entitlementPool.getOperationalScope());
    info.addProperty("EntitlementTime", entitlementPool.getTime());
    info.addProperty("manufacturerReferenceNumber",
        entitlementPool.getManufacturerReferenceNumber());

    return info;
  }

  private static List<String> getEntitlementPoolNamespace(
      EntitlementPoolEntity entitlementPoolEntity) {
    return ElementHandler.getElementPath("");
  }

}
