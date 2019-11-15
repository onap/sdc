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

package org.openecomp.sdc.ci.tests.utils.general;

import com.google.gson.Gson;
import fj.data.Either;
import org.apache.commons.codec.binary.Base64;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertTrue;

public class OnboardingUtillViaApis {

	public static VendorSoftwareProductObject createVspViaApis(ResourceReqDetails resourceReqDetails, String filepath, String vnfFile, User user) throws Exception {

		VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(user);
		return VendorSoftwareProductRestUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filepath, user,
            vendorLicenseModel);
	}
	
	public static Resource createResourceFromVSP(ResourceReqDetails resourceDetails) throws Exception {
		Resource resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		return resource;

	}
	
	public static Resource createResourceFromVSP(ResourceReqDetails resourceDetails, UserRoleEnum userRole) throws Exception {
		Resource resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, userRole, true).left().value();
		return resource;

	}
	
	public static void downloadToscaCsarToDirectory(Component component, File file) {
		try {
			Either<String, RestResponse> componentToscaArtifactPayload = AtomicOperationUtils.getComponenetArtifactPayload(component, "assettoscacsar");
			if(componentToscaArtifactPayload.left().value() != null){
				convertPayloadToFile(componentToscaArtifactPayload.left().value(), file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void convertPayloadToFile(String payload, File file) throws IOException{
		
		Gson gson = new Gson();
		@SuppressWarnings("unchecked")
		Map<String, String> fromJson = gson.fromJson(payload, Map.class);
		String string = fromJson.get("base64Contents").toString();
		byte[] byteArray = Base64.decodeBase64(string.getBytes(StandardCharsets.UTF_8));
		File downloadedFile = new File(file.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(downloadedFile);
		fos.write(byteArray);
		fos.flush();
		fos.close();
	}
	
	
	public static void convertPayloadToZipFile(String payload, File file) throws IOException{
		
		byte[] byteArray = payload.getBytes(StandardCharsets.ISO_8859_1);
		File downloadedFile = new File(file.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(downloadedFile);
		fos.write(byteArray);
		fos.flush();
		fos.close();
		
		System.out.println("");
		
		BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.ISO_8859_1)));
		String filePath = file.toString();
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
		int inByte;
		while((inByte = bis.read()) != -1) bos.write(inByte);
		bis.close();
		bos.close();
	}
	
	public static Either<String, RestResponse> getVendorSoftwareProduct(String vspId, User user, Boolean validateState) throws IOException {

		Config config = Utils.getConfig();
		String url = String.format(Urls.GET_VENDOR_SOFTWARE_PRODUCT, config.getOnboardingBeHost(), config.getOnboardingBePort(), vspId);
		String userId = user.getUserId();
		Map<String, String> headersMap = OnboardingUtils.prepareHeadersMap(userId);
		headersMap.put(HttpHeaderEnum.X_ECOMP_REQUEST_ID_HEADER.getValue(), "123456");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "*/*");
		headersMap.put("Accept-Encoding", "gzip, deflate, br");
		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendGet(url, headersMap);
		if (validateState) {
			assertTrue("add property to resource failed: " + response.getResponseMessage(), response.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		}
		if (response.getErrorCode() != BaseRestUtils.STATUS_CODE_SUCCESS && response.getResponse().getBytes() == null && response.getResponse().getBytes().length == 0) {
			return Either.right(response);
		}
		return Either.left(response.getResponse());
		
	}

	public static ResourceReqDetails prepareOnboardedResourceDetailsBeforeCreate(ResourceReqDetails resourceDetails, VendorSoftwareProductObject vendorSoftwareProductObject) {

		List<String> tags = new ArrayList<>();
		tags.add(vendorSoftwareProductObject.getName());
		resourceDetails.setCsarUUID(vendorSoftwareProductObject.getVspId());
		resourceDetails.setCsarVersion(vendorSoftwareProductObject.getVersion());
		resourceDetails.setName(vendorSoftwareProductObject.getName());
		resourceDetails.setTags(tags);
		resourceDetails.setDescription(vendorSoftwareProductObject.getDescription());
		resourceDetails.setVendorName(vendorSoftwareProductObject.getVendorName());
		resourceDetails.setResourceType("VF");
		resourceDetails.setResourceVendorModelNumber("666");
		resourceDetails.setContactId(vendorSoftwareProductObject.getAttContact());

		return resourceDetails;
	}
	
	public static ServiceReqDetails prepareServiceDetailsBeforeCreate(User user) {

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService(ServiceCategoriesEnum.NETWORK_L4, user);
		serviceDetails.setServiceType("MyServiceType");
		serviceDetails.setServiceRole("MyServiceRole");
		serviceDetails.setNamingPolicy("MyServiceNamingPolicy");
		serviceDetails.setEcompGeneratedNaming(false);
		
		return serviceDetails;
	}
}
