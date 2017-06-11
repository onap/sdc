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

package org.openecomp.sdc.ci.tests.utils;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.common.api.ToscaNodeTypeInfo;
import org.openecomp.sdc.common.api.YamlConstants;
import org.yaml.snakeyaml.Yaml;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Utils {

	Gson gson = new Gson();

	static Logger logger = Logger.getLogger(Utils.class.getName());

	String contentTypeHeaderData = "application/json";
	String acceptHeaderDate = "application/json";

	public Utils() {
		/*
		 * super();
		 * 
		 * StartTest.enableLogger(); logger =
		 * Logger.getLogger(Utils.class.getName());
		 */

	}

	// public String serviceTopologyPattern = "/topology/topology/%s";
	// public String serviceTopologyTemplatePattern =
	// "/topologytemplate/topologytemplate/%s";
	//
	// public String serviceTopologySearchPattern =
	// "topology/topology/_search?q=%s";
	// public String serviceTopologyTemplateSearchPattern =
	// "topologytemplate/topologytemplate/_search?q=%s";
	//
	// public ArtifactTypeEnum getFileTypeByExtension(String fileName) {
	//
	// String fileExtension = null;
	// if (fileName.matches("(.*)\\.(.*)")) {
	// System.out.println(fileName.substring(fileName.lastIndexOf(".") + 1));
	// fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
	// }
	//
	// switch (fileExtension) {
	// case "sh":
	// return ArtifactTypeEnum.SHELL_SCRIPT;
	// case "png":
	// return ArtifactTypeEnum.ICON;
	// case "ppp":
	// return ArtifactTypeEnum.PUPPET;
	// case "yang":
	// return ArtifactTypeEnum.YANG;
	// default:
	// return ArtifactTypeEnum.UNKNOWN;
	// }
	//
	// }
	//
	// public ArrayList<String> getScriptList (List<UploadArtifactInfo>
	// artifactsList){
	//
	// ArrayList<String> scriptNameArray = new ArrayList<>();
	// if (artifactsList != null){
	// for (UploadArtifactInfo fileInArtifactsList : artifactsList){
	// String artifactFileName = fileInArtifactsList.getArtifactName();
	// ArtifactTypeEnum artifactFileType =
	// fileInArtifactsList.getArtifactType();
	// if (! artifactFileType.equals(ArtifactTypeEnum.ICON)){
	// scriptNameArray.add(artifactFileName);
	// }
	// continue;
	// }
	// return scriptNameArray;
	// }
	// return null;
	// }
	//
	//
	// public String getYamlFileLocation(File testResourcesPath) {
	// File[] files = testResourcesPath.listFiles();
	// if (files.length == 0){
	// return null;
	// }else{
	// for (int i = 0; i < files.length; i++){
	// if (files[i].isFile()){
	// return files[i].getAbsoluteFile().toString();
	// }
	// }
	// }
	// return null;
	// }
	//
	// public String readFileContentToString (String fileName) throws
	// IOException {
	//
	// Path path = Paths.get(fileName);
	// String stringFromFile = new String(Files.readAllBytes(path));
	// return stringFromFile;
	//
	//
	// }
	//
	@SuppressWarnings("unchecked")
	public ToscaNodeTypeInfo parseToscaNodeYaml(String fileContent) {

		ToscaNodeTypeInfo result = new ToscaNodeTypeInfo();
		Object templateVersion = null;
		Object templateName = null;

		if (fileContent != null) {
			Yaml yaml = new Yaml();

			Map<String, Object> yamlObject = (Map<String, Object>) yaml.load(fileContent);

			templateVersion = yamlObject.get(YamlConstants.TEMPLATE_VERSION);
			if (templateVersion != null) {
				result.setTemplateVersion(templateVersion.toString());
			}
			templateName = yamlObject.get(YamlConstants.TEMPLATE_NAME);
			if (templateName != null) {
				result.setTemplateName(templateName.toString());
			}
			Object nodeTypes = yamlObject.get(YamlConstants.NODE_TYPES);

			if (nodeTypes != null) {
				Map<String, Object> nodeTypesMap = (Map<String, Object>) nodeTypes;
				for (Entry<String, Object> entry : nodeTypesMap.entrySet()) {

					String nodeName = entry.getKey();
					if (nodeName != null) {
						result.setNodeName(nodeName);
					}

					break;

				}
			}

		}

		return result;
	}

	//
	//
	// public ArtifactsMetadata getArtifactsMetadata(String response){
	// ArtifactsMetadata artifactsMetadata = new ArtifactsMetadata();
	//
	// artifactsMetadata.setId(getJsonObjectValueByKey(response, "id"));
	// artifactsMetadata.setName(getJsonObjectValueByKey(response, "name"));
	// artifactsMetadata.setType(getJsonObjectValueByKey(response, "type"));
	//
	// artifactsMetadata.setCreator(getJsonObjectValueByKey(response,
	// "creator"));
	// artifactsMetadata.setCreationTime(getJsonObjectValueByKey(response,
	// "creationTime"));
	// artifactsMetadata.setLastUpdateTime(getJsonObjectValueByKey(response,
	// "lastUpdateTime"));
	// artifactsMetadata.setChecksum(getJsonObjectValueByKey(response,
	// "checksum"));
	// artifactsMetadata.setDescription(getJsonObjectValueByKey(response,
	// "description"));
	// artifactsMetadata.setLastUpdater(getJsonObjectValueByKey(response,
	// "lastUpdater"));
	//
	// return artifactsMetadata;
	// }
	//
	public static String getJsonObjectValueByKey(String metadata, String key) {
		JsonElement jelement = new JsonParser().parse(metadata);

		JsonObject jobject = jelement.getAsJsonObject();
		Object obj = jobject.get(key);
		if (obj == null) {
			return null;
		} else {
			String value;
			value = (String) jobject.get(key).getAsString();
			return value;
		}
	}

	public static Config getConfig() throws FileNotFoundException {
		Config config = Config.instance();
		return config;
	}

	// public void uploadNormativeTypes() throws IOException{
	// Config config = getConfig();
	// String[] normativeTypes = {"root", "compute", "blockStorage",
	// "softwareComponent", "DBMS", "database", "network", "objectStorage",
	// "webServer", "webApplication"};
	// for( String normativeType : normativeTypes ){
	// uploadComponent(config.getComponentsConfigDir()+File.separator+"normativeTypes"+File.separator+normativeType);
	// }
	//
	// }
	//
	// public void uploadApacheComponent() throws IOException{
	// Config config = getConfig();
	// uploadComponent(config.getComponentsConfigDir()+File.separator+"apache");
	// }
	//
	// public void uploadComponent(String componentDir) throws IOException{
	//
	// //*********************************************upload*************************************************************
	// Config config = getConfig();
	// ZipDirectory zipDirectory = new ZipDirectory();
	// System.out.println(config.getEsHost());
	//
	// List<UploadArtifactInfo> artifactsList = new
	// ArrayList<UploadArtifactInfo>();
	//
	//// read test resources and zip it as byte array
	// byte[] zippedAsByteArray = zipDirectory.zip(componentDir, artifactsList);
	//
	//// encode zipped directory using base64
	// String payload = Decoder.encode(zippedAsByteArray);
	//
	//// zip name build as testName with ".zip" extension
	// String payloadZipName = getPayloadZipName(componentDir);
	//
	//// build json
	// UploadResourceInfo resourceInfo = new UploadResourceInfo(payload,
	// payloadZipName, "description", "category/mycategory", null,
	// artifactsList);
	// String json = new Gson().toJson(resourceInfo);
	//
	//// calculate md5 on the content of json
	// String jsonMd5 =
	// org.apache.commons.codec.digest.DigestUtils.md5Hex(json);
	//
	//// encode the md5 to base64, sent as header in post http request
	// String encodedMd5 = Decoder.encode(jsonMd5.getBytes());
	//
	//// upload component to Elastic Search DB
	// String url = null;
	// HttpRequest http = new HttpRequest();
	//
	// url = String.format(Urls.UPLOAD_ZIP_URL, config.getCatalogFeHost(),
	// config.getCatalogFePort());
	//
	//// Prepare headers to post upload component request
	// HeaderData headerData = new HeaderData(encodedMd5, "application/json",
	// "att", "test", "testIvanovich", "RoyalSeal", "Far_Far_Away",
	// "getResourceArtifactListTest");
	//
	// MustHeaders headers = new MustHeaders(headerData);
	// System.out.println("headers:"+headers.getMap());
	//
	// RestResponse response = http.httpSendPost(url, json, headers.getMap());
	//
	// assertEquals("upload component failed with code " +
	// response.getErrorCode().intValue(),response.getErrorCode().intValue(),
	// 204);
	// }
	//
	// private String getPayloadZipName(String componentDir) {
	// String payloadName;
	// if( componentDir.contains( File.separator) ){
	// String delimiter = null;
	// if( File.separator.equals("\\")){
	// delimiter ="\\\\";
	// }
	// else{
	// delimiter = File.separator;
	// }
	// String[] split = componentDir.split(delimiter);
	// payloadName = split[split.length-1];
	// }
	// else{
	// payloadName = componentDir;
	// }
	// return payloadName+".zip";
	// }
	//
	//
	//
	// public List<UploadArtifactInfo> createArtifactsList(String srcDir) {
	//
	// List<UploadArtifactInfo> artifactsList = new
	// ArrayList<UploadArtifactInfo>();
	// File srcFile = new File(srcDir);
	// addFileToList(srcFile, artifactsList);
	//
	// return artifactsList;
	// }
	//
	// public void addFileToList(File srcFile, List<UploadArtifactInfo>
	// artifactsList) {
	//
	// File[] files = srcFile.listFiles();
	//
	// for (int i = 0; i < files.length; i++) {
	// // if the file is directory, use recursion
	// if (files[i].isDirectory()) {
	// addFileToList(files[i], artifactsList);
	// continue;
	// }
	//
	// String fileName = files[i].getName();
	// String artifactPath = fileName;
	//
	// if ( ! files[i].getName().matches("(.*)\\.y(?)ml($)")) {
	// UploadArtifactInfo uploadArtifactInfo = new UploadArtifactInfo();
	// uploadArtifactInfo.setArtifactName(files[i].getName());
	// String parent = files[i].getParent();
	//
	// if (parent != null) {
	// System.out.println(parent);
	// int lastSepartor = parent.lastIndexOf(File.separator);
	// if (lastSepartor > -1) {
	// String actualParent = parent.substring(lastSepartor + 1);
	// artifactPath = actualParent + "/" + artifactPath;
	// }
	// }
	//
	// uploadArtifactInfo.setArtifactPath(artifactPath);
	// uploadArtifactInfo.setArtifactType(getFileTypeByExtension(fileName));
	// uploadArtifactInfo.setArtifactDescription("description");
	// artifactsList.add(uploadArtifactInfo);
	//
	// System.out.println("artifact list: " + artifactsList);
	//
	// }
	//
	// }
	// }
	//
	//
	// public String buildArtifactListUrl (String nodesType, String
	// templateVersion, String artifactName) throws FileNotFoundException{
	// //"http://172.20.43.132/sdc2/v1/catalog/resources/tosca.nodes.Root/1.0.0.wd03-SNAPSHOT/artifacts/wxs_baseline_compare.sh"
	// Config config = getConfig();
	// return "\"http://" + config.getCatalogBeHost() + ":" +
	// config.getCatalogBePort() + "/sdc2/v1/catalog/resources/" +nodesType +
	// "/" + templateVersion + "/artifacts/" + artifactName +"\"";
	// }
	//
	//
	// public void addTopologyToES(String testFolder, String
	// serviceTopologyPattern) throws IOException{
	// Config config = getConfig();
	// String url = String.format(Urls.ES_URL, config.getEsHost(),
	// config.getEsPort()) + serviceTopologyPattern;
	// String sourceDir =
	// config.getResourceConfigDir()+File.separator+testFolder;
	// Path filePath = FileSystems.getDefault().getPath(sourceDir,
	// "topology.txt");
	// postFileContentsToUrl(url, filePath);
	// }
	//
	// public void addTopologyTemplateToES(String testFolder, String
	// serviceTopologyTemplatePattern) throws IOException{
	// Config config = getConfig();
	// String url = String.format(Urls.ES_URL, config.getEsHost(),
	// config.getEsPort()) + serviceTopologyTemplatePattern;
	// String sourceDir =
	// config.getResourceConfigDir()+File.separator+testFolder;
	// Path filePath = FileSystems.getDefault().getPath(sourceDir,
	// "topologyTemplate.txt");
	// postFileContentsToUrl(url, filePath);
	// }
	//
	//
	// public void postFileContentsToUrl(String url, Path filePath) throws
	// IOException {
	// HttpClientContext localContext = HttpClientContext.create();
	// CloseableHttpResponse response = null;
	//
	// byte[] fileContent = Files.readAllBytes(filePath);
	//
	// try(CloseableHttpClient httpClient = HttpClients.createDefault()){
	// HttpPost httpPost = new HttpPost(url);
	// StringEntity entity = new StringEntity(new String(fileContent) ,
	// ContentType.APPLICATION_JSON);
	// httpPost.setEntity(entity);
	// response = httpClient.execute(httpPost, localContext);
	//
	// }
	// finally{
	// response.close();
	// }
	//
	//
	// }
	//
	//
	//// public boolean isPatternInEsDb(String patternToSearch)throws
	// IOException{
	//// Config config = getConfig();
	//// String url = String.format(Urls.GET_SEARCH_DATA_FROM_ES,
	// config.getEsHost(), config.getEsPort(),patternToSearch);
	//// HttpRequest httpRequest = new HttpRequest();
	//// RestResponse restResponse = httpRequest.httpSendGet(url);
	//// if (restResponse.getErrorCode() == 200){
	//// return true;
	//// }
	//// if (restResponse.getErrorCode() == 404){
	//// return false;
	//// }
	////
	//// return false;
	//// }
	//
	// public static RestResponse deleteAllDataFromEs() throws IOException{
	// return deleteFromEsDbByPattern("_all");
	// }
	//

	//
	// public List<String> buildIdArrayListByTypesIndex (String index, String
	// types) throws IOException{
	//
	// Config config = getConfig();
	// HttpRequest http = new HttpRequest();
	// RestResponse getResponce =
	// http.httpSendGet(String.format(Urls.GET_ID_LIST_BY_INDEX_FROM_ES,
	// config.getEsHost(), config.getEsPort(), index, types), null);
	//
	// List <String> idArray = new ArrayList<String>();
	//
	// JsonElement jelement = new JsonParser().parse(getResponce.getResponse());
	// JsonObject jobject = jelement.getAsJsonObject();
	// JsonObject hitsObject = (JsonObject) jobject.get("hits");
	// JsonArray hitsArray = (JsonArray) hitsObject.get("hits");
	// for (int i = 0; i < hitsArray.size(); i ++){
	// JsonObject idObject = (JsonObject) hitsArray.get(i);
	// String id = idObject.get("_id").toString();
	// id = id.replace("\"", "");
	// idArray.add(id);
	// }
	//
	// return idArray;
	// }
	//
	// public List<String> buildCategoriesTagsListFromJson(String
	// categoriesTagsJson){
	//
	// ArrayList<String> categoriesTagsArray = new ArrayList<>();
	// JsonElement jelement = new JsonParser().parse(categoriesTagsJson);
	// JsonArray jArray = jelement.getAsJsonArray();
	// for (int i = 0; i < jArray.size(); i ++){
	// JsonObject categoriesTagsObject = (JsonObject) jArray.get(i);
	// String categories = categoriesTagsObject.get("name").toString();
	// categoriesTagsArray.add(categories);
	// }
	//
	// return categoriesTagsArray;
	// }
	//
	// public ArrayList <String> getCategoriesFromDb() throws Exception{
	//
	// ArrayList<String> categoriesFromDbArrayList = new ArrayList<>();
	// RestResponse restResponse = new RestResponse();
	// String contentTypeHeaderData = "application/json";
	// String acceptHeaderDate = "application/json";
	//
	// Map<String, String> headersMap = new HashMap<String,String>();
	// headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(),contentTypeHeaderData);
	// headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), acceptHeaderDate);
	//
	// HttpRequest httpRequest = new HttpRequest();
	// String url = String.format(Urls.QUERY_NEO4J,
	// Config.instance().getNeoHost(), Config.instance().getNeoPort());
	// String body = "{\"statements\" : [ { \"statement\" : \"MATCH
	// (category:category) return (category)\"} ]}";
	// restResponse = httpRequest.httpSendPostWithAuth(url, body, headersMap,
	// Config.instance().getNeoDBusername(),
	// Config.instance().getNeoDBpassword());
	//
	// if (restResponse.getResponse()==null){
	// return categoriesFromDbArrayList;
	// }else{
	// JsonElement jelement = new
	// JsonParser().parse(restResponse.getResponse());
	// JsonObject jobject = jelement.getAsJsonObject();
	// JsonArray resultsArray = (JsonArray) jobject.get("results");
	// JsonObject resObject = (JsonObject) resultsArray.get(0);
	// JsonArray dataArray = (JsonArray) resObject.get("data");
	// for (int i = 0; i < dataArray.size(); i ++){
	// JsonObject rowObject = (JsonObject) dataArray.get(i);
	// JsonArray rowArray = (JsonArray) rowObject.get("row");
	// JsonObject nameObject = (JsonObject) rowArray.get(0);
	// String name = nameObject.get("name").toString();
	//// name = name.replace("\"", "");
	// categoriesFromDbArrayList.add(name);
	// }
	//
	//
	// }
	//
	// return categoriesFromDbArrayList;
	// }
	//
	public static void compareArrayLists(List<String> actualArraylList, List<String> expectedArrayList,
			String message) {

		ArrayList<String> actual = new ArrayList<String>(actualArraylList);
		ArrayList<String> expected = new ArrayList<String>(expectedArrayList);
		// assertEquals(message + " count got by rest API not match to " +
		// message + " expected count", expected.size(),actual.size());
		expected.removeAll(actual);
		assertEquals(message + " content got by rest API not match to " + message + " actual content", 0,
				expected.size());
	}

	public static Object parseYamlConfig(String pattern) throws FileNotFoundException {

		Yaml yaml = new Yaml();
		Config config = getConfig();
		String configurationFile = config.getConfigurationFile();
		File file = new File(configurationFile);
		// File file = new
		// File("../catalog-be/src/main/resources/config/configuration.yaml");
		InputStream inputStream = new FileInputStream(file);
		Map<?, ?> map = (Map<?, ?>) yaml.load(inputStream);
		Object patternMap = (Object) map.get(pattern);

		return patternMap;
	}

	public static String getDepArtLabelFromConfig(ArtifactTypeEnum artifactTypeEnum) throws FileNotFoundException {

		@SuppressWarnings("unchecked")
		Map<String, Object> mapOfDepResArtTypesObjects = (Map<String, Object>) parseYamlConfig(
				"deploymentResourceArtifacts");
		for (Map.Entry<String, Object> iter : mapOfDepResArtTypesObjects.entrySet()) {
			if (iter.getValue().toString().contains(artifactTypeEnum.getType())) {
				return iter.getKey().toLowerCase();
			}
		}

		return "defaultLabelName";
	}

	
	public static String multipleChar(String ch, int repeat) {
		return StringUtils.repeat(ch, repeat);
	}
	
	public static List<String> getListOfDepResArtLabels(Boolean isLowerCase) throws FileNotFoundException {

		List<String> listOfResDepArtTypesFromConfig = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Map<String, Object> resourceDeploymentArtifacts = (Map<String, Object>) parseYamlConfig(
				"deploymentResourceArtifacts");
		if (resourceDeploymentArtifacts != null) {

			if (isLowerCase) {
				for (Map.Entry<String, Object> iter : resourceDeploymentArtifacts.entrySet()) {
					listOfResDepArtTypesFromConfig.add(iter.getKey().toLowerCase());
				}
			} else {

				for (Map.Entry<String, Object> iter : resourceDeploymentArtifacts.entrySet()) {
					listOfResDepArtTypesFromConfig.add(iter.getKey());
				}
			}
		}
		return listOfResDepArtTypesFromConfig;
	}

	public static List<String> getListOfToscaArtLabels(Boolean isLowerCase) throws FileNotFoundException {

		List<String> listOfToscaArtTypesFromConfig = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		Map<String, Object> toscaArtifacts = (Map<String, Object>) parseYamlConfig("toscaArtifacts");
		if (toscaArtifacts != null) {

			if (isLowerCase) {
				for (Map.Entry<String, Object> iter : toscaArtifacts.entrySet()) {
					listOfToscaArtTypesFromConfig.add(iter.getKey().toLowerCase());
				}
			} else {
				for (Map.Entry<String, Object> iter : toscaArtifacts.entrySet()) {
					listOfToscaArtTypesFromConfig.add(iter.getKey());
				}
			}
		}
		return listOfToscaArtTypesFromConfig;
	}

	//
	// public static List<String> getListOfResDepArtTypes() throws
	// FileNotFoundException {
	//
	// List<String> listOfResDepArtTypesFromConfig = new ArrayList<String>();
	// @SuppressWarnings("unchecked")
	// Map<String, Object> resourceDeploymentArtifacts = (Map<String, Object>)
	// parseYamlConfig("resourceDeploymentArtifacts");
	// for (Map.Entry<String, Object> iter :
	// resourceDeploymentArtifacts.entrySet()) {
	// listOfResDepArtTypesFromConfig.add(iter.getKey());
	// }
	//
	// return listOfResDepArtTypesFromConfig;
	// }
	//
	// public static List<String> getListOfDepResInstArtTypes() throws
	// FileNotFoundException {
	//
	// List<String> listOfResInstDepArtTypesFromConfig = new
	// ArrayList<String>();
	// @SuppressWarnings("unchecked")
	// Map<String, Object> resourceDeploymentArtifacts = (Map<String, Object>)
	// parseYamlConfig("deploymentResourceInstanceArtifacts");
	// for (Map.Entry<String, Object> iter :
	// resourceDeploymentArtifacts.entrySet()) {
	// listOfResInstDepArtTypesFromConfig.add(iter.getKey().toLowerCase());
	// }
	//
	// return listOfResInstDepArtTypesFromConfig;
	// }
	//
	public static List<String> getListOfResPlaceHoldersDepArtTypes() throws FileNotFoundException {
		List<String> listResDepArtTypesFromConfig = new ArrayList<String>();
		List<String> listOfResDepArtLabelsFromConfig = getListOfDepResArtLabels(false);
		assertNotNull("deployment artifact types list is null", listOfResDepArtLabelsFromConfig);
		Object parseYamlConfig = Utils.parseYamlConfig("deploymentResourceArtifacts");
		Map<String, Object> mapOfDepResArtTypesObjects = (Map<String, Object>) Utils
				.parseYamlConfig("deploymentResourceArtifacts");

		// assertNotNull("deployment artifact types list is null",
		// mapOfDepResArtTypesObjects);
		if (listOfResDepArtLabelsFromConfig != null) {
			for (String resDepArtType : listOfResDepArtLabelsFromConfig) {
				Object object = mapOfDepResArtTypesObjects.get(resDepArtType);
				if (object instanceof Map<?, ?>) {
					Map<String, Object> map = (Map<String, Object>) object;
					listResDepArtTypesFromConfig.add((String) map.get("type"));
				} else {
					assertTrue("return object does not instance of map", false);
				}
			}
		}
		return listResDepArtTypesFromConfig;
	}

	public static Long getEpochTimeFromUTC(String time) throws ParseException {
	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");
	    java.util.Date date = df.parse(time);
	    long epoch = date.getTime();
	    return epoch;
	}
}
