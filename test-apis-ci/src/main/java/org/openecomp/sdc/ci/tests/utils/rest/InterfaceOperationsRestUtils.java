package org.openecomp.sdc.ci.tests.utils.rest;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterfaceOperationsRestUtils extends BaseRestUtils {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(InterfaceOperationsRestUtils.class.getName());

    public static RestResponse addInterfaceOperations(Component component, Map<String, Object> interfaceDefinitionMap, User user) throws IOException {
        Config config = Utils.getConfig();
        String url = String.format(Urls.ADD_INTERFACE_OPERATIONS, config.getCatalogBeHost(), config.getCatalogBePort(), ComponentTypeEnum
            .findParamByType(component.getComponentType()), component.getUniqueId());
        String jsonBody = new Gson().toJson(interfaceDefinitionMap);
        return sendPost(url, jsonBody, user.getUserId(), acceptHeaderData);
    }

    public static RestResponse updateInterfaceOperations(Component component, Map<String, Object> interfaceDefinitionMap, User user) throws IOException {
        Config config = Utils.getConfig();
        String url = String.format(Urls.UPDATE_INTERFACE_OPERATIONS, config.getCatalogBeHost(), config.getCatalogBePort(), ComponentTypeEnum
            .findParamByType(component.getComponentType()), component.getUniqueId());
        String jsonBody = new Gson().toJson(interfaceDefinitionMap);
        return sendPut(url, jsonBody, user.getUserId(), acceptHeaderData);
    }

    public static RestResponse getInterfaceOperations(Component component, String interfaceId, String operationIds, User user) throws IOException {
        Config config = Utils.getConfig();
        String url = String.format(Urls.GET_INTERFACE_OPERATIONS, config.getCatalogBeHost(), config.getCatalogBePort(), ComponentTypeEnum
            .findParamByType(component.getComponentType()), component.getUniqueId(), interfaceId, operationIds);
        return sendGet(url, user.getUserId());
    }

    public static RestResponse deleteInterfaceOperations(Component component, String interfaceId, String operationIds, User user) throws IOException {
        Config config = Utils.getConfig();
        String url = String.format(Urls.DELETE_INTERFACE_OPERATIONS, config.getCatalogBeHost(), config.getCatalogBePort(), ComponentTypeEnum
            .findParamByType(component.getComponentType()), component.getUniqueId(), interfaceId, operationIds);
        return sendDelete(url, user.getUserId());
    }

}
