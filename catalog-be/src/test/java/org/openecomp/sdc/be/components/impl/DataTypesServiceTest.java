package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;


import java.util.HashMap;
import java.util.Map;

public class DataTypesServiceTest {
    ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
    ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);

    DataTypesService dataTypesService = new DataTypesService(componentsUtils);
    Map<String, DataTypeDefinition> mapreturn = new HashMap<>();
    JanusGraphOperationStatus janusGraphOperationStatus = JanusGraphOperationStatus.NOT_FOUND;
    Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes;

    @Before
    public void setup() {
        mapreturn.put("Demo",new DataTypeDefinition());
        allDataTypes = Either.left(mapreturn);
        when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

    }

    @Test
    public void getAllDataTypes_success() {
        Assert.assertEquals(true,dataTypesService.getAllDataTypes(applicationDataTypeCache).isLeft());
    }

    @Test
    public void getAllDataTypes_failure() {
        allDataTypes = Either.right(janusGraphOperationStatus);
        when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);
        Assert.assertEquals(true,dataTypesService.getAllDataTypes(applicationDataTypeCache).isRight());
    }

}