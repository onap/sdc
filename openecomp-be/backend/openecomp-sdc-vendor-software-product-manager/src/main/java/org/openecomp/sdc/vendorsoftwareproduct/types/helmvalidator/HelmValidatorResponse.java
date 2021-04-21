package org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public final class HelmValidatorResponse {

    private List<String> renderErrors;
    private List<String> lintWarning;
    private List<String> lintError;
    private String versionUsed;
    @SerializedName("valid")
    private Boolean isValid;
    @SerializedName("deployable")
    private Boolean isDeployable;
    private List<String> otherErrors;

    /**
     * Get renderErrors
     *
     * @return renderErrors
     **/
    public List<String> getRenderErrors() {
        return renderErrors != null ? renderErrors : Collections.emptyList();
    }

    /**
     * Get lintWarning
     *
     * @return lintWarning
     **/
    public List<String> getLintWarning() {
        return lintWarning != null ? lintWarning : Collections.emptyList();
    }

    /**
     * Get lintError
     *
     * @return lintError
     **/
    public List<String> getLintError() {
        return lintError != null ? lintError : Collections.emptyList();
    }

    /**
     * Get versionUsed
     *
     * @return versionUsed
     **/
    public String getVersionUsed() {
        return versionUsed;
    }

    /**
     * Get valid
     *
     * @return valid
     **/
    public Boolean isValid() {
        return isValid;
    }

    /**
     * Get deployable
     *
     * @return deployable
     **/
    public Boolean isDeployable() {
        return isDeployable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HelmValidatorResponse {\n");

        sb.append("    renderErrors: ").append(toIndentedString(renderErrors)).append("\n");
        sb.append("    lintWarning: ").append(toIndentedString(lintWarning)).append("\n");
        sb.append("    lintError: ").append(toIndentedString(lintError)).append("\n");
        sb.append("    versionUsed: ").append(toIndentedString(versionUsed)).append("\n");
        sb.append("    valid: ").append(toIndentedString(isValid)).append("\n");
        sb.append("    deployable: ").append(toIndentedString(isDeployable)).append("\n");
        sb.append("    other: ").append(toIndentedString(otherErrors)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
