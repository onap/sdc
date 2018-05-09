package org.openecomp.sdc.translator.services.heattotosca;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

public class ConsolidationTypesConnectivity {

    private static SetMultimap<ConsolidationEntityType, ConsolidationEntityType> entityToEntitiesWithoutRelationship =
            Multimaps.synchronizedSetMultimap(HashMultimap.create());


    static {
        getIgnoredComputeRelationships();
        getIgnoredPortRelationships();
        getIgnoredVolumeRelationships();
        getIgnoredVfcNestedRelationships();
        getIgnoredNestedRelationships();
    }

    private ConsolidationTypesConnectivity() {
    }

    private static void getIgnoredComputeRelationships() {
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.COMPUTE, ConsolidationEntityType.COMPUTE);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.COMPUTE, ConsolidationEntityType.VOLUME);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.COMPUTE, ConsolidationEntityType.PORT);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.COMPUTE, ConsolidationEntityType.NESTED);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.COMPUTE, ConsolidationEntityType.VFC_NESTED);
    }

    private static void getIgnoredPortRelationships() {
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.PORT, ConsolidationEntityType.COMPUTE);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.PORT, ConsolidationEntityType.VOLUME);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.PORT, ConsolidationEntityType.PORT);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.PORT, ConsolidationEntityType.NESTED);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.PORT, ConsolidationEntityType.VFC_NESTED);
    }

    private static void getIgnoredVolumeRelationships() {
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VOLUME, ConsolidationEntityType.COMPUTE);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VOLUME, ConsolidationEntityType.VOLUME);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VOLUME, ConsolidationEntityType.PORT);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VOLUME, ConsolidationEntityType.NESTED);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VOLUME, ConsolidationEntityType.VFC_NESTED);
    }

    private static void getIgnoredVfcNestedRelationships() {
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VFC_NESTED, ConsolidationEntityType.COMPUTE);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VFC_NESTED, ConsolidationEntityType.VOLUME);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VFC_NESTED, ConsolidationEntityType.PORT);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VFC_NESTED, ConsolidationEntityType.NESTED);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.VFC_NESTED, ConsolidationEntityType.VFC_NESTED);
    }

    private static void getIgnoredNestedRelationships() {
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.NESTED, ConsolidationEntityType.COMPUTE);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.NESTED, ConsolidationEntityType.VOLUME);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.NESTED, ConsolidationEntityType.PORT);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.NESTED, ConsolidationEntityType.NESTED);
        entityToEntitiesWithoutRelationship.put(ConsolidationEntityType.NESTED, ConsolidationEntityType.VFC_NESTED);
    }

    public static boolean isDependsOnRelationshipValid(ConsolidationEntityType source, ConsolidationEntityType target) {
        if (eitherSourceOrTargetIsNested(source, target)) {
            return false;
        }

        Set<ConsolidationEntityType> consolidationEntityTypes = entityToEntitiesWithoutRelationship.get(source);
        return CollectionUtils.isEmpty(consolidationEntityTypes) || !consolidationEntityTypes.contains(target);

    }

    private static boolean eitherSourceOrTargetIsNested(ConsolidationEntityType source,
                                                               ConsolidationEntityType target) {
        return ConsolidationEntityType.isEntityTypeNested(source) || ConsolidationEntityType.isEntityTypeNested(target);
    }
}
