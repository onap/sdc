package org.openecomp.sdc.be.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResponseView {
    /**
     * The mixins that will be used when converting the resource's response into a specific view of that response.
     * A number of mixins can be declared, each of which corresponds to a different object.
     */
    Class<? extends Mixin>[] mixin();
}

