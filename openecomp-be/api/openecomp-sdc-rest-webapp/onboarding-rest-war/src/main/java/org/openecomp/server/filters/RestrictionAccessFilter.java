package org.openecomp.server.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.sdc.security.ISessionValidationFilterConfiguration;
import org.onap.sdc.security.filters.SessionValidationFilter;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.config.CookieConfig;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.EntryNotConfiguredException;

import javax.servlet.http.Cookie;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RestrictionAccessFilter extends SessionValidationFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionAccessFilter.class);
    private static final String CONFIG_FILE_PROPERTY = "onboardingConfiguration.yaml";
    private static final String CONFIG_SECTION = "authCookie";

    private static class Configuration implements ISessionValidationFilterConfiguration {
        private static Configuration instance;
        private String securityKey;
        private long maxSessionTimeOut;
        private long sessionIdleTimeOut;
        private String cookieName;
        private String redirectURL;
        private List<String> excludedUrls;


        private Configuration() {
            try {

                String file = Objects.requireNonNull(System.getProperty(CONFIG_FILE_PROPERTY),
                        "Config file location must be specified via system property " + CONFIG_FILE_PROPERTY);

                Object config = getAuthenticationConfiguration(file);
                ObjectMapper mapper = new ObjectMapper();
                CookieConfig cookieConfig = mapper.convertValue(config, CookieConfig.class);
                this.securityKey = cookieConfig.getSecurityKey();
                this.maxSessionTimeOut = cookieConfig.getMaxSessionTimeOut();
                this.sessionIdleTimeOut = cookieConfig.getSessionIdleTimeOut();
                this.cookieName = cookieConfig.getCookieName();
                this.redirectURL = cookieConfig.getRedirectURL();
                this.excludedUrls = cookieConfig.getOnboardingExcludedUrls();

            } catch (Exception e) {
                LOGGER.warn("Failed to load configuration. ", e);
            }

        }

        public static Configuration getInstance() {
            if (instance == null) {
                instance = new Configuration();
            }
            return instance;
        }

        private static Object getAuthenticationConfiguration(String file) throws IOException {

            Map<?, ?> configuration = Objects.requireNonNull(readConfigurationFile(file), "Configuration cannot be empty");
            Object authenticationConfig = configuration.get(CONFIG_SECTION);
            if (authenticationConfig == null) {
                throw new EntryNotConfiguredException(CONFIG_SECTION + " section");
            }

            return authenticationConfig;
        }

        private static Map<?, ?> readConfigurationFile(String file) throws IOException {

            try (InputStream fileInput = new FileInputStream(file)) {
                YamlUtil yamlUtil = new YamlUtil();
                return yamlUtil.yamlToMap(fileInput);
            }
        }

        @Override
        public String getSecurityKey() {
            return securityKey;
        }

        @Override
        public long getMaxSessionTimeOut() {
            return maxSessionTimeOut;
        }

        @Override
        public long getSessionIdleTimeOut() {
            return sessionIdleTimeOut;
        }

        @Override
        public String getCookieName() {
            return cookieName;
        }

        @Override
        public String getRedirectURL() {
            return redirectURL;
        }

        @Override
        public List<String> getExcludedUrls() {
            return excludedUrls;
        }
    }

    @Override
    public ISessionValidationFilterConfiguration getFilterConfiguration() {
        return Configuration.getInstance();
    }

    @Override
    protected Cookie addRoleToCookie(Cookie cookie) {
        return cookie;
    }

    @Override
    protected boolean isRoleValid(Cookie cookie) {
        return true;
    }
}
