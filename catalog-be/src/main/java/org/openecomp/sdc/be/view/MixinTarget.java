package org.openecomp.sdc.be.view;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface MixinTarget {
    /**
     *
     * @return the class which is the target for the mixin. (i.e the class to be serialized into a json response)
     */
    Class<?> target();

}
