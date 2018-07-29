/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.translator.services.heattotosca;

import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil.*;

/**
 * The enum Entity type.
 */
public enum ConsolidationEntityType {
    COMPUTE, PORT, VOLUME, NESTED,
    //Simple nested VFC (nested file with one compute) or a complex VFC (nested ST with more than
    //one compute)
    VFC_NESTED, SUB_INTERFACE, OTHER;

    private ConsolidationEntityType sourceEntityType;
    private ConsolidationEntityType targetEntityType;

    public ConsolidationEntityType getSourceEntityType() {
        return sourceEntityType;
    }

    public ConsolidationEntityType getTargetEntityType() {
        return targetEntityType;
    }


    /**
     * Sets entity type.
     *
     * @param sourceResource the source resource
     * @param targetResource the target resource
     */
    public void setEntityType(Resource sourceResource, Resource targetResource, TranslationContext context) {
        targetEntityType = getEntityType(targetResource, context);
        sourceEntityType = getEntityType(sourceResource, context);
    }

    private static final Set<ConsolidationEntityType> consolidationEntityTypes = initConsolidationEntities();

    private static Set<ConsolidationEntityType> initConsolidationEntities() {
        return Collections.unmodifiableSet(EnumSet.allOf(ConsolidationEntityType.class).stream().filter(
                consolidationEntityType -> consolidationEntityType != ConsolidationEntityType.OTHER
                && consolidationEntityType != ConsolidationEntityType.VOLUME).collect(Collectors.toSet()));
    }

    public static Set<ConsolidationEntityType> getSupportedConsolidationEntities() {
        return consolidationEntityTypes;
    }

    private ConsolidationEntityType getEntityType(Resource resource, TranslationContext context) {
        ConsolidationEntityType consolidationEntityType = ConsolidationEntityType.OTHER;
        if (isComputeResource(resource)) {
            consolidationEntityType = ConsolidationEntityType.COMPUTE;
        } else if (isPortResource(resource)) {
            consolidationEntityType = ConsolidationEntityType.PORT;
        } else if (HeatToToscaUtil.isSubInterfaceResource(resource, context)) {
            consolidationEntityType = ConsolidationEntityType.SUB_INTERFACE;
        } else if (isVolumeResource(resource)) {
            consolidationEntityType = ConsolidationEntityType.VOLUME;
        } else if (HeatToToscaUtil.isNestedResource(resource)) {
            if (HeatToToscaUtil.isNestedVfcResource(resource, context)) {
                consolidationEntityType = ConsolidationEntityType.VFC_NESTED;
            } else {
                consolidationEntityType = ConsolidationEntityType.NESTED;
            }
        }
        return consolidationEntityType;
    }
}