package org.openecomp.sdc.ci.tests.execute.AmdocsComplexService;

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

import org.openecomp.sdc.ci.tests.utils.Utils;


public interface PathUrls {

    final static String SDC_HTTP_METHOD = Utils.getConfigHandleException() == null ? "http" : Utils.getConfigHandleException().getSdcHttpMethod();
    final String AMDOCS_HTTP_METHOD = SDC_HTTP_METHOD;

    // onboard
    final String CREATE_NEW_ITEM_VERSION = SDC_HTTP_METHOD + "://%s:%s/onboarding-api/v1.0/items/%s/versions/%s/";
    final String UPDATE_VENDOR_SOFTWARE_PRODUCT = SDC_HTTP_METHOD + "://%s:%s/onboarding-api/v1.0/vendor-software-products/%s/versions/%s";
    final String GET_SERVICE_PATHS = SDC_HTTP_METHOD + "://%s:%s/sdc1/feProxy/rest/v1/catalog/services/%s/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=forwardingPaths";
    final String SERVICE_PATH_LINK_MAP = SDC_HTTP_METHOD + "://%s:%s/sdc1/rest/v1/catalog/services/%s/linksMap";
    final String SERVICE_FORWARDING_PATHS = SDC_HTTP_METHOD + "://%s:%s/sdc1/rest/v1/catalog/services/%s/filteredDataByParams?include=componentInstancesRelations&include=componentInstances&include=forwardingPaths";
}
