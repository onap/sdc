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

import javax.annotation.PostConstruct;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.util.ZipUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

@org.springframework.stereotype.Component("csar-operation")
public class CsarOperation {

	private static Logger log = LoggerFactory.getLogger(CsarOperation.class.getName());

	@javax.annotation.Resource
	private OnboardingClient onboardingClient;

	public static void main(String[] args) {

		CsarOperation csarOperation = new CsarOperation();
		csarOperation.init();

		String csarUuid = "70025CF6081B489CA7B1CBA583D5278D";
		Either<Map<String, byte[]>, StorageOperationStatus> csar = csarOperation.getCsar(csarUuid, null);
		System.out.println(csar.left().value());

	}

	@PostConstruct
	public void init() {

	}

	// Mock returning a file from the file system until we have API from onboarding
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
		return Either.right(StorageOperationStatus.CSAR_NOT_FOUND);
	}

	/**
	 * get csar from remote repository
	 * 
	 * @param csarUuid
	 * @return
	 */
	public Either<Map<String, byte[]>, StorageOperationStatus> getCsar(String csarUuid, User user) {

		Either<Map<String, byte[]>, StorageOperationStatus> result = onboardingClient.getCsar(csarUuid,
				user.getUserId());

		if (result.isRight()) {
			log.debug("Cannot find csar {}. Status returned is {}", csarUuid, result.right().value());
		} else {
			Map<String, byte[]> values = result.left().value();
			if (values != null) {
				log.debug("The returned files are {}", values.keySet());
			}
		}

		return result;
	}

	public OnboardingClient getOnboardingClient() {
		return onboardingClient;
	}

}
