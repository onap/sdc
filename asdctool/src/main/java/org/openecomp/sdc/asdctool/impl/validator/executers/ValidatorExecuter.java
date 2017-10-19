package org.openecomp.sdc.asdctool.impl.validator.executers;

/**
 * Created by chaya on 7/3/2017.
 */
public interface ValidatorExecuter {

    boolean executeValidations();
    String getName();
}
