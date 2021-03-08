/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.listen;

import mockit.Deencapsulation;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BEAppContextListenerTest {
	@InjectMocks
	private BEAppContextListener testSubject;

	@Mock
	private ServletContextEvent servletContextEvent;

	@Mock
	private ServletContext servletContext;

	@Test
	public void testGetVersionFromManifestFailed() {
		InputStream inputStream = BEAppContextListenerTest.class.getResourceAsStream("/Test_MANIFEST_Err.MF");
		Mockito.when(servletContextEvent.getServletContext()).thenReturn(servletContext);
		Mockito.when(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF")).thenReturn(inputStream);

		String result = Deencapsulation.invoke(testSubject, "getVersionFromManifest", servletContextEvent);
		verify(servletContextEvent, times(1)).getServletContext();
		verify(servletContext, times(1)).getResourceAsStream("/META-INF/MANIFEST.MF");
		assertNull(result);
	}

	@Test
	public void testGetVersionFromManifest() {
		InputStream inputStream = BEAppContextListenerTest.class.getResourceAsStream("/Test_MANIFEST.MF");
		Mockito.when(servletContextEvent.getServletContext()).thenReturn(servletContext);
		Mockito.when(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF")).thenReturn(inputStream);

		String result = Deencapsulation.invoke(testSubject, "getVersionFromManifest", servletContextEvent);
		verify(servletContextEvent, times(1)).getServletContext();
		verify(servletContext, times(1)).getResourceAsStream("/META-INF/MANIFEST.MF");

		assertEquals(result, "1.0");
	}
}