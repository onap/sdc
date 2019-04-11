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

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

import java.util.*;

public class OnboardingUtils {

	/**
	 * excluded VNF file list
	 */
	public static List<String> exludeVnfList =
//		new ArrayList<>();
	Arrays.asList(
//		DUPLICATE_RESOURCE_ID_IN_DIFFERENT_FILES:
		"2017-376_vMOG_11_1.zip",
		"HeatCandidate_2017-09-20_15-06_66Name_2016-20-visbc1vf-v6.0-(VOIP).zip",
		"1 Apndns-1710-vf v3.0.zip",
		"1 AVPN_vRE_BV_volume-V2.zip",
		"1-Vf-zrdm5bpxtc02-092017-(MOBILITY)_v3.0.zip",
		"1-Mobility_vprobe_fe_11_2_1_vf_(MOBILITY)_v1.0.zip",
//		ORCHESTRATION_NOT_FOUND:
		"1-2017-491-4vshaken-HTTP-CM-vf-(VOIP)_v2.0.zip",
//		did not succeed to create package of new VSP  expected:<200> but was:<500>:
		"1-Riverbed-WANx1710VFv_v2.0.zip",
		"1-LMSP_v5-062317_v3.0.zip",
		"1-VF-Cisco-vCSR-1710_v2.0.zip",
		"1-mDNS-preload_1710-0914_v3.0.zip",
		"1-Firewall_170_Pala_Aloto_VF_v2.0.zip",
		"2-mDNS-preload-1710-0914_v3.1VF.zip",
		"1-2016-233_xsp_bfsa_nin2-vf-V1.0-VOIP-10-20.zip",
//		did not succeed to submit new VSP expected:<200> but was:<417>
		"1-VF_vEPDG_v4.0.zip",
		"1-VF-2017-491_9vShaken-F5-LB_v1.0.zip",
		"2-2016-73_Mow-AVPN-vpe-BV-L_v1.0.zip",
		"VF-2017389vTSBCDPA2-v4.0.zip",
		"1-vEPDG_V1.0(3).zip",
		"1-vHSS-EPC-RDM3-Lab-VF-0830_V3.0.zip",
		"2-2016-73_Mow-AVPN-vPE-BV-L_VF_V2.0_1027.zip",
		"1-ADIOD_base_vPE_BV_v9.0.zip",
		"Vhss-epc-rdm3-lab-vf-0921-v2.0-MOBILITY-10-20.zip",
		"1-Vhss-epc-rdm3-lab-vf-0921_VF00816v2.0-MOBILITY-10-20.zip",
		"1-VF-vUSP-CCF-DB-0620_v1.0base_vDB.zip",
		"1-Vusp_vhss-ims_cm-repo1_V1.zip",
		"1-base_vepdg_volume_v4.0.zip",
		"1-Vocg_1710-2017-509(2)_v1.0.zip",
		"1-2016-20-vISBC3VF_v3.0.zip",
		"Apndns-1710-vf-v3.0-10-20.zip",
		"1-VF-EFMC_DBE_Nin_v27.0.zip",
		"2-APNDNS_1710-VF_v4.0.zip",
		"1-Mow_adig_vpe_bv-V3.0.zip",
		"1-VF-2017-488_ADOID_vPE_v9.0.zip",
		"1-MOBT_Nimbus_3_Sprint-1.zip",
		"1-VF-2017-491_9vShaken-CM_v1.0.zip",
		"1-VF_zrdm5bpxtc02_092017_v2.0.zip",
//		Error: TOSCA yaml file %1 cannot be modeled to VF as it does not contain \u0027topology_template
		"1-VF-Checkpoint_vFW-1710_v1.0.zip"
	);

	/**
	 * additional files to exludeVnfList files for tosca parser tests
	 */
	protected static List<String> exludeVnfListForToscaParser = //new ArrayList<String>();
	Arrays.asList(
		"1-Vvig-062017-(MOBILITY)_v5.1.zip",
		"1-Mvm-sbc-1710-092017-(MOBILITY)_v7.0.zip",
		"1-2017-492-5vshaken-SIP-AS-vf-(VOIP)_v2.0.zip",
		"1-201712-488-adiod-vpe-(Layer-0-3)_v2.0.zip",
		"2017-502.zip",
		"1-2017-505-urlb-vhepe-(Layer-0-3)_v2.0.zip",
		"2017-376_vMOG_11_1.zip",
		"HeatCandidate_2017-09-22_01-30_60Name_Vdbe-vsp-15.1x49-d50.3-v3.0-(VOIP).zip",
		"HeatCandidate_2017-09-22_01-42_57Name_2017389vtsbc4vf-v10.0-(VOIP).zip",
		"HeatCandidate_2017-09-20_13-47_68Name_2017-492-5vshaken-SIP-AS-vf-v1.0-(VOIP)_10202017.zip",
		"1-2016-20-visbc3vf-(VOIP)_v2.1.zip",
		"1-2017-404_vUSP_vCCF_AIC3.0-(VOIP)_v6.0.zip",
		"1-2017389vtsbc4vf-(VOIP)_v11.0.zip"
	);
	
	public static String handleFilename(String heatFileName) {
		final String namePrefix = String.format("%sVF%s", ElementFactory.getResourcePrefix(), "Onboarded-");
		final String nameSuffix = "-" + getShortUUID();

		String subHeatFileName = heatFileName.substring(0, heatFileName.lastIndexOf("."));

		if ((namePrefix + subHeatFileName + nameSuffix).length() >= 50) {
			subHeatFileName = subHeatFileName.substring(0, 50 - namePrefix.length() - nameSuffix.length());
		}

		if (subHeatFileName.contains("(") || subHeatFileName.contains(")")) {
			subHeatFileName = subHeatFileName.replace("(", "-");
			subHeatFileName = subHeatFileName.replace(")", "-");
		}

		String vnfName = namePrefix + subHeatFileName + nameSuffix;
		return vnfName;
	}
	

	public static String getShortUUID() {
		return UUID.randomUUID().toString().split("-")[0];
	}

	private static RestResponse actionOnComponent(String vspid, String body, String onboardComponent, User user, String componentVersion) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.ACTION_ON_COMPONENT, config.getOnboardingBeHost(), config.getOnboardingBePort(), onboardComponent, vspid, componentVersion);
		String userId = user.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPut(url, body, headersMap);
		return response;
	}

	public static String getVspValidationConfiguration() throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.VSP_VALIDATION_CONFIGURATION, config.getOnboardingBeHost(), config.getOnboardingBePort());
		Map<String, String> headersMap = prepareHeadersMap("cs0008");

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendGet(url, headersMap);
		if(response.getErrorCode().intValue() == 200){
			return ResponseParser.getValueFromJsonResponse(response.getResponse(), "enabled");
		}
		throw new Exception("Cannot get configuration file");
		//return response;
	}

	public static String putVspValidationConfiguration(boolean value) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.VSP_VALIDATION_CONFIGURATION, config.getOnboardingBeHost(), config.getOnboardingBePort());
		Map<String, String> headersMap = prepareHeadersMap("cs0008");

		String body = String.format("{\"enabled\": \"%s\"}", value);

		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendPut(url, body, headersMap);
		if(response.getErrorCode().intValue() == 200){
			return ResponseParser.getValueFromJsonResponse(response.getResponse(), "enabled");
		}
		throw new Exception("Cannot set configuration file");
		//return response;
	}

	protected static Map<String, String> prepareHeadersMap(String userId) {
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.ACCEPT.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		return headersMap;
	}


	/**
	 * @return
	 * The method returns VNF names list from Files directory under sdc-vnfs repository
	 */
	public static List<String> getVnfNamesFileList() {
		String filepath = FileHandling.getVnfRepositoryPath();
		List<String> fileNamesFromFolder = FileHandling.getZipFileNamesFromFolder(filepath);
		//Please remove the hardcoded configuration ONAP Tal G!!!!!!
		fileNamesFromFolder.removeAll(exludeVnfList);
		//List<String> halfResourceListByDay = divideListByDayOfMonth(fileNamesFromFolder);
		//System.out.println(halfResourceListByDay.toString());
		return fileNamesFromFolder;
	}

	/**
	 * @param vnfNamesFileList
	 * @return divide List according to day of month, if day of month is even as get first half part of the List, else - second
	 */
	public static List<String> divideListByDayOfMonth(List<String> vnfNamesFileList){

		Calendar cal = Calendar.getInstance();
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)+1;
		int filesCount = vnfNamesFileList.size();
		if(dayOfMonth%2 == 0){
			return vnfNamesFileList.subList(0,filesCount/2);
		}else{
			return vnfNamesFileList.subList(filesCount/2, filesCount);
		}
	}
	
	/**
	 * @return
	 * The method returns VNF names list from Files directory under sdc-vnfs repository excluding zip files that known as failed in tosca parser
	 */
	public static List<String> getVnfNamesFileListExcludeToscaParserFailure() {
		List<String> fileNamesFromFolder = getVnfNamesFileList();
		fileNamesFromFolder.removeAll(exludeVnfListForToscaParser);
		return fileNamesFromFolder;
	}


	public static Object[][] filterObjectArrWithExcludedVnfs(Object[][] objectArr)
	{
		Object[][] filteredArObject = new Object[objectArr.length][];

		int index = 0;

		for (int i = 0; i < objectArr.length ; i++) {

			String vnfSourceFile = (String) objectArr[i][0];
			String vnfUpdateFile = (String) objectArr[i][1];

			if(!exludeVnfList.contains(vnfSourceFile) && !exludeVnfList.contains(vnfUpdateFile))
			{
				filteredArObject[index] = new Object[]{vnfSourceFile , vnfUpdateFile };
				index++;
			}
		}

		return filteredArObject;
	}
}

	