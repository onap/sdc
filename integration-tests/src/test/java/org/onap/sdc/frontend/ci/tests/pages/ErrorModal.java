package org.onap.sdc.frontend.ci.tests.pages;

import org.onap.sdc.frontend.ci.tests.datatypes.DataTestIdEnum;
import org.onap.sdc.frontend.ci.tests.utilities.GeneralUIUtils;

public class ErrorModal extends GeneralPageElements {
    public static boolean isModalOpen(){
        return GeneralUIUtils.isWebElementExistByTestId(DataTestIdEnum.ErrorModalEnum.OK.getValue());
    }
}
