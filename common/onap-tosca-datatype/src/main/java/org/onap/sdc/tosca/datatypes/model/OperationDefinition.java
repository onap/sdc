package org.onap.sdc.tosca.datatypes.model;

import java.util.Objects;

public class OperationDefinition implements Cloneable {

    protected String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperationDefinition)) {
            return false;
        }
        OperationDefinition that = (OperationDefinition) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {

        return Objects.hash(description);
    }

    @Override
    public OperationDefinition clone() {
        OperationDefinition operationDefinition = new OperationDefinition();
        operationDefinition.setDescription(this.getDescription());
        return operationDefinition;
    }
}
