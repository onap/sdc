package com.att.sdc.tosca.datatypes;

import org.openecomp.config.api.Configuration;
import org.openecomp.config.api.ConfigurationManager;
import org.openecomp.sdc.tosca.services.ConfigConstants;

/**
 * Created by TALIO on 5/17/2017.
 */
public class AttToscaPolicyType {
  private static Configuration config = ConfigurationManager.lookup();

  public static final String POLICY_TYPE_PREFIX =
      config.getAsString(ConfigConstants.NAMESPACE, ConfigConstants.PREFIX_POLICY_TYPE);


  public static final String PLACEMENT_VALET_AFFINITY = POLICY_TYPE_PREFIX + "placement.valet" +
      ".Affinity";
  public static final String PLACEMENT_VALET_EXCLUSIVITY =
      POLICY_TYPE_PREFIX + "placement.valet.Exclusivity";
  public static final String PLACEMENT_VALET_DIVERSITY = POLICY_TYPE_PREFIX + "placement.valet" +
      ".Diversity";
}
