package org.openecomp.sdc.be.view;

import java.lang.annotation.*;

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
