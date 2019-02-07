package org.openecomp.sdc.ci.tests.datatypes;

public class ServiceFilterDetails {
    private String servicePropertyName;
    private String constraintOperator;
    private String sourceType;
    private String sourceName;
    private Object value;

    public ServiceFilterDetails() {}

    public ServiceFilterDetails(String servicePropertyName, String constraintOperator, String sourceType, String sourceName,
                        Object value) {
        this.servicePropertyName = servicePropertyName;
        this.constraintOperator = constraintOperator;
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.value = value;
    }

    public String getServicePropertyName() {
        return servicePropertyName;
    }

    public void setServicePropertyName(String servicePropertyName) {
        this.servicePropertyName = servicePropertyName;
    }

    public String getConstraintOperator() {
        return constraintOperator;
    }

    public void setConstraintOperator(String constraintOperator) {
        this.constraintOperator = constraintOperator;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}
