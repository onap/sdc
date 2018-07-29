package org.openecomp.sdc.be.components.impl.lock;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional
@Inherited
public @interface LockingTransactional {

}
