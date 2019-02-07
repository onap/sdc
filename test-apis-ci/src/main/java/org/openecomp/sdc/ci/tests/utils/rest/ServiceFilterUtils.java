package org.openecomp.sdc.ci.tests.utils.rest;

import com.google.gson.Gson;

import java.util.List;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ServiceFilterDetails;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceFilterUtils extends BaseRestUtils {

    private static Logger logger = LoggerFactory.getLogger(ServiceFilterUtils.class.getName());

    private static Gson gson = new Gson();

    public static RestResponse createServiceFilter(String externalServiceId, String proxyServiceId,
                                                   ServiceFilterDetails serviceFilterDetails,
                                                   User user) throws Exception{
        Config config = Config.instance();

        String url = String.format(Urls.CREATE_SERVICE_FILTER, config.getCatalogBeHost(), config.getCatalogBePort(),
                externalServiceId, proxyServiceId);

        return sendPost(url, gson.toJson(serviceFilterDetails), user.getUserId(), acceptHeaderData);
    }

    public static RestResponse updateServiceFilter(String externalServiceId, String proxyServiceId,
                                                   List<ServiceFilterDetails> serviceFilterDetailsList,
                                                   User user) throws Exception{
        Config config = Config.instance();

        String url = String.format(Urls.UPDATE_SERVICE_FILTER, config.getCatalogBeHost(), config.getCatalogBePort(),
                externalServiceId, proxyServiceId);

        return sendPut(url, gson.toJson(serviceFilterDetailsList), user.getUserId(), acceptHeaderData);
    }

    public static RestResponse deleteServiceFilter(String externalServiceId, String proxyServiceId,
                                                   int constraintIndex,
                                                   User user) throws Exception{
        Config config = Config.instance();

        String url = String.format(Urls.DELETE_SERVICE_FILTER, config.getCatalogBeHost(), config.getCatalogBePort(),
                externalServiceId, proxyServiceId, constraintIndex);

        return sendDelete(url, user.getUserId());
    }
}
