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

package org.openecomp.sdc.be.model.operations.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.be.config.Configuration.OnboardingConfig;
import org.openecomp.sdc.be.dao.rest.HttpRestClient;
import org.openecomp.sdc.be.dao.rest.RestConfigurationInfo;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.rest.api.RestResponse;
import org.openecomp.sdc.common.rest.api.RestResponseAsByteArray;
import org.openecomp.sdc.common.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("onboarding-client")
public class OnboardingClient {

	private static Logger log = LoggerFactory.getLogger(OnboardingClient.class.getName());

	private HttpRestClient httpRestClient = null;

	private static Properties downloadCsarHeaders = new Properties();

	static {
		downloadCsarHeaders.put("Accept", "application/octet-stream");
	}

	public OnboardingClient() {
		super();
	}

	public static void main(String[] args) {

		OnboardingClient csarOperation = new OnboardingClient();
		csarOperation.init();

		String csarUuid = "70025CF6081B489CA7B1CBA583D5278D";
		Either<Map<String, byte[]>, StorageOperationStatus> csar = csarOperation.getCsar(csarUuid, null);
		System.out.println(csar.left().value());

	}

	@PostConstruct
	public void init() {

		// TODO: read connection configuration from OnboardingConfig
		// onboardingConfig =
		// ConfigurationManager.getConfigurationManager().getConfiguration().getOnboarding();

		RestConfigurationInfo restConfigurationInfo = new RestConfigurationInfo();
		httpRestClient = new HttpRestClient(restConfigurationInfo);

		if (false == httpRestClient.isInitialized()) {
			BeEcompErrorManager.getInstance().logInternalFlowError("InitializeRestClient", "Failed to initialize rest client", ErrorSeverity.FATAL);
			httpRestClient = null;
		}

	}

	public Either<Map<String, byte[]>, StorageOperationStatus> getMockCsar(String csarUuid) {
		File dir = new File("/var/tmp/mockCsar");
		FileFilter fileFilter = new WildcardFileFilter("*.csar");
		File[] files = dir.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			File csar = files[i];
			if (csar.getName().startsWith(csarUuid)) {
				log.debug("Found CSAR file {} matching the passed csarUuid {}", csar.getAbsolutePath(), csarUuid);
				byte[] data;
				try {
					data = Files.readAllBytes(csar.toPath());
				} catch (IOException e) {
					log.debug("Error reading mock file for CSAR, error: {}", e);
					return Either.right(StorageOperationStatus.NOT_FOUND);
				}
				Map<String, byte[]> readZip = ZipUtil.readZip(data);
				return Either.left(readZip);
			}
		}
		log.debug("Couldn't find mock file for CSAR starting with {}", csarUuid);
		return Either.right(StorageOperationStatus.NOT_FOUND);
	}

	public Either<Map<String, byte[]>, StorageOperationStatus> getCsar(String csarUuid, String userId) {

		if (httpRestClient == null) {
			BeEcompErrorManager.getInstance().logInternalFlowError("RestClient", "Rest Client could not be initialized", ErrorSeverity.ERROR);
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}

		String url = buildDownloadCsarUrl() + "/" + csarUuid;

		Properties headers = new Properties();
		if (downloadCsarHeaders != null) {
			downloadCsarHeaders.forEach((k, v) -> headers.put(k, v));
		}

		if (userId != null) {
			headers.put(Constants.USER_ID_HEADER, userId);
		}

		log.debug("Url for downloading csar is {}. Headers are {}", url, headers);

		RestResponseAsByteArray restResponse = httpRestClient.doGetAsByteArray(url, headers);
		log.debug("After fetching csar {}. Http return code is {}", csarUuid, restResponse.getHttpStatusCode());

		switch (restResponse.getHttpStatusCode()) {
		case HttpStatus.SC_OK:
			byte[] data = restResponse.getResponse();
			if (data != null && data.length > 0) {
				Map<String, byte[]> readZip = ZipUtil.readZip(data);
				return Either.left(readZip);
			} else {
				log.debug("Data received from rest is null or empty");
				return Either.right(StorageOperationStatus.NOT_FOUND);
			}

		case HttpStatus.SC_NOT_FOUND:
			return Either.right(StorageOperationStatus.CSAR_NOT_FOUND);

		default:
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}

	}
	
	public Either<String, StorageOperationStatus> getPackages(String userId) {

		if (httpRestClient == null) {
			BeEcompErrorManager.getInstance().logInternalFlowError("RestClient", "Rest Client could not be initialized", ErrorSeverity.ERROR);
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}

		String url = buildDownloadCsarUrl();

		Properties headers = new Properties();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");

		if (userId != null) {
			headers.put(Constants.USER_ID_HEADER, userId);
		}

		log.debug("Url for downloading packages is {}. Headers are {}", url, headers);

		RestResponse restResponse = httpRestClient.doGET(url, headers);
		log.debug("After fetching packages. Http return code is {}", restResponse.getHttpStatusCode());

		switch (restResponse.getHttpStatusCode()) {
		case HttpStatus.SC_OK:
			String data = restResponse.getResponse();
			return Either.left(data);

		case HttpStatus.SC_NOT_FOUND:
			return Either.right(StorageOperationStatus.CSAR_NOT_FOUND);

		default:
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}

	}

	public HttpRestClient getHttpRestClient() {
		return httpRestClient;
	}

	public void setHttpRestClient(HttpRestClient httpRestClient) {
		this.httpRestClient = httpRestClient;
	}

	/**
	 * Build the url for download CSAR
	 * 
	 * E.g., http://0.0.0.0:8181/onboarding-api/v1.0/vendor-software-products/packages/
	 * 
	 * @return
	 */
	public String buildDownloadCsarUrl() {

		OnboardingConfig onboardingConfig = ConfigurationManager.getConfigurationManager().getConfiguration().getOnboarding();

		String protocol = onboardingConfig.getProtocol();
		String host = onboardingConfig.getHost();
		Integer port = onboardingConfig.getPort();
		String uri = onboardingConfig.getDownloadCsarUri();

		String getCsarUrl = protocol + "://" + host + ":" + port + uri;

		return getCsarUrl;
	}

}
