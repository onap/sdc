package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.openecomp.sdc.asdctool.impl.validator.utils.ElementTypeEnum;
import org.openecomp.sdc.be.model.Component;

import java.util.List;

/**
 * Created by chaya on 7/3/2017.
 */
public interface ValidatorExecuter {

    boolean executeValidations();
    String getName();
}
