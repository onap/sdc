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

package org.openecomp.sdc.be.model.jsonjanusgraph.operations;

import fj.data.Either;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

/**
 * Created by cb478c on 6/13/2017.
 */
@org.springframework.stereotype.Component("test-tosca-element-operation")
public class ToscaElementOperationTestImpl extends ToscaElementOperation {

    @Override
    protected <T extends ToscaElement> Either<T, StorageOperationStatus> getLightComponent(GraphVertex vertexComponent, ComponentTypeEnum nodeType, ComponentParametersView parametersFilter) {
        janusGraphDao.parseVertexProperties(vertexComponent, JsonParseFlagEnum.ParseMetadata);
        T toscaElement = convertToComponent(vertexComponent);
        return Either.left(toscaElement);
    }

    @Override
    public <T extends ToscaElement> Either<T, StorageOperationStatus> getToscaElement(String uniqueId, ComponentParametersView componentParametersView) {
        return null;
    }

    @Override
    public <T extends ToscaElement> Either<T, StorageOperationStatus> getToscaElement(GraphVertex toscaElementVertex, ComponentParametersView componentParametersView) {
        return null;
    }

    @Override
    public <T extends ToscaElement> Either<T, StorageOperationStatus> deleteToscaElement(GraphVertex toscaElementVertex) {
        return null;
    }

    @Override
    public <T extends ToscaElement> Either<T, StorageOperationStatus> createToscaElement(ToscaElement toscaElement) {
        return null;
    }

    @Override
    protected <T extends ToscaElement> JanusGraphOperationStatus setCategoriesFromGraph(GraphVertex vertexComponent, T toscaElement) {
        return null;
    }

    @Override
    protected <T extends ToscaElement> JanusGraphOperationStatus setCapabilitiesFromGraph(GraphVertex componentV, T toscaElement) {
        return null;
    }

    @Override
    protected <T extends ToscaElement> JanusGraphOperationStatus setRequirementsFromGraph(GraphVertex componentV, T toscaElement) {
        return null;
    }

    @Override
    protected <T extends ToscaElement> StorageOperationStatus validateCategories(T toscaElementToUpdate, GraphVertex elementV) {
        return null;
    }

    @Override
    protected <T extends ToscaElement> StorageOperationStatus updateDerived(T toscaElementToUpdate, GraphVertex updateElementV) {
        return null;
    }

    @Override
    public <T extends ToscaElement> void fillToscaElementVertexData(GraphVertex elementV, T toscaElementToUpdate, JsonParseFlagEnum flag) {

    }
}
