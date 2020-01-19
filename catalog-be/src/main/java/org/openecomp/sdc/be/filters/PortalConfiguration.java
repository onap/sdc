package org.openecomp.sdc.be.filters;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.onap.sdc.security.IPortalConfiguration;
import org.onap.sdc.security.PortalClient;
import org.springframework.stereotype.Component;

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