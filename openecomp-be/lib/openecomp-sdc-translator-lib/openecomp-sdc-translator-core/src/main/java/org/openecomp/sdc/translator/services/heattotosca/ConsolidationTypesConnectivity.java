/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.translator.services.heattotosca;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

public class ConsolidationTypesConnectivity {

    private static Multimap<ConsolidationEntityType, ConsolidationEntityType> entityToEntitiesWithoutRelationship;
    private static ImmutableSet<ConsolidationEntityType> consolidationEntityRelationNodes = ImmutableSet
        .of(ConsolidationEntityType.COMPUTE, ConsolidationEntityType.VOLUME, ConsolidationEntityType.PORT, ConsolidationEntityType.NESTED,
            ConsolidationEntityType.VFC_NESTED);

    static {
        entityToEntitiesWithoutRelationship = ImmutableSetMultimap.<ConsolidationEntityType, ConsolidationEntityType>builder()
            .putAll(ConsolidationEntityType.COMPUTE, consolidationEntityRelationNodes)
            .putAll(ConsolidationEntityType.PORT, consolidationEntityRelationNodes)
            .putAll(ConsolidationEntityType.VOLUME, consolidationEntityRelationNodes)
            .putAll(ConsolidationEntityType.VFC_NESTED, consolidationEntityRelationNodes)
            .putAll(ConsolidationEntityType.NESTED, consolidationEntityRelationNodes).build();
    }

    private ConsolidationTypesConnectivity() {
    }

    static boolean isDependsOnRelationshipValid(ConsolidationEntityType source, ConsolidationEntityType target) {
        return !entityToEntitiesWithoutRelationship.containsKey(source) || !entityToEntitiesWithoutRelationship.containsEntry(source, target);
    }
}
