package org.openecomp.sdc.be.view;

/**
 * A pojo which holds a mapping between a mixin source and its corresponding target class
 */
public class MixinSourceTarget {

    private Class<?> mixinSource;
    private Class<?> target;

    MixinSourceTarget(Class<?> mixinSource, Class<?> target) {
        this.mixinSource = mixinSource;
        this.target = target;
    }

    public Class<?> getMixinSource() {
        return mixinSource;
    }

    public Class<?> getTarget() {
        return target;
    }
}
