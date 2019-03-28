package org.openecomp.sdc.be.dao.impl.heal;

@FunctionalInterface
public interface HealGraphDao<G,L>  {
    G performGraphReadHealing(G childVertex, L edgeLabelEnum);
}
