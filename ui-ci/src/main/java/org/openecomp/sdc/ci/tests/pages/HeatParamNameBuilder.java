package org.openecomp.sdc.ci.tests.pages;

import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum;

public class HeatParamNameBuilder {
    private static final String CURRENT_VAL = DataTestIdEnum.EnvParameterView.ENV_CURRENT_VALUE.getValue();
    private static final String DEFAULT_VAL = DataTestIdEnum.EnvParameterView.ENV_DEFAULT_VALUE.getValue();


    public static String buildCurrentHeatParamValue(String paramName){
        return new StringBuilder().append(CURRENT_VAL).append(paramName).toString();
    }

    public static String buildDefaultHeatParamValue(String paramName){
        return new StringBuilder().append(DEFAULT_VAL).append(paramName).toString();
    }


}
