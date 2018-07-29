package org.openecomp.sdc.be.tosca.model;

import java.util.HashMap;
import java.util.Map;

public class ToscaInput extends ToscaProperty {
    private  Map<String,ToscaAnnotation> annotations;

    //copy constructor
    public ToscaInput(ToscaProperty toscaProperty){
        super(toscaProperty);
    }


    public Map<String, ToscaAnnotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Map<String, ToscaAnnotation> annotations) {
        this.annotations = annotations;
    }
    public void addAnnotation(String name, ToscaAnnotation annotaion){
        if ( annotations == null ){
            annotations = new HashMap<>();
        }
        annotations.put(name, annotaion);


    }
}
