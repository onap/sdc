package org.openecomp.sdc.vendorsoftwareproduct.utils;

import org.onap.config.api.Configuration;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorConfig;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorConfig.HelmValidationConfigBuilder;

public class HelmValidatorConfigReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmValidatorConfigReader.class);
    private static final String CONFIG_NAMESPACE = "helmvalidator";
    private static final String ERROR_MESSAGE = "Failed to read helm validator configuration key '{}', default value '{}' will be used";
    private final Configuration config;

    public HelmValidatorConfigReader(Configuration config) {
        this.config = config;
    }

    public HelmValidatorConfig getHelmValidatorConfig() {
        String version = readValue("hValidatorVersion", ".3.2.1");
        String validatorUrl = readValue("hValidatorUrl", "http://172.17.0.1:8082/validate");
        boolean enabled = readValue("hValidatorEnabled", false);
        boolean deployable = readValue("hValidatorDeployable", false);
        boolean lintable = readValue("hValidatorLintable", false);
        boolean strictLintable = readValue("hValidatorStrictLintable", false);

        HelmValidationConfigBuilder validationConfigBuilder = new HelmValidationConfigBuilder();
        validationConfigBuilder.setValidatorUrl(validatorUrl);
        validationConfigBuilder.setVersion(version);
        validationConfigBuilder.setEnabled(enabled);
        validationConfigBuilder.setDeployable(deployable);
        validationConfigBuilder.setLintable(lintable);
        validationConfigBuilder.setStrictLintable(strictLintable);
        return validationConfigBuilder.build();
    }


    private String readValue(String key, String defaultValue) {
        try {
            String value = config.getAsString(CONFIG_NAMESPACE, key);
            return (value == null) ? defaultValue : value;
        } catch (Exception e) {
            LOGGER.error(ERROR_MESSAGE, key, defaultValue, e);
            return defaultValue;
        }
    }

    private boolean readValue(String key, boolean defaultValue) {
        try {
            Boolean value = config.getAsBooleanValue(CONFIG_NAMESPACE, key);
            return (value == null) ? defaultValue : value;
        } catch (Exception e) {
            LOGGER.error(ERROR_MESSAGE, key, defaultValue, e);
            return defaultValue;
        }
    }

}
