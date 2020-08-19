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

package org.openecomp.sdc.ci.tests.utils.rest;

import com.google.gson.Gson;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RestUtils for inputs
 *
 * @author il0695
 */
public class InputsRestUtils extends BaseRestUtils {

	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(InputsRestUtils.class.getName());

	/**
	 * Add inputs to service
	 *
	 * @param component
	 * @param inputs
	 * @param userRole
	 * @return {@link org.openecomp.sdc.ci.tests.datatypes.http.RestResponse}
	 * @throws Exception
	 */
	public static RestResponse addInput(Component component, ComponentInstInputsMap inputs, UserRoleEnum userRole)
			throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.ADD_INPUTS, config.getCatalogBeHost(), config.getCatalogBePort(),
				ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUniqueId());
		String json = new Gson().toJson(inputs);
		return sendPost(url, json, userRole.getUserId(), acceptHeaderData);
	}

	/**
	 * Update inputs to service
	 *
	 * @param component
	 * @param data
	 * @param userRole
	 * @return {@link org.openecomp.sdc.ci.tests.datatypes.http.RestResponse}
	 * @throws Exception
	 */
	public static RestResponse updateInput(Component component, String data, UserRoleEnum userRole) throws Exception {
		Config config = Utils.getConfig();
		String url = String.format(Urls.UPDATE_INPUTS, config.getCatalogBeHost(), config.getCatalogBePort(),
				ComponentTypeEnum.findParamByType(component.getComponentType()), component.getUniqueId());
		return sendPost(url, data, userRole.getUserId(), acceptHeaderData);
	}

	/**
	 * Get all Component inputs
	 *
	 * @param component
	 * @return {@link org.openecomp.sdc.ci.tests.datatypes.http.RestResponse}
	 * @throws Exception
	 */
	public static RestResponse getComponentInputs(Component component) throws Exception {
		Config config = Utils.getConfig();
		//services/{componentId}/inputs
		String url = String.format(Urls.GET_COMPONENT_INPUTS, config.getCatalogBeHost(), config.getCatalogBePort(),
				component.getUniqueId());
		return sendGet(url, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
	}

	/**
	 * Get all inputs of component instance
	 *
	 * @param parentComponent
	 * @param componentInstance
	 * @return {@link org.openecomp.sdc.ci.tests.datatypes.http.RestResponse}
	 * @throws Exception
	 */
	public static RestResponse getComponentInstanceInputs(Component parentComponent,
														  ComponentInstance componentInstance) throws Exception {
		Config config = Utils.getConfig();
		//{componentType}/{componentId}/componentInstances/{instanceId}/{originComonentUid}/inputs
		String url =
				String.format(Urls.GET_COMPONENT_INSTANCE_INPUTS, config.getCatalogBeHost(), config.getCatalogBePort(),
						ComponentTypeEnum.findParamByType(parentComponent.getComponentType()),
						parentComponent.getUniqueId(), componentInstance.getUniqueId(),
						componentInstance.getComponentUid());
		return sendGet(url, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
	}

	/**
	 * Delete input from component
	 *
	 * @param parentComponent
	 * @param inputId
	 * @return {@link org.openecomp.sdc.ci.tests.datatypes.http.RestResponse}
	 * @throws Exception
	 */
	public static RestResponse deleteInputFromComponent(Component parentComponent, String inputId) throws Exception {
		return deleteInputFromComponent(ComponentTypeEnum.findParamByType(parentComponent.getComponentType()),
				parentComponent.getUniqueId(), inputId);
	}

	/**
	 * Delete input from component
	 *
	 * @param componentType
	 * @param componentId
	 * @param inputUniqueId
	 * @return {@link org.openecomp.sdc.ci.tests.datatypes.http.RestResponse}
	 * @throws Exception
	 */
	public static RestResponse deleteInputFromComponent(String componentType, String componentId, String inputUniqueId)
			throws Exception {
		Config config = Utils.getConfig();
		//{componentType}/{componentId}/delete/{inputId}/input
		String url = String.format(Urls.DELETE_INPUT_BY_ID, config.getCatalogBeHost(), config.getCatalogBePort(),
				componentType, componentId, inputUniqueId);
		return sendDelete(url, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER).getUserId());
	}

}
