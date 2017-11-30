package org.openecomp.sdc.ci.tests.utils.rest;

import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonRestUtils extends BaseRestUtils {

    private static Logger logger = LoggerFactory.getLogger(CommonRestUtils.class.getName());

    public static RestResponse getHealthCheck() throws Exception {

        Config config = Utils.getConfig();
        String url = String.format(Urls.GET_HEALTH_CHECK_VIA_PROXY, config.getCatalogFeHost(), config.getCatalogFePort());

        return sendGet(url, null);

    }
}
