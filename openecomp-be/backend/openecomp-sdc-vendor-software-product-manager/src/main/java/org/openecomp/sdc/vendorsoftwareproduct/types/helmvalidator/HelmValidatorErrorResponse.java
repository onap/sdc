package org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator;

/**
 * ValidationErrorResponse
 */

public class HelmValidatorErrorResponse {

    private String message;

    /**
     * Get message
     *
     * @return message
     **/
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ValidationErrorResponse {\n");

        sb.append("    message: ").append(toIndentedString(message)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
