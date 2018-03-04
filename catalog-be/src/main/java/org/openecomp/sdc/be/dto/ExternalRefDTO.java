package org.openecomp.sdc.be.dto;

/**
 * Created by yavivi on 18/02/2018.
 */
public class ExternalRefDTO {
    private String referenceUUID;

    public ExternalRefDTO(){
        //Empty constructor for serialization purposes
    }

    public ExternalRefDTO(String ref){
        this.referenceUUID = ref;
    }

    public String getReferenceUUID() {
        return referenceUUID;
    }

    public void setReferenceUUID(String referenceUUID) {
        this.referenceUUID = referenceUUID;
    }

    @Override
    public String toString() {
        return this.referenceUUID;
    }
}
