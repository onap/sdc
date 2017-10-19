package org.openecomp.sdc.asdctool.impl.validator.utils;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.asdctool.impl.validator.executers.VfValidatorExecuter;

/**
 * Created by chaya on 7/4/2017.
 */
public enum ElementTypeEnum {

    VF ("vf", VfValidatorExecuter.class);
    //SERVICE("service", ServiceValidatorExecuter.class)

    private String elementType;
    private Class clazz;

    ElementTypeEnum(String elementType, Class clazz) {
       this. elementType = elementType;
       this.clazz = clazz;
    }

    public static ElementTypeEnum getByType(String elementType){
        for(ElementTypeEnum currType :ElementTypeEnum.values()){
            if(currType.getElementType().equals(elementType)){
                return currType;
            }
        }
        return null;
    }

    public static List<String> getAllTypes() {

        List<String> arrayList = new ArrayList<String>();

        for (ElementTypeEnum graphType : ElementTypeEnum.values()) {
            arrayList.add(graphType.getElementType());
        }
        return arrayList;
    }


    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
