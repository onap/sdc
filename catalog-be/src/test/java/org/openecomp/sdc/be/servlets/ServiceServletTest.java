package org.openecomp.sdc.be.servlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.exception.ResponseFormat;

import com.google.common.base.Equivalence.Wrapper;
import com.google.common.util.concurrent.Service;

import fj.data.Either;

public class ServiceServletTest {

	private ServiceServlet createTestSubject() {
		return new ServiceServlet();
	}

	
	@Test
	public void testCreateService() throws Exception {
		ServiceServlet testSubject;
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testParseToService() throws Exception {
		ServiceServlet testSubject;
		String serviceJson = "";
		User user = null;
		Either<Service, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateServiceName() throws Exception {
		ServiceServlet testSubject;
		String serviceName = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetComponentAuditRecords() throws Exception {
		ServiceServlet testSubject;
		String componentType = "";
		String componentUniqueId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testFillUUIDAndVersion() throws Exception {
	ServiceServlet testSubject;Wrapper<Response> responseWrapper = null;
	Wrapper<String> uuidWrapper = null;
	Wrapper<String> versionWrapper = null;
	User user = null;
	ComponentTypeEnum componentTypeEnum = null;
	String componentUniqueId = "";
	ServletContext context = null;
	
	
	// default test
	}

	
	@Test
	public void testDeleteService() throws Exception {
		ServiceServlet testSubject;
		String serviceId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDeleteServiceByNameAndVersion() throws Exception {
		ServiceServlet testSubject;
		String serviceName = "";
		String version = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateServiceMetadata() throws Exception {
		ServiceServlet testSubject;
		String serviceId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateGroupInstancePropertyValues() throws Exception {
		ServiceServlet testSubject;
		String serviceId = "";
		String componentInstanceId = "";
		String groupInstanceId = "";
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetServiceById() throws Exception {
		ServiceServlet testSubject;
		String serviceId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testGetServiceByNameAndVersion() throws Exception {
		ServiceServlet testSubject;
		String serviceName = "";
		String serviceVersion = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testUpdateServiceDistributionState() throws Exception {
		ServiceServlet testSubject;
		LifecycleChangeInfoWithAction jsonChangeInfo = null;
		String serviceId = "";
		String state = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testActivateDistribution() throws Exception {
		ServiceServlet testSubject;
		String serviceId = "";
		String env = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testMarkDistributionAsDeployed() throws Exception {
		ServiceServlet testSubject;
		String serviceId = "";
		String did = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testTempUrlToBeDeleted() throws Exception {
		ServiceServlet testSubject;
		String serviceId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testDownloadServiceArtifact() throws Exception {
		ServiceServlet testSubject;
		String artifactName = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
		
	}

	
	@Test
	public void testExecuteCommand() throws Exception {
		ServiceServlet testSubject;
		String artifactName = "";
		Either<byte[], ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}
}