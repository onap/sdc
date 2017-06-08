package org.openecomp.sdc.translator.services.heattotosca.buildconsolidationdata;

public class TestConstants{

  public static final String MAIN_SERVICE_TEMPLATE = "MainServiceTemplate.yaml";

  public static final String TEST_GROUP_POSITIVE = "TestGroupsPositive";
  public static final String TEST_GROUP_NEGATIVE = "TestGroupsNegative";

  public static final String TEST_VOLUME_POSITIVE = "TestVolumePositive";
  public static final String TEST_VOLUME_NEGATIVE = "TestVolumeNegative";

  public static final String TEST_PORT_POSITIVE = "TestPortPositive";
  public static final String TEST_PORT_NEGATIVE = "TestPortNegative";

  public static final String TEST_CONNECTIVITY_POSITIVE = "TestConnectivityPositive";
  public static final String TEST_CONNECTIVITY_NEGATIVE = "TestConnectivityNegative";

  public static final String TEST_DEPENDS_ON_NODES_CONNECTED_IN = "TestDependsOnNodesConnectedIn";
  public static final String TEST_DEPENDS_ON_NODES_CONNECTED_OUT = "TestDependsOnNodesConnectedOut";
  public static final String TEST_DEPENDS_ON_NODES_CONNECTED_IN_AND_OUT =
      "TestDependsOnNodesConnectedInAndOut";
  public static final String TEST_DEPENDS_ON_NO_DEPENDENCY =
      "TestDependsOnNoDependency";
  public static final String TEST_DEPENDS_ON_INVALID_DEPENDENCY_CANDIDATE =
      "TestDependsOnInvalidDependencyCandidate";
  public static final String TEST_GET_ATTR_FOR_NONE_TO_PORT_OR_COMPUTE =
      "testTranslateGetAttForNoneToPortOrCompute";
  public static final String TEST_GET_ATTR_FOR_ONLY_RESOURCE_NAME =
      "testTranslateGetAttOnlyResourceName";
  public static final String TEST_GET_ATTR_FOR_MORE_THAN_ONE_ATTR_IN_ATTR_LIST =
      "testTranslateGetAtt";
  public static final String TEST_GET_ATTR_FOR_NOT_SUPPORTED_ATTR_IN_ATTR_LIST =
      "testTranslateGetAttUnsupportedAttr";
  public static final String TEST_IGNORE_GET_ATTR_FROM_OUTPUT =
      "testTranslateGetAttUnsupportedResource";
  public static final String TEST_OUTPUT_GET_ATTR =
      "testTranslateGetAttDynamicParam";
  public static final String TEST_DEPENDS_ON_MULTIPLE_COMPUTE = "TestDependsOnMultipleComputes";
  public static final String TEST_DEPENDS_ON_NODE_TEMPLATE_TRANSLATION_ORDER_INVARIANCE =
      "TestDependsOnNodeTemplateTranslationOrderInvariance";

  public static final String TEST_SINGLE_NESTED_RESOURCE = "TestTranslateHeatSingleNestedResource";
  public static final String TEST_MULTIPLE_NESTED_RESOURCE = "TestTranslateHeatNestedMultiBase";
  public static final String TEST_MULTIPLE_MULTI_LEVEL_NESTED_RESOURCE =
      "TestTranslateHeatNestedRecursiveMultiLevel";

  public static final String TEST_SECURITY_RULE_PORT_NESTED_CONNECTION =
      "TestSecurityRuleToPortNestedConnection";
  public static final String TEST_SECURITY_RULE_PORT_NESTED_SHARED_PORT =
      "TestSecurityRuleToPortSharedPortNestedConnection";
  public static final String TEST_SECURITY_RULE_PORT_MULTI_LEVEL_NESTED_CONNECTION =
      "TestSecurityRuleToPortMultiLevelNestedConnection";
  public static final String TEST_SECURITY_RULE_PORT_MULTI_LEVEL_NESTED_SHARED_PORT =
      "TestSecurityRuleToPortSharedPortMultiLevelNestedConnection";
}
