package org.openecomp.sdc.translator.services.heattotosca;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

public class ConsolidationTypesConnectivity {

    private static Multimap<ConsolidationEntityType, ConsolidationEntityType> entityToEntitiesWithoutRelationship;
    private static ImmutableSet<ConsolidationEntityType> consolidationEntityRelationNodes = ImmutableSet.of(
            ConsolidationEntityType.COMPUTE, ConsolidationEntityType.VOLUME,
            ConsolidationEntityType.PORT, ConsolidationEntityType.NESTED,
            ConsolidationEntityType.VFC_NESTED);

    static {
        entityToEntitiesWithoutRelationship =
                ImmutableSetMultimap.<ConsolidationEntityType, ConsolidationEntityType>builder()
                        .putAll(ConsolidationEntityType.COMPUTE, consolidationEntityRelationNodes)
                        .putAll(ConsolidationEntityType.PORT, consolidationEntityRelationNodes)
                        .putAll(ConsolidationEntityType.VOLUME, consolidationEntityRelationNodes)
                        .putAll(ConsolidationEntityType.VFC_NESTED, consolidationEntityRelationNodes)
                        .putAll(ConsolidationEntityType.NESTED, consolidationEntityRelationNodes)
                        .build();
    }

    private ConsolidationTypesConnectivity() {
    }

    static boolean isDependsOnRelationshipValid(ConsolidationEntityType source, ConsolidationEntityType target) {
        return !eitherSourceOrTargetIsNested(source, target)
                       && (!entityToEntitiesWithoutRelationship.containsKey(source)
                                   || !entityToEntitiesWithoutRelationship.containsEntry(source, target));
    }

    private static boolean eitherSourceOrTargetIsNested(ConsolidationEntityType source,
                                                               ConsolidationEntityType target) {
        return ConsolidationEntityType.isEntityTypeNested(source) || ConsolidationEntityType.isEntityTypeNested(target);
    }
}
