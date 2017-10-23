package org.openecomp.sdc.be.tosca;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;

import fj.data.Either;

public class ToscaExportHandlerTest {

	private ToscaExportHandler createTestSubject() {
		return new ToscaExportHandler();
	}

	
	@Test
	public void testGetDependencies() throws Exception {
		ToscaExportHandler testSubject;
		Component component = null;
		Either<ToscaTemplate, ToscaError> result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetInterfaceFilename() throws Exception {
		String artifactName = "";
		String result;

		// default test
	}
}