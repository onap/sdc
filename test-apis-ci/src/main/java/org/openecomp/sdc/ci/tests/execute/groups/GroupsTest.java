package org.openecomp.sdc.ci.tests.execute.groups;

import fj.data.Either;
import org.apache.commons.collections.MapUtils;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.lifecycle.LCSbaseTest;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils.*;
import static org.testng.Assert.assertNull;

public class GroupsTest extends ComponentBaseTest {
	@Rule
	public static TestName name = new TestName();
	
	public static final String groupName = "x_group";
	public static final String capabilityName = "vlan_assignment";
	public static final String propertyName = "vfc_instance_group_reference";
	public static final String csarsFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "CI" + File.separator + "csars" ;
	
	public GroupsTest() {
		super(name,  GroupsTest.class.getName());
	}
	
	@Test
	public void importResourceWithGroupsTest() throws Exception {
		importResource("with_groups.csar");
	}
	
	@Test
	public void importResourceWitIncorrectCapabilityNameTest() throws Exception {
		RestResponse createResource = getCreateResourceRestResponse(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, "incorrect_cap.csar", csarsFilePath);
		BaseRestUtils.checkErrorResponse(createResource, ActionStatus.MISSING_CAPABILITIES,(Lists.newArrayList("vlan_assignment1")).toString(), "group", "x_group");
	}
	
	@Test
	public void importResourceWithoutCapabilitiesTest() throws Exception {
		Resource resource =  importResourceFromCsar(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, "without_caps.csar", csarsFilePath);
		validateComponentGroupCapabilityPropertyValue(resource, null);
	}
	
	@Test
	public void updateResourceWithGroupsTest() throws Exception {
		Resource resource = importResource("with_groups.csar");
		updateResource(resource, "with_groups_update.csar");
	}

	@Test
	public void supportGroupsWithCapabilitiesServiceLevelTest() throws Exception {
		Resource resource = importCertifiedResourceFromCsar(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, "VLANTaggingFromAmdox1303_2018.csar", csarsFilePath);
        CapReqDef caps = ComponentInstanceRestUtils.getInstancesCapabilitiesRequirements(resource, UserRoleEnum.DESIGNER.getUserId());
		validateVlanAssignmentGroupCapabilitiesInvisible(caps.getCapabilities());

        Service service1 = createCertifiedServiceWithInstance(ServiceCategoriesEnum.MOBILITY, resource);
        caps = ComponentInstanceRestUtils.getInstancesCapabilitiesRequirements(service1, UserRoleEnum.DESIGNER.getUserId());
		validateVlanAssignmentGroupCapabilitiesVisible(caps.getCapabilities());

        Service service2 = createCertifiedServiceWithInstance(ServiceCategoriesEnum.NETWORK_L4, resource);
        caps = ComponentInstanceRestUtils.getInstancesCapabilitiesRequirements(service2, UserRoleEnum.DESIGNER.getUserId());
		validateVlanAssignmentGroupCapabilitiesVisible(caps.getCapabilities());

        Service serviceContainer = createCertifiedServiceWithProxyInstances(ServiceCategoriesEnum.NETWORK_L3, service1, service2);
        caps = ComponentInstanceRestUtils.getInstancesCapabilitiesRequirements(serviceContainer, UserRoleEnum.DESIGNER.getUserId());
		validateVlanAssignmentGroupCapabilitiesVisible(caps.getCapabilities());

        assertTrue(serviceContainer.getComponentInstances()!=null);
    }

	private void validateVlanAssignmentGroupCapabilitiesVisible(Map<String, List<CapabilityDefinition>> capabilities) {
		if(MapUtils.isNotEmpty(capabilities)){
			validateVlanAssignmentGroupCapabilitiesVisibility(capabilities, true);
		}
	}

	private void validateVlanAssignmentGroupCapabilitiesInvisible(Map<String, List<CapabilityDefinition>> capabilities) {
		if(MapUtils.isNotEmpty(capabilities)){
			validateVlanAssignmentGroupCapabilitiesVisibility(capabilities, false);
		}
	}
    private void validateVlanAssignmentGroupCapabilitiesVisibility(Map<String, List<CapabilityDefinition>> capabilities, boolean shouldBeVisible) {
		assertTrue(capabilities.containsKey("org.openecomp.capabilities.VLANAssignment") == shouldBeVisible);
    }

    private Service createCertifiedServiceWithProxyInstances(ServiceCategoriesEnum category, Service service1, Service service2) throws Exception {
        Either<Service, RestResponse> createServiceRes = createServiceByCategory(category, UserRoleEnum.DESIGNER, true);
        assertTrue(createServiceRes.isLeft());
        Either<ComponentInstance, RestResponse> result = addComponentInstanceToComponentContainer(service1, createServiceRes.left().value());
        assertTrue(result.isLeft());
        result = addComponentInstanceToComponentContainer(service2, createServiceRes.left().value());
        assertTrue(result.isLeft());
        return certifyService(createServiceRes);
    }

    private Service createCertifiedServiceWithInstance(ServiceCategoriesEnum category, Resource resource) throws Exception {
        Either<Service, RestResponse> createServiceRes = createServiceByCategory(category, UserRoleEnum.DESIGNER, true);
        assertTrue(createServiceRes.isLeft());
        Either<ComponentInstance, RestResponse> result = addComponentInstanceToComponentContainer(resource, createServiceRes.left().value());
        assertTrue(result.isLeft());
        return certifyService(createServiceRes);
    }

    private Service certifyService(Either<Service, RestResponse> serviceProxy1) throws Exception {
        Service service = getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, serviceProxy1.left().value().getName(), "0.1" );
        assertNotNull(service);
        ServiceReqDetails serviceReqDetails = new ServiceReqDetails(service);
        RestResponse restResponseService = LCSbaseTest.certifyService(serviceReqDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
        assertTrue(restResponseService.getErrorCode()==200);
        return getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, serviceProxy1.left().value().getName(), "1.0" );
    }

    private static Resource updateResource(Resource resource, String csarFileName) throws Exception {
		Resource updatedResource = updateResourceFromCsar(resource, UserRoleEnum.DESIGNER, csarFileName, csarsFilePath);
		validateComponentGroupCapabilityPropertyValue(updatedResource, "new_value");
		return updatedResource;
	}

	private static Resource importResource(String csarFileName) throws Exception {
		Resource resource = importResourceFromCsar(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, csarFileName, csarsFilePath);
		validateComponentGroupCapabilityPropertyValue(resource, "success");
		return resource;
	}

	private static void validateComponentGroupCapabilityPropertyValue(Component component, String propertyValue) {
		assertNotNull(component);
		assertNotNull(component.getGroups());
		assertFalse(component.getGroups().isEmpty());
		assertTrue(component.getGroups().size() == 5);
		Optional<GroupDefinition> vfcInstanceGroup = component.getGroups().stream().filter(g->g.getName().equals(groupName)).findFirst();
		assertTrue(vfcInstanceGroup.isPresent());
		assertFalse(vfcInstanceGroup.get().getCapabilities().isEmpty());
		assertTrue(vfcInstanceGroup.get().getCapabilities().size() == 1);
		assertNotNull(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment"));
		assertTrue(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment").size() == 1);
		assertNotNull(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment").get(0));
		assertTrue(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment").get(0).getName().equals(capabilityName));
		assertNotNull(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment").get(0).getProperties());
		assertTrue(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment").get(0).getProperties().size() == 1);
		assertTrue(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment").get(0).getProperties().get(0).getName().equals(propertyName));
		if(propertyValue == null)
			assertNull(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment").get(0).getProperties().get(0).getValue());
		else
			assertTrue(vfcInstanceGroup.get().getCapabilities().get("org.openecomp.capabilities.VLANAssignment").get(0).getProperties().get(0).getValue().equals(propertyValue));
	}
	
}
