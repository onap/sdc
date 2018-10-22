package org.openecomp.core.enrichment.types;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.Getter;
import lombok.Setter;


public class ComponentProcessInfo {

    @Setter
    @Getter
    private String name;

    @Setter
    private byte[] content;


    public InputStream getContent() {
        return new ByteArrayInputStream(this.content);
    }


}
