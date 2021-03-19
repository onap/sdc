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
package org.openecomp.sdc.be.components.merge;

import fj.data.Either;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.components.impl.generic.GenericTypeBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

@org.springframework.stereotype.Component
public class GlobalInputsFilteringBusinessLogic {

    private GenericTypeBusinessLogic genericTypeBusinessLogic;
    private ToscaOperationFacade toscaOperationFacade;
    private ComponentsUtils componentsUtils;

    public GlobalInputsFilteringBusinessLogic(GenericTypeBusinessLogic genericTypeBusinessLogic, ToscaOperationFacade toscaOperationFacade,
                                              ComponentsUtils componentsUtils) {
        this.genericTypeBusinessLogic = genericTypeBusinessLogic;
        this.toscaOperationFacade = toscaOperationFacade;
        this.componentsUtils = componentsUtils;
    }

    public Either<List<InputDefinition>, ActionStatus> filterGlobalInputs(Component newResource) {
        Either<Resource, StorageOperationStatus> genericComp = toscaOperationFacade
            .getLatestCertifiedNodeTypeByToscaResourceName(newResource.fetchGenericTypeToscaNameFromConfig());
        return genericComp.bimap(genericResource -> findCommonInputs(genericResource, newResource),
            storageOperationStatus -> componentsUtils.convertFromStorageResponse(storageOperationStatus));
    }

    private List<InputDefinition> findCommonInputs(Resource genericResource, Component resource) {
        List<InputDefinition> resourceInputs = resource.getInputs();
        List<InputDefinition> genericInputs = genericTypeBusinessLogic.generateInputsFromGenericTypeProperties(genericResource);
        Set<String> genericInputsNames = genericInputs.stream().map(InputDefinition::getName).collect(Collectors.toSet());
        return resourceInputs.stream().filter(input -> genericInputsNames.contains(input.getName())).collect(Collectors.toList());
    }
}
