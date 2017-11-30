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

package org.openecomp.sdc.ci.tests.utilities;

public class OnboardingUtillViaApis {

//	protected static Map<String, String> prepareHeadersMap(String userId) {
//		Map<String, String> headersMap = new HashMap<String, String>();
//		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
//		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
//		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
//		return headersMap;
//	}
	
//	public static Pair<String, VendorSoftwareProductObject> createVspViaApis(ResourceReqDetails resourceReqDetails, String filepath, String vnfFile, User user, Boolean skipReport) throws Exception {
//
//		VendorSoftwareProductObject vendorSoftwareProductObject = new VendorSoftwareProductObject();
//
//		AmdocsLicenseMembers amdocsLicenseMembers = OnboardingUiUtils.createVendorLicense(user);
//		Pair<String, Map<String, String>> createVendorSoftwareProduct = OnboardingUiUtils.createVendorSoftwareProduct(resourceReqDetails, vnfFile, filepath, user, amdocsLicenseMembers);
//		Map<String, String> map = createVendorSoftwareProduct.right;
//		vendorSoftwareProductObject.setAttContact(map.get("attContact"));
//		vendorSoftwareProductObject.setCategory(map.get("category"));
//		vendorSoftwareProductObject.setComponentId(map.get("componentId"));
//		vendorSoftwareProductObject.setDescription(map.get("description"));
//		vendorSoftwareProductObject.setSubCategory(map.get("subCategory"));
//		vendorSoftwareProductObject.setVendorName(map.get("vendorName"));
//		vendorSoftwareProductObject.setVspId(map.get("vspId"));
//		Pair<String, VendorSoftwareProductObject> pair = new Pair<String, VendorSoftwareProductObject>(createVendorSoftwareProduct.left, vendorSoftwareProductObject);
//		return pair;
//	}
	
/*	public static Resource createResourceFromVSP(Pair<String, Map<String, String>> createVendorSoftwareProduct, String vspName) throws Exception {
		List<String> tags = new ArrayList<>();
		tags.add(vspName);
		Map<String, String> map = createVendorSoftwareProduct.right;
		ResourceReqDetails resourceDetails = new ResourceReqDetails();
		resourceDetails.setCsarUUID(map.get("vspId"));
		resourceDetails.setCsarVersion("1.0");
		resourceDetails.setName(vspName);
		resourceDetails.setTags(tags);
		resourceDetails.setDescription(map.get("description"));
		resourceDetails.setResourceType(map.get("componentType"));
		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_DATABASE.getCategory(), ResourceCategoryEnum.GENERIC_DATABASE.getSubCategory());
		resourceDetails.setVendorName(map.get("vendorName"));
		resourceDetails.setVendorRelease("1.0");
		resourceDetails.setResourceType("VF");
		resourceDetails.setResourceVendorModelNumber("666");
		resourceDetails.setContactId(map.get("attContact"));
		resourceDetails.setIcon("defaulticon");
		Resource resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
		
		return resource; 
	}*/

//	public static Resource createResourceFromVSP(ResourceReqDetails resourceDetails) throws Exception {
//		Resource resource = AtomicOperationUtils.createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
//		return resource;
//	}
	
//	public static void downloadToscaCsarToDirectory(Component component, File file) {
//		try {
//			Either<String, RestResponse> componentToscaArtifactPayload = AtomicOperationUtils.getComponenetArtifactPayload(component, "assettoscacsar");
//			if(componentToscaArtifactPayload.left().value() != null){
//				convertPayloadToFile(componentToscaArtifactPayload.left().value(), file);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
//	public static void convertPayloadToFile(String payload, File file, boolean isBased64, boolean isSdcFormat) throws IOException{
//		
//		Gson gson = new Gson();
//		byte[] byteArray = null;
//		Map<String, String> fromJson;
//		@SuppressWarnings("unchecked")
//		String string = null;// = fromJson.get("base64Contents").toString();
//		if(isSdcFormat){
//			fromJson = gson.fromJson(payload, Map.class);
//			string = fromJson.get("base64Contents").toString();
//		}else if (isBased64) {
//			byteArray = Base64.decode(string.getBytes(StandardCharsets.UTF_8));
//		}else{
//			byteArray = payload.getBytes(StandardCharsets.UTF_8);
//		}
//		File downloadedFile = new File(file.getAbsolutePath());
//		FileOutputStream fos = new FileOutputStream(downloadedFile);
//		fos.write(byteArray);
//		fos.flush();
//		fos.close();
//		
//	}

//	public static void convertPayloadToFile(String payload, File file) throws IOException{
//
//		Gson gson = new Gson();
//		@SuppressWarnings("unchecked")
//		Map<String, String> fromJson = gson.fromJson(payload, Map.class);
//		String string = fromJson.get("base64Contents").toString();
//		byte[] byteArray = Base64.decode(string.getBytes(StandardCharsets.UTF_8));
//		File downloadedFile = new File(file.getAbsolutePath());
//		FileOutputStream fos = new FileOutputStream(downloadedFile);
//		fos.write(byteArray);
//		fos.flush();
//		fos.close();
//	}
	
	
//	public static void convertPayloadToZipFile(String payload, File file) throws IOException{
//
//		byte[] byteArray = payload.getBytes(StandardCharsets.ISO_8859_1);
//		File downloadedFile = new File(file.getAbsolutePath());
//		FileOutputStream fos = new FileOutputStream(downloadedFile);
//		fos.write(byteArray);
//		fos.flush();
//		fos.close();
//
//
////		ZipOutputStream fos = null;
////
////
////		for (Charset charset : Charset.availableCharsets().values()) {
////			try{
////		//		System.out.println("How to do it???");
////				File downloadedFile = new File(file.getAbsolutePath() + "_" + charset +".csar");
////				fos = new ZipOutputStream(new FileOutputStream(downloadedFile));
////				byte[] byteArray = payload.getBytes(charset);
////				fos.write(byteArray);
////				fos.flush();
////
////			}
////			catch(Exception e){
////				fos.close();
////			}
////		}
//		System.out.println("");
//
////		ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(byteArray));
////		ZipEntry entry = null;
////		while ((entry = zipStream.getNextEntry()) != null) {
////
////		    String entryName = entry.getName();
////
////		    FileOutputStream out = new FileOutputStream(file+"/"+entryName);
////
////		    byte[] byteBuff = new byte[4096];
////		    int bytesRead = 0;
////		    while ((bytesRead = zipStream.read(byteBuff)) != -1)
////		    {
////		        out.write(byteBuff, 0, bytesRead);
////		    }
////
////		    out.close();
////		    zipStream.closeEntry();
////		}
////		zipStream.close();
////
//
//
//
//		BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(payload.getBytes(StandardCharsets.ISO_8859_1)));
//		String filePath = file.toString();
//		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
//		int inByte;
//		while((inByte = bis.read()) != -1) bos.write(inByte);
//		bis.close();
//		bos.close();
//	}
	
//	public static Either<String, RestResponse> getVendorSoftwareProduct(String vspId, User user, Boolean validateState) throws Exception {
//
//		Config config = Utils.getConfig();
//		String url = String.format(Urls.GET_VENDOR_SOFTWARE_PRODUCT, config.getCatalogBeHost(), config.getCatalogBePort(), vspId);
//		String userId = user.getUserId();
//		Map<String, String> headersMap = prepareHeadersMap(userId);
//		headersMap.put(HttpHeaderEnum.X_ECOMP_REQUEST_ID_HEADER.getValue(), "123456");
//		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "*/*");
//		headersMap.put("Accept-Encoding", "gzip, deflate, br");
//		HttpRequest http = new HttpRequest();
//		RestResponse response = http.httpSendGet(url, headersMap);
//		if (validateState) {
//			assertTrue("add property to resource failed: " + response.getResponseMessage(), response.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
//		}
//		if (response.getErrorCode() != BaseRestUtils.STATUS_CODE_SUCCESS && response.getResponse().getBytes() == null && response.getResponse().getBytes().length == 0) {
//			return Either.right(response);
//		}
//		return Either.left(response.getResponse());
//
//	}
	
//	public static ResourceReqDetails prepareOnboardedResourceDetailsBeforeCreate(ResourceReqDetails resourceDetails, VendorSoftwareProductObject vendorSoftwareProductObject) {
//
//		List<String> tags = new ArrayList<>();
//		tags.add(vendorSoftwareProductObject.getName());
////		ResourceReqDetails resourceDetails = new ResourceReqDetails();
//		resourceDetails.setCsarUUID(vendorSoftwareProductObject.getVspId());
//		resourceDetails.setCsarVersion(vendorSoftwareProductObject.getVersion());
//		resourceDetails.setName(vendorSoftwareProductObject.getName());
//		resourceDetails.setTags(tags);
//		resourceDetails.setDescription(vendorSoftwareProductObject.getDescription());
////		resourceDetails.addCategoryChain(ResourceCategoryEnum.GENERIC_DATABASE.getCategory(), ResourceCategoryEnum.GENERIC_DATABASE.getSubCategory());
//		resourceDetails.setVendorName(vendorSoftwareProductObject.getVendorName());
////		resourceDetails.setVendorRelease("1.0");
//		resourceDetails.setResourceType("VF");
//		resourceDetails.setResourceVendorModelNumber("666");
//		resourceDetails.setContactId(vendorSoftwareProductObject.getAttContact());
////		resourceDetails.setIcon("defaulticon");
//
//		return resourceDetails;
//	}
	
	/*public static ServiceReqDetails prepareServiceDetailsBeforeCreate(ServiceReqDetails serviceDetails, User user) {

		serviceDetails.setServiceType("MyServiceType");
		serviceDetails.setServiceRole("MyServiceRole");
		serviceDetails.setNamingPolicy("MyServiceNamingPolicy");
		serviceDetails.setEcompGeneratedNaming(true);
		
		return serviceDetails;
	}*/
}
