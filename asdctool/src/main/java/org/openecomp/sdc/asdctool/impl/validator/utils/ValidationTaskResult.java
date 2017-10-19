package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;

/**
 * Created by chaya on 7/5/2017.
 */
public class ValidationTaskResult {
    public ValidationTaskResult(GraphVertex vertex, String name, String resultMessage, boolean isSuccessful) {
        this.vertexScanned = vertex;
        this.name = name;
        this.resultMessage = resultMessage;
        this.isSuccessful = isSuccessful;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    private String name;
    private String resultMessage;
    private boolean isSuccessful;
    private GraphVertex vertexScanned;
}
