package org.openecomp.sdc.be.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
public class SerializedHashMap<T,R>  extends java.util.HashMap<T,R> {

    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }
}
