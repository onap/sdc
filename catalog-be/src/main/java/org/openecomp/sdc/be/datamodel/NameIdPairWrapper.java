package org.openecomp.sdc.be.datamodel;

import java.util.HashMap;

public class NameIdPairWrapper extends HashMap<String, Object> {
    public static final String ID = "id";
    public static final String DATA = "data";

    public NameIdPairWrapper() {
    }

    public NameIdPairWrapper(NameIdPair nameIdPair) {
        super();
        init(nameIdPair);
    }
    public void init(NameIdPair nameIdPair) {
        setId(nameIdPair.getId());
        setData(new NameIdPair(nameIdPair));
    }

    public String getId() {
        return get(ID).toString();
    }

    public void setId(String id) {
        super.put(ID, id);
    }

    public NameIdPair getData() {
        return (NameIdPair) get(DATA);
    }

    public void setData(NameIdPair data) {
        put(DATA, data);
    }

    public NameIdPair getNameIdPair(){
        return new NameIdPair(getData().getName(),getData().getId());
    }


}
