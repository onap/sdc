package org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator;

public class HelmValidatorConfig {
    private final String validatorUrl;
    private final String version;
    private final boolean isEnabled;
    private final boolean isDeployable;
    private final boolean isLintable;
    private final boolean isStrictLintable;

    private HelmValidatorConfig(String validatorUrl, String version, boolean isEnabled, boolean isDeployable,
        boolean isLintable, boolean isStrictLintable) {
        this.validatorUrl = validatorUrl;
        this.version = version;
        this.isEnabled = isEnabled;
        this.isDeployable = isDeployable;
        this.isLintable = isLintable;
        this.isStrictLintable = isStrictLintable;
    }

    public String getVersion() {
        return version;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isDeployable() {
        return isDeployable;
    }

    public boolean isLintable() {
        return isLintable;
    }

    public boolean isStrictLintable() {
        return isStrictLintable;
    }

    public String getValidatorUrl() {
        return validatorUrl;
    }

    public static class HelmValidationConfigBuilder {

        private String validatorUrl;
        private String version;
        private boolean enabled;
        private boolean deployable;
        private boolean lintable;
        private boolean strictLintable;

        public HelmValidationConfigBuilder setVersion(String version) {
            this.version = version;
            return this;
        }

        public HelmValidationConfigBuilder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public HelmValidationConfigBuilder setDeployable(boolean deployable) {
            this.deployable = deployable;
            return this;
        }

        public HelmValidationConfigBuilder setLintable(boolean lintable) {
            this.lintable = lintable;
            return this;
        }

        public HelmValidationConfigBuilder setStrictLintable(boolean strictLintable) {
            this.strictLintable = strictLintable;
            return this;
        }

        public HelmValidationConfigBuilder setValidatorUrl(String validatorUrl) {
            this.validatorUrl = validatorUrl;
            return this;
        }

        public HelmValidatorConfig build() {
            return new HelmValidatorConfig(validatorUrl, version, enabled, deployable, lintable, strictLintable);
        }
    }

}
