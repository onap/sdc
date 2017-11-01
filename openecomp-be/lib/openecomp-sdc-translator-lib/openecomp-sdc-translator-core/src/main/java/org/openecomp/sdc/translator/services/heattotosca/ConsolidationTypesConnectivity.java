package org.openecomp.sdc.translator.services.heattotosca;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsolidationTypesConnectivity {

  private static Map<ConsolidationEntityType, Set<ConsolidationEntityType>>
      entityToEntitiesWithoutRelationship;

  static {
    entityToEntitiesWithoutRelationship = new HashMap<>();
    updateIgnoredComputeRelationships();
    updateIgnoredPortRelationships();
    updateIgnoredVolumeRelationships();
    updateIgnoredVfcNestedRelationships();
  }

  private static void updateIgnoredComputeRelationships(){
    entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.COMPUTE,
        Stream.of(ConsolidationEntityType.COMPUTE,
            ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT,
            ConsolidationEntityType.VFC_NESTED).collect(Collectors.toSet()));
  }

  private static void updateIgnoredPortRelationships(){
    entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.PORT,
        Stream.of(ConsolidationEntityType.COMPUTE,
            ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT,
            ConsolidationEntityType.VFC_NESTED).collect(Collectors.toSet()));
  }

  private static void updateIgnoredVolumeRelationships(){
    entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VOLUME,
        Stream.of(ConsolidationEntityType.COMPUTE,
            ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT,
            ConsolidationEntityType.VFC_NESTED).collect(Collectors.toSet()));
  }

  private static void updateIgnoredVfcNestedRelationships(){
    entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VFC_NESTED,
        Stream.of(ConsolidationEntityType.COMPUTE,
            ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT,
            ConsolidationEntityType.VFC_NESTED).collect(Collectors.toSet()));
  }

  public static boolean isDependsOnRelationshipValid(ConsolidationEntityType source,
                                                     ConsolidationEntityType target) {
    Set<ConsolidationEntityType> consolidationEntityTypes =
        entityToEntitiesWithoutRelationship.get(source);
    return CollectionUtils.isEmpty(consolidationEntityTypes) ||
        !consolidationEntityTypes.contains(target);

  }
}
