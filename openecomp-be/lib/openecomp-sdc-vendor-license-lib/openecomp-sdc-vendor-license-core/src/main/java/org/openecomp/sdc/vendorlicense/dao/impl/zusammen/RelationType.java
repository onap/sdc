package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

public enum RelationType {
    LicenseAgreementToFeatureGroup,
    FeatureGroupToEntitlmentPool,
    FeatureGroupToLicenseKeyGroup,
    FeatureGroupToReferencingLicenseAgreement,
    EntitlmentPoolToReferencingFeatureGroup,
    LicenseKeyGroupToReferencingFeatureGroup
}
