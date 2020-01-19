package org.openecomp.sdc.be.model;



import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class CatalogUpdateTimestamp  {

  

    @JsonProperty("previousUpdateTime")
    private long previousUpdateTime;
    @JsonProperty("currentUpdateTime")
    private long currentUpdateTime;

    public CatalogUpdateTimestamp() {
    }
    public CatalogUpdateTimestamp(long previousUpdateTime, long currentUpdateTime) {
        this.previousUpdateTime = previousUpdateTime;
        this.currentUpdateTime = currentUpdateTime;
    }

    public CatalogUpdateTimestamp(CatalogUpdateTimestamp catalogUpdateTimestamp) {
        this.previousUpdateTime = catalogUpdateTimestamp.getPreviousUpdateTime();
        this.currentUpdateTime = catalogUpdateTimestamp.getCurrentUpdateTime();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CatalogUpdateTimestamp){
            return (this.getCurrentUpdateTime() == ((CatalogUpdateTimestamp) obj).getCurrentUpdateTime()
                    && this.getPreviousUpdateTime() == ((CatalogUpdateTimestamp) obj).getPreviousUpdateTime())
                    || super.equals(obj);
        }
        return false;
    }
    
   
    public long getCurrentUpdateTime() {
        return currentUpdateTime;
    }

    
    public long getPreviousUpdateTime() {
        return previousUpdateTime;
    }
    
    
    public void setPreviousUpdateTime(long previousUpdateTime) {
        this.previousUpdateTime = previousUpdateTime;
    }

    public void setCurrentUpdateTime(long currentUpdateTime) {
        this.currentUpdateTime = currentUpdateTime;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "CatalogUpdateTimestamp [currentUpdateTime = " + currentUpdateTime + ", previousUpdateTime = " + previousUpdateTime + "]";
    }
    
    public    static CatalogUpdateTimestamp buildFromHttpResponse(String responseBody) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        TypeReference<CatalogUpdateTimestamp> typeRef = new TypeReference<CatalogUpdateTimestamp>() {};
        return objectMapper.readValue(responseBody, typeRef);
    }
	public static CatalogUpdateTimestamp buildDummyCatalogUpdateTimestamp() {
		long currentTimeMillis = System.currentTimeMillis();
		return new CatalogUpdateTimestamp(0L, currentTimeMillis);
	}

}
