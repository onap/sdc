/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.filters;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.onap.sdc.security.IPortalConfiguration;
import org.onap.sdc.security.PortalClient;

import java.security.InvalidParameterException;

public class PortalConfiguration implements IPortalConfiguration {
    private static final String PROPERTY_NOT_SET = "%s property value is not set in portal.properties file";
    private String portalUser;
    private String portalPassword;
    private String ecompPortalRestURL;
    private String portalAppName;
    private String uebKey;

    public PortalConfiguration() throws org.onap.portalsdk.core.onboarding.exception.CipherUtilException {
        this.portalUser = org.onap.portalsdk.core.onboarding.util.PortalApiProperties.getProperty(org.onap.sdc.security.PortalClient.PortalPropertiesEnum.USER.value());
        this.portalPassword = org.onap.portalsdk.core.onboarding.util.PortalApiProperties.getProperty(PortalClient.PortalPropertiesEnum.PASSWORD.value());
        this.portalAppName = org.onap.portalsdk.core.onboarding.util.PortalApiProperties.getProperty(PortalClient.PortalPropertiesEnum.APP_NAME.value());
        this.ecompPortalRestURL = org.onap.portalsdk.core.onboarding.util.PortalApiProperties.getProperty(org.onap.sdc.security.PortalClient.PortalPropertiesEnum.ECOMP_REST_URL.value());
        this.uebKey = org.onap.portalsdk.core.onboarding.util.PortalApiProperties.getProperty(org.onap.sdc.security.PortalClient.PortalPropertiesEnum.UEB_APP_KEY.value());
    }

    @VisibleForTesting
    String getPortalProperty(String key) {
        String value = PortalApiProperties.getProperty(key);
        if (StringUtils.isEmpty(value)) {
            throw new InvalidParameterException(String.format(PROPERTY_NOT_SET, key));
        }
        return value;
    }

    @Override
    public String getPortalApiPrefix() {
        return null;
    }

    @Override
    public long getMaxIdleTime() {
        return 0;
    }

    @Override
    public String getUserAttributeName() {
        return null;
    }

    @Override
    public boolean IsUseRestForFunctionalMenu() {
        return false;
    }

    @Override
    public String getPortalApiImplClass() {
        return null;
    }

    @Override
    public String getRoleAccessCentralized() {
        return null;
    }

    @Override
    public boolean getUebListenersEnable() {
        return false;
    }

    @Override
    public String getEcompRedirectUrl() {
        return null;
    }

    @Override
    public String getEcompRestUrl() {
        return ecompPortalRestURL;
    }

    @Override
    public String getPortalUser() {
        return portalUser;
    }

    @Override
    public String getPortalPass() {
        return portalPassword;
    }

    @Override
    public String getPortalAppName() {
        return portalAppName;
    }

    @Override
    public String getUebAppKey() {
        return uebKey;
    }

    @Override
    public String getAafNamespace() {
        return null;
    }

    @Override
    public String getAuthNamespace() {
        return null;
    }

    @Override
    public String getCspCookieName() {
        return null;
    }

    @Override
    public String getCspGateKeeperProdKey() {
        return null;
    }

    @Override
    public String getExtReqConnectionTimeout() {
        return null;
    }

    @Override
    public String getExtReqReadTimeout() {
        return null;
    }
}