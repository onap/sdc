package org.openecomp.sdc.asdctool.impl.validator.utils;

/**
 * Created by chaya on 7/25/2017.
 */
public class VertexResult {

    boolean status;

    public VertexResult() {

    }

    public VertexResult(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getResult() {
        return String.valueOf(status);
    }

}
