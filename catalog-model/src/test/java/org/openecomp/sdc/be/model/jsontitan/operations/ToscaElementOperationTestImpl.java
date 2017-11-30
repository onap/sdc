package org.openecomp.sdc.be.model.jsontitan.operations;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsontitan.datamodel.ToscaElement;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

import fj.data.Either;

/**
 * Created by cb478c on 6/13/2017.
 */
@org.springframework.stereotype.Component("test-tosca-element-operation")
public class ToscaElementOperationTestImpl extends ToscaElementOperation {

    @Override
    protected <T extends ToscaElement> Either<T, StorageOperationStatus> getLightComponent(GraphVertex vertexComponent, ComponentTypeEnum nodeType, ComponentParametersView parametersFilter) {
        titanDao.parseVertexProperties(vertexComponent, JsonParseFlagEnum.ParseMetadata);
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
    protected <T extends ToscaElement> TitanOperationStatus setCategoriesFromGraph(GraphVertex vertexComponent, T toscaElement) {
        return null;
    }

    @Override
    protected <T extends ToscaElement> TitanOperationStatus setCapabilitiesFromGraph(GraphVertex componentV, T toscaElement) {
        return null;
    }

    @Override
    protected <T extends ToscaElement> TitanOperationStatus setRequirementsFromGraph(GraphVertex componentV, T toscaElement) {
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
