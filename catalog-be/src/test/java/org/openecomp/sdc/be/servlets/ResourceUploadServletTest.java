/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.servlets;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.resources.api.IResourceUploader;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;

public class ResourceUploadServletTest extends JerseyTest {
	private static Logger log = LoggerFactory.getLogger(ResourceUploadServletTest.class.getName());
	final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
	final HttpSession session = Mockito.mock(HttpSession.class);
	final ServletContext servletContext = Mockito.mock(ServletContext.class);
	final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
	final IResourceUploader iResourceUploader = Mockito.mock(IResourceUploader.class);
	final AuditingManager iAuditingManager = Mockito.mock(AuditingManager.class);

	Gson gson = new Gson();

	public void zipDirectory() {

	}

	@Before
	public void setup() {
		ExternalConfiguration.setAppName("catalog-be");

		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
		// when(servletContext.getAttribute(Constants.AUDITING_MANAGER)).thenReturn(iAuditingManager);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
		when(webApplicationContext.getBean(IResourceUploader.class)).thenReturn(iResourceUploader);
		when(iResourceUploader.saveArtifact((ESArtifactData) anyObject(), eq(true))).thenReturn(ResourceUploadStatus.OK);
		when(webApplicationContext.getBean(AuditingManager.class)).thenReturn(iAuditingManager);
	}

	@Override
	protected Application configure() {

		ResourceConfig resourceConfig = new ResourceConfig(ResourceUploadServlet.class);

		resourceConfig.register(MultiPartFeature.class);
		resourceConfig.register(new AbstractBinder() {

			@Override
			protected void configure() {
				// The below code was cut-pasted to here from setup() because
				// due to it now has
				// to be executed during servlet initialization
				bind(request).to(HttpServletRequest.class);
				when(request.getSession()).thenReturn(session);
				when(session.getServletContext()).thenReturn(servletContext);
				String appConfigDir = "src/test/resources/config/catalog-be";
				ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
				ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
				for (String mandatoryHeader : configurationManager.getConfiguration().getIdentificationHeaderFields()) {

					when(request.getHeader(mandatoryHeader)).thenReturn(mandatoryHeader);

				}

				when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
			}
		});

		return resourceConfig;
	}

	@Override
	protected void configureClient(ClientConfig config) {
		config.register(MultiPartFeature.class);
	}

	public class DelegatingServletInputStream extends ServletInputStream {

		private final InputStream sourceStream;

		/**
		 * Create a DelegatingServletInputStream for the given source stream.
		 * 
		 * @param sourceStream
		 *            the source stream (never <code>null</code>)
		 */
		public DelegatingServletInputStream(InputStream sourceStream) {
			// Assert.notNull(sourceStream,
			// "Source InputStream must not be null");
			this.sourceStream = sourceStream;
		}

		/**
		 * Return the underlying source stream (never <code>null</code>).
		 */
		public final InputStream getSourceStream() {
			return this.sourceStream;
		}

		public int read() throws IOException {
			return this.sourceStream.read();
		}

		public void close() throws IOException {
			super.close();
			this.sourceStream.close();
		}

	}

	@Test
	public void testMultipart() {
		FileDataBodyPart filePart = new FileDataBodyPart("resourceZip", new File("src/test/resources/config/normative-types-root.zip"));

		List<String> tags = new ArrayList<String>();
		tags.add("tag1");
		tags.add("tag2");
		UploadResourceInfo resourceInfo = new UploadResourceInfo("payload", "normative-types-root.yml", "my_description", "category/mycategory", tags, null);

		FormDataBodyPart metadataPart = new FormDataBodyPart("resourceMetadata", gson.toJson(resourceInfo), MediaType.APPLICATION_JSON_TYPE);
		MultiPart multipartEntity = new FormDataMultiPart();
		multipartEntity.bodyPart(filePart);
		multipartEntity.bodyPart(metadataPart);

		Response response = target().path("/v1/catalog/upload/" + ResourceUploadServlet.NORMATIVE_TYPE_RESOURCE).request(MediaType.APPLICATION_JSON).post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);
		log.debug("{}", response);
	}

}
