package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition;

/**
 * @author SHIRIA
 * @since March 02, 2017.
 */
public enum UnifiedCompositionMode {
  SingleSubstitution,
  ScalingInstances,
  // todo - support B1 SingleSubstitution, but create diff types/services vm_type+<index>
  CatalogInstance,
  NestedSingleCompute;
}
