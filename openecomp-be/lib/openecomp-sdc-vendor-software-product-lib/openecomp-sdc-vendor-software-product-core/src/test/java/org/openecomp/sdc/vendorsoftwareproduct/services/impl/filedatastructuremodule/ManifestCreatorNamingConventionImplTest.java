package org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule;

//import org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule.ManifestCreatorNamingConventionImpl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ManifestCreatorNamingConventionImplTest extends ManifestCreatorNamingConventionImpl {

    private static final String ARTIFACT_1 = "cloudtech_k8s_charts.zip";
    private static final String ARTIFACT_2 = "cloudtech_azure_day0.zip";
    private static final String ARTIFACT_3 = "cloudtech_aws_configtemplate.zip";
    private static final String ARTIFACT_4 = "k8s_charts.zip";
    private static final String ARTIFACT_5 = "cloudtech_openstack_configtemplate.zip";

    @Test
    public void testIsCloudSpecificArtifact() {
        assertTrue(isCloudSpecificArtifact(ARTIFACT_1));
        assertTrue(isCloudSpecificArtifact(ARTIFACT_2));
        assertTrue(isCloudSpecificArtifact(ARTIFACT_3));
        assertFalse(isCloudSpecificArtifact(ARTIFACT_4));
        assertFalse(isCloudSpecificArtifact(ARTIFACT_5));
    }
}
