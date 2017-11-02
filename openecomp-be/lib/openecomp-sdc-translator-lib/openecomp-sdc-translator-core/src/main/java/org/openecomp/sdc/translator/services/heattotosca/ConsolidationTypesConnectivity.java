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
    entityToEntitiesWithoutRelationship
        .put(ConsolidationEntityType.COMPUTE, getIgnoredComputeRelationships());
    entityToEntitiesWithoutRelationship
        .put(ConsolidationEntityType.PORT,getIgnoredPortRelationships());
    entityToEntitiesWithoutRelationship
        .put(ConsolidationEntityType.VOLUME, getIgnoredVolumeRelationships());
    entityToEntitiesWithoutRelationship
        .put(ConsolidationEntityType.VFC_NESTED, getIgnoredVfcNestedRelationships());
  }

  private static Set<ConsolidationEntityType> getIgnoredComputeRelationships(){
        return Stream.of(ConsolidationEntityType.COMPUTE,
            ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT,
            ConsolidationEntityType.VFC_NESTED).collect(Collectors.toSet());
  }

  private static Set<ConsolidationEntityType> getIgnoredPortRelationships(){
        return Stream.of(ConsolidationEntityType.COMPUTE,
            ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT,
            ConsolidationEntityType.VFC_NESTED).collect(Collectors.toSet());
  }

  private static Set<ConsolidationEntityType> getIgnoredVolumeRelationships(){
        return Stream.of(ConsolidationEntityType.COMPUTE,
            ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT,
            ConsolidationEntityType.VFC_NESTED).collect(Collectors.toSet());
  }

  private static Set<ConsolidationEntityType> getIgnoredVfcNestedRelationships(){
        return Stream.of(ConsolidationEntityType.COMPUTE,
            ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT,
            ConsolidationEntityType.VFC_NESTED).collect(Collectors.toSet());
  }

  public static boolean isDependsOnRelationshipValid(ConsolidationEntityType source,
                                                     ConsolidationEntityType target) {
    Set<ConsolidationEntityType> consolidationEntityTypes =
        entityToEntitiesWithoutRelationship.get(source);
    return CollectionUtils.isEmpty(consolidationEntityTypes) ||
        !consolidationEntityTypes.contains(target);

  }
}
