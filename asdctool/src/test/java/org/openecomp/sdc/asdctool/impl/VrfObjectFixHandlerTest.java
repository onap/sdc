package org.openecomp.sdc.asdctool.impl;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class VrfObjectFixHandlerTest {

    private TitanDao titanDao;

    private VrfObjectFixHandler vrfObjectFixHandler;

    @Before
    public void init(){
        titanDao = Mockito.mock(TitanDao.class);
        vrfObjectFixHandler = new VrfObjectFixHandler(titanDao);
    }

    @Test
    public void handleInvalidModeTest(){
        assertThat(vrfObjectFixHandler.handle("invalid mode", null)).isFalse();
    }

    @Test
    public void handleDetectNotFoundTest(){
        when(titanDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), anyMap())).thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        assertThat(vrfObjectFixHandler.handle("detect", null)).isTrue();
    }

    @Test
    public void handleDetectTitanNotConnectedTest(){
        when(titanDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), anyMap())).thenReturn(Either.right(TitanOperationStatus.NOT_CONNECTED));
        assertThat(vrfObjectFixHandler.handle("detect", null)).isFalse();
    }

    @Test
    public void handleFixNotFoundTest(){
        when(titanDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), anyMap())).thenReturn(Either.right(TitanOperationStatus.NOT_FOUND));
        assertThat(vrfObjectFixHandler.handle("fix", null)).isTrue();
    }

    @Test
    public void handleFixNotCreatedTest(){
        when(titanDao.getByCriteria(eq(VertexTypeEnum.NODE_TYPE), anyMap())).thenReturn(Either.right(TitanOperationStatus.NOT_CREATED));
        assertThat(vrfObjectFixHandler.handle("fix", null)).isFalse();
    }

}
