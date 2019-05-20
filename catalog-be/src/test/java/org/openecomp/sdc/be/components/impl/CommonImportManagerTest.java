package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.CommonImportManager.ElementTypeEnum;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.exception.ServiceException;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CommonImportManagerTest {
    private CommonImportManager commonImportManager;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    PropertyOperation propertyOperation;
    @Mock
    private JanusGraphGenericDao janusGraphGenericDao;
    
    @Mock
    private Function<Object, Either<ActionStatus, ResponseFormat>> validator;
    @Mock
    private Function<Object, ImmutablePair<ElementTypeEnum, String>> elementInfoGetter;
    @Mock
    private Function<String, Either<Object, StorageOperationStatus>> elementFetcher;
    @Mock
    private Function<Object, Either<Object, StorageOperationStatus>> elementAdder;
    @Mock
    private BiFunction<Object, Object, Either<Object, StorageOperationStatus>> elementUpgrader;
    
    @Before
    public void startUp() {
        commonImportManager = new CommonImportManager(componentsUtils, propertyOperation);
        
        when(propertyOperation.getJanusGraphGenericDao()).thenReturn(janusGraphGenericDao);
    }
    
    @Test
    public void testCreateElementTypesByDao_validationFailed() {
        Object type1 = new Object();
        List<Object> elementTypesToCreate = Arrays.asList(type1);
        
        ImmutablePair<ElementTypeEnum, String> elementInfo = new ImmutablePair<>(ElementTypeEnum.CAPABILITY_TYPE, "TestCapability"); 
        when(elementInfoGetter.apply(type1)).thenReturn(elementInfo);
        
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setServiceException(new ServiceException());
        when(validator.apply(type1)).thenReturn(Either.right(responseFormat));
        
        
        commonImportManager.createElementTypesByDao(elementTypesToCreate , validator , elementInfoGetter, elementFetcher, elementAdder, elementUpgrader);
        
        verify(elementAdder, never()).apply(Mockito.any());
        verify(elementUpgrader, never()).apply(Mockito.any(), Mockito.any());
        verify(janusGraphGenericDao).rollback();
    }
    
    @Test
    public void testCreateElementTypesByDao_RuntTimeExceptionInValidation() {
        Object type1 = new Object();
        List<Object> elementTypesToCreate = Arrays.asList(type1);
        
        ImmutablePair<ElementTypeEnum, String> elementInfo = new ImmutablePair<>(ElementTypeEnum.CAPABILITY_TYPE, "TestCapability"); 
        when(elementInfoGetter.apply(type1)).thenReturn(elementInfo);
        when(validator.apply(type1)).thenThrow(new RuntimeException("Test Exception"));
        
        try {
            commonImportManager.createElementTypesByDao(elementTypesToCreate , validator , elementInfoGetter, elementFetcher, elementAdder, elementUpgrader);
        }
        catch(Exception skip) {
        }
        
        verify(elementAdder, never()).apply(Mockito.any());
        verify(elementUpgrader, never()).apply(Mockito.any(), Mockito.any());
        verify(janusGraphGenericDao).rollback();
    }
    
    @Test
    public void testCreateElementTypesByDao_capabilityTypeFetcherFailed() {
        CapabilityTypeDefinition type1 = new CapabilityTypeDefinition();
        List<Object> elementTypesToCreate = Arrays.asList(type1);
        
        ImmutablePair<ElementTypeEnum, String> elementInfo = new ImmutablePair<>(ElementTypeEnum.CAPABILITY_TYPE, "TestCapability"); 
        when(elementInfoGetter.apply(type1)).thenReturn(elementInfo);
        
        when(validator.apply(type1)).thenReturn(Either.left(ActionStatus.OK));
        when(elementFetcher.apply("TestCapability")).thenReturn(Either.right(StorageOperationStatus.BAD_REQUEST));
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setServiceException(new ServiceException());
        when(componentsUtils.convertFromStorageResponseForCapabilityType(Mockito.any())).thenCallRealMethod();
        when(componentsUtils.getResponseFormatByCapabilityType(ActionStatus.INVALID_CONTENT, type1)).thenReturn(responseFormat);
        
        
        commonImportManager.createElementTypesByDao(elementTypesToCreate , validator , elementInfoGetter, elementFetcher, elementAdder, elementUpgrader);
        
        verify(elementAdder, never()).apply(Mockito.any());
        verify(elementUpgrader, never()).apply(Mockito.any(), Mockito.any());
        verify(janusGraphGenericDao).rollback();
    }
    
    @Test
    public void testCreateElementTypesByDao_capabilityTypeNotFound_AddFailed() {
        CapabilityTypeDefinition type1 = new CapabilityTypeDefinition();
        List<Object> elementTypesToCreate = Arrays.asList(type1);
        
        ImmutablePair<ElementTypeEnum, String> elementInfo = new ImmutablePair<>(ElementTypeEnum.CAPABILITY_TYPE, "TestCapability"); 
        when(elementInfoGetter.apply(type1)).thenReturn(elementInfo);
        
        when(validator.apply(type1)).thenReturn(Either.left(ActionStatus.OK));
        when(elementFetcher.apply("TestCapability")).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(elementAdder.apply(type1)).thenReturn(Either.right(StorageOperationStatus.SCHEMA_VIOLATION));
        
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setServiceException(new ServiceException());
        when(componentsUtils.convertFromStorageResponseForCapabilityType(Mockito.any())).thenCallRealMethod();
        when(componentsUtils.getResponseFormatByCapabilityType(ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST, type1)).thenReturn(responseFormat);

        
        commonImportManager.createElementTypesByDao(elementTypesToCreate , validator , elementInfoGetter, elementFetcher, elementAdder, elementUpgrader);

        verify(elementAdder).apply(type1);
        verify(elementUpgrader, never()).apply(Mockito.any(), Mockito.any());
        verify(janusGraphGenericDao).rollback();
    }

    
    @Test
    public void testCreateElementTypesByDao_capabilityTypeNotFound_AddSucceeded() {
        CapabilityTypeDefinition type1 = new CapabilityTypeDefinition();
        List<Object> elementTypesToCreate = Arrays.asList(type1);
        
        ImmutablePair<ElementTypeEnum, String> elementInfo = new ImmutablePair<>(ElementTypeEnum.CAPABILITY_TYPE, "TestCapability"); 
        when(elementInfoGetter.apply(type1)).thenReturn(elementInfo);
        
        when(validator.apply(type1)).thenReturn(Either.left(ActionStatus.OK));
        when(elementFetcher.apply("TestCapability")).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(elementAdder.apply(type1)).thenReturn(Either.left(type1));
        
        
        Either<List<ImmutablePair<Object, Boolean>>, ResponseFormat> result = commonImportManager.createElementTypesByDao(elementTypesToCreate , validator , elementInfoGetter, elementFetcher, elementAdder, elementUpgrader);

        verify(elementAdder).apply(type1);
        verify(elementUpgrader, never()).apply(Mockito.any(), Mockito.any());
        verify(janusGraphGenericDao).commit();
        
        assertEquals(type1, result.left().value().get(0).getLeft());
        assertEquals(true, result.left().value().get(0).getRight());
    }
    
    @Test
    public void testCreateElementTypesByDao_capabilityTypeFound_UpgradeFailed() {
        CapabilityTypeDefinition type1 = new CapabilityTypeDefinition();
        CapabilityTypeDefinition type1_1 = new CapabilityTypeDefinition();
        List<Object> elementTypesToCreate = Arrays.asList(type1_1);
        
        ImmutablePair<ElementTypeEnum, String> elementInfo = new ImmutablePair<>(ElementTypeEnum.CAPABILITY_TYPE, "TestCapability"); 
        when(elementInfoGetter.apply(type1_1)).thenReturn(elementInfo);
        
        when(validator.apply(type1_1)).thenReturn(Either.left(ActionStatus.OK));
        when(elementFetcher.apply("TestCapability")).thenReturn(Either.left(type1));
        when(elementUpgrader.apply(type1_1, type1)).thenReturn(Either.right(StorageOperationStatus.SCHEMA_VIOLATION));
        
        ResponseFormat responseFormat = new ResponseFormat();
        responseFormat.setServiceException(new ServiceException());
        when(componentsUtils.convertFromStorageResponseForCapabilityType(Mockito.any())).thenCallRealMethod();
        when(componentsUtils.getResponseFormatByCapabilityType(ActionStatus.CAPABILITY_TYPE_ALREADY_EXIST, type1_1)).thenReturn(responseFormat);

        
        commonImportManager.createElementTypesByDao(elementTypesToCreate , validator , elementInfoGetter, elementFetcher, elementAdder, elementUpgrader);

        verify(elementAdder, never()).apply(Mockito.any());
        verify(elementUpgrader).apply(type1_1, type1);
        verify(janusGraphGenericDao).rollback();
    }
    
    @Test
    public void testCreateElementTypesByDao_capabilityTypeFound_UpgradeSucceeded() {
        CapabilityTypeDefinition type1 = new CapabilityTypeDefinition();
        CapabilityTypeDefinition type1_1 = new CapabilityTypeDefinition();
        List<Object> elementTypesToCreate = Arrays.asList(type1_1);
        
        ImmutablePair<ElementTypeEnum, String> elementInfo = new ImmutablePair<>(ElementTypeEnum.CAPABILITY_TYPE, "TestCapability"); 
        when(elementInfoGetter.apply(type1_1)).thenReturn(elementInfo);
        
        when(validator.apply(type1_1)).thenReturn(Either.left(ActionStatus.OK));
        when(elementFetcher.apply("TestCapability")).thenReturn(Either.left(type1));
        when(elementUpgrader.apply(type1_1, type1)).thenReturn(Either.left(type1_1));
        
        Either<List<ImmutablePair<Object, Boolean>>, ResponseFormat> result = commonImportManager.createElementTypesByDao(elementTypesToCreate , validator , elementInfoGetter, elementFetcher, elementAdder, elementUpgrader);

        verify(elementAdder, never()).apply(Mockito.any());
        verify(elementUpgrader).apply(type1_1, type1);
        verify(janusGraphGenericDao).commit();
        
        assertEquals(type1_1, result.left().value().get(0).getLeft());
        assertEquals(true, result.left().value().get(0).getRight());
    }
    
    @Test
    public void testCreateElementTypesByDao_capabilityTypeFound_UpgradeAlreadyExists() {
        CapabilityTypeDefinition type1 = new CapabilityTypeDefinition();
        CapabilityTypeDefinition type1_1 = new CapabilityTypeDefinition();
        List<Object> elementTypesToCreate = Arrays.asList(type1_1);
        
        ImmutablePair<ElementTypeEnum, String> elementInfo = new ImmutablePair<>(ElementTypeEnum.CAPABILITY_TYPE, "TestCapability"); 
        when(elementInfoGetter.apply(type1_1)).thenReturn(elementInfo);
        
        when(validator.apply(type1_1)).thenReturn(Either.left(ActionStatus.OK));
        when(elementFetcher.apply("TestCapability")).thenReturn(Either.left(type1));
        when(elementUpgrader.apply(type1_1, type1)).thenReturn(Either.right(StorageOperationStatus.OK));
        
        Either<List<ImmutablePair<Object, Boolean>>, ResponseFormat> result = commonImportManager.createElementTypesByDao(elementTypesToCreate , validator , elementInfoGetter, elementFetcher, elementAdder, elementUpgrader);

        verify(elementAdder, never()).apply(Mockito.any());
        verify(elementUpgrader).apply(type1_1, type1);
        verify(janusGraphGenericDao).commit();
        
        assertEquals(type1_1, result.left().value().get(0).getLeft());
        assertEquals(false, result.left().value().get(0).getRight());
    }


}
