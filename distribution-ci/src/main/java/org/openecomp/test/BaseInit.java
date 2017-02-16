package org.openecomp.test;

//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertTrue;
//
//import java.io.IOException;
//
//import org.junit.Before;
//import org.junit.rules.TestName;
//
//import org.openecomp.sdc.be.model.User;
//import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
//import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
//import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
//import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
//import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
//import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
////import org.openecomp.sdc.ci.tests.execute.lifecycle.LCSbaseTest;
//import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
//import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
//import org.openecomp.sdc.ci.tests.utils.ArtifactUtils;
//import org.openecomp.sdc.ci.tests.utils.DbUtils;
////import org.openecomp.sdc.ci.tests.utils.ResourceUtils;
////import org.openecomp.sdc.ci.tests.utils.ServiceUtils;
//import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
//import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
//import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
//import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
//import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
//import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public abstract class BaseInit {

	
//	protected ResourceReqDetails resourceDetails;
//	protected ResourceReqDetails resourceDetails1;
//	protected ServiceReqDetails serviceDetails;
//	protected ServiceReqDetails serviceDetails2;
//	protected ComponentInstanceReqDetails resourceInstanceReqDetails;
//	protected ComponentInstanceReqDetails resourceInstanceReqDetails2;
//	protected User sdncDesignerDetails1;
//	protected User sdncTesterDeatails1;
//	protected User sdncAdminDetails1;
//	protected ArtifactReqDetails heatArtifactDetails;
//	
//	protected ArtifactReqDetails defaultArtifactDetails;
//	//protected ResourceUtils resourceUtils;
//	protected ArtifactUtils artifactUtils;
//	
//	
//	//static ServiceUtils serviceUtils = new ServiceUtils();
//	public BaseInit(TestName testName, String className) {
//		super(testName, className);
//	}
//	@Before
//	public void before() throws Exception{
//
//		initializeMembers();
//		
//		createComponents();
//	
//	}
//	public void initializeMembers() throws IOException, Exception {
//		
//		resourceDetails = ElementFactory.getDefaultResource();
//		resourceDetails1 = ElementFactory.getDefaultResource("secondResource", NormativeTypesEnum.ROOT);
//		serviceDetails = ElementFactory.getDefaultService();
//		serviceDetails2 = ElementFactory.getDefaultService("newTestService2", ServiceCategoriesEnum.MOBILITY, "al1976");
//		sdncDesignerDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
//		sdncTesterDeatails1 = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
//		sdncAdminDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
//		heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
//		resourceInstanceReqDetails = ElementFactory.getDefaultComponentInstance();
//		resourceInstanceReqDetails2 = ElementFactory.getDefaultComponentInstance();
//		
//	}
//	protected void createComponents() throws Exception{
//		
////		Create resources
//		RestResponse response = ResourceRestUtils.createResource(resourceDetails, sdncDesignerDetails1);		
//		assertTrue("create request returned status:" + response.getErrorCode(),response.getErrorCode() == 201);
//		assertNotNull("resource uniqueId is null:", resourceDetails.getUniqueId());
//
//		response = ResourceRestUtils.createResource(resourceDetails1, sdncDesignerDetails1);		
//		assertTrue("create request returned status:" + response.getErrorCode(),response.getErrorCode() == 201);
//		assertNotNull("resource uniqueId is null:", resourceDetails1.getUniqueId());
//		
////		Create services
//		response = ServiceRestUtils.createService(serviceDetails, sdncDesignerDetails1);		
//		assertTrue("create request returned status:" + response.getErrorCode(),response.getErrorCode() == 201);
//		assertNotNull("service uniqueId is null:", serviceDetails.getUniqueId());
//		
//		response = ServiceRestUtils.createService(serviceDetails2, sdncDesignerDetails1);		
//		assertTrue("create request returned status:" + response.getErrorCode(),response.getErrorCode() == 201);
//		assertNotNull("service uniqueId is null:", serviceDetails2.getUniqueId());
//		
//		ArtifactReqDetails heatArtifactDetails = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
//		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails, sdncDesignerDetails1, resourceDetails.getUniqueId());
//		assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),response.getErrorCode() == 200);
//		
//		ArtifactReqDetails heatArtifactDetails1 = ElementFactory.getDefaultDeploymentArtifactForType(ArtifactTypeEnum.HEAT.getType());
//		response = ArtifactRestUtils.addInformationalArtifactToResource(heatArtifactDetails1, sdncDesignerDetails1, resourceDetails1.getUniqueId());
//		assertTrue("add HEAT artifact to resource request returned status:" + response.getErrorCode(),response.getErrorCode() == 200);
//		
//		// certified resources
////		response = LCSbaseTest.certifyResource(resourceDetails);
////		assertTrue("certify resource request returned status:" + response.getErrorCode(),response.getErrorCode() == 200);
////		
////		response = LCSbaseTest.certifyResource(resourceDetails1);
////		assertTrue("certify resource request returned status:" + response.getErrorCode(),response.getErrorCode() == 200);
////		
////		add resource instance with HEAT deployment artifact to the service
//		resourceInstanceReqDetails.setUniqueId(resourceDetails.getUniqueId());
////		response = ServiceUtils.createResourceInstance(resourceInstanceReqDetails, sdncDesignerDetails1, serviceDetails.getUniqueId());
////		assertTrue("response code is not 200, returned: " + response.getErrorCode(),response.getErrorCode() == 200);
//		
//		DbUtils.cleanAllAudits();
//	
//	
//	}
	
}
