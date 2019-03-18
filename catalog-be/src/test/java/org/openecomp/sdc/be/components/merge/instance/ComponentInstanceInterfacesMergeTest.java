package org.openecomp.sdc.be.components.merge.instance;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;

public class ComponentInstanceInterfacesMergeTest {

  @InjectMocks
  private ComponentInstanceInterfacesMerge componentInstanceInterfacesMerge;

  @Mock
  private DataForMergeHolder dataHolder;

  @Mock
  private ComponentsUtils componentsUtils;

  @Mock
  private Component containerComponent;

  @Mock
  private ToscaOperationFacade toscaOperationFacade;

  private Component updatedContainer;
  private Component origContainer;
  private Component origComponent;
  private ComponentInstance currentResourceInstance;
  private ComponentInstanceInterface origComponentInstanceInterface;
  private ComponentInstanceInterface newComponentInstanceInterface;
  private User user;

  @Before
  public void setUpData() {
    MockitoAnnotations.initMocks(this);
    user = new User();
    user.setUserId("44");
    user.setRole(UserRoleEnum.ADMIN.getName());

    currentResourceInstance = new ComponentInstance();
    currentResourceInstance.setUniqueId("TestUniqueID1");
    currentResourceInstance.setComponentUid("TestUID1");

    origComponent = new Service();
    origComponent.setUniqueId("TestUniqueID12");

    dataHolder = new DataForMergeHolder();
    dataHolder.setOrigInstanceNode(origComponent);

    Map<String, InterfaceDefinition> origInterfaceDefinition =
            InterfaceOperationTestUtils.createMockInterfaceDefinitionMap("Interface1", "Operation1", "Operation1");
    origComponentInstanceInterface = new ComponentInstanceInterface("TestService1", origInterfaceDefinition.get("Interface1"));

    Map<String, InterfaceDefinition> newInterfaceDefinition =
            InterfaceOperationTestUtils.createMockInterfaceDefinitionMap("Interface2", "Operation2", "Operation2");
    newComponentInstanceInterface = new ComponentInstanceInterface("TestService2", newInterfaceDefinition.get("Interface2"));

    when(toscaOperationFacade.updateComponentInstanceInterfaces(any(), anyString())).thenReturn(StorageOperationStatus.OK);
    when(componentsUtils.convertFromStorageResponse(any())).thenReturn(ActionStatus.OK);

    ComponentInstance componentInstance = new ComponentInstance();
    componentInstance.setUniqueId("CI_1");
    componentInstance.setInterfaces((Map) newInterfaceDefinition);

    Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces = new HashMap<>();
    componentInstanceInterfaces.put(componentInstance.getUniqueId(), Collections.singletonList(newComponentInstanceInterface));

    updatedContainer = new Service();
    updatedContainer.setComponentInstances(Collections.singletonList(componentInstance));
    updatedContainer.setComponentInstancesInterfaces(componentInstanceInterfaces);

    origContainer = new Service();
    origContainer.setComponentInstances(Collections.singletonList(componentInstance));
    origContainer.setComponentInstancesInterfaces(componentInstanceInterfaces);
  }

  @Test
  public void saveDataBeforeMerge() {
    doReturn(Collections.singletonList(origComponentInstanceInterface)).when(containerComponent).safeGetComponentInstanceInterfaces(any());
    componentInstanceInterfacesMerge.saveDataBeforeMerge(dataHolder, containerComponent, currentResourceInstance, origComponent);
    assertEquals(origComponent, dataHolder.getOrigInstanceNode());
    assertEquals(origComponentInstanceInterface, dataHolder.getOrigComponentInstanceInterfaces().get(0));
  }

  @Test
  public void mergeDataAfterCreate() {
    doReturn(Collections.singletonList(origComponentInstanceInterface)).when(containerComponent).safeGetComponentInstanceInterfaces(any());
    componentInstanceInterfacesMerge.saveDataBeforeMerge(dataHolder, containerComponent, currentResourceInstance, origComponent);
    componentInstanceInterfacesMerge.mergeDataAfterCreate(user, dataHolder, updatedContainer, "CI_1");
    assertEquals(updatedContainer.getComponentInstancesInterfaces().get("CI_1"), origContainer.getComponentInstancesInterfaces().get("CI_1"));
  }
}