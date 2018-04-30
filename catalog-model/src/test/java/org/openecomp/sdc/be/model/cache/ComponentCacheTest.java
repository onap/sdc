package org.openecomp.sdc.be.model.cache;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.cassandra.ComponentCassandraDao;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.resources.data.ComponentCacheData;
import org.slf4j.Logger;

import fj.data.Either;

public class ComponentCacheTest {
	ComponentCassandraDao componentCassandraDao = Mockito.mock(ComponentCassandraDao.class);

	ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
	Configuration configuration = Mockito.mock(Configuration.class);
	
	Logger logger = Mockito.mock(Logger.class);
	
	@InjectMocks
	ComponentCache componentCache = new ComponentCache();

	private ComponentCache createTestSubject() {
		return componentCache;
	}

	@Before 
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}




	@Test
	public void testIsEnabled() throws Exception {
		boolean result;

		// default test
		result = componentCache.isEnabled();
	}

	@Test
	public void testSetEnabled() throws Exception {
		boolean enabled = false;

		// default test
		componentCache.setEnabled(enabled);
	}



	@Test
	public void testGetAllComponentIdTimeAndType() throws Exception {
		Either<List<ComponentCacheData>, ActionStatus> result;

		// default test
		result = componentCache.getAllComponentIdTimeAndType();
	}


	@Test
	public void testDeleteComponentFromCache() throws Exception {
		ComponentCache testSubject;
		String id = "";
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteComponentFromCache(id);
	}
}