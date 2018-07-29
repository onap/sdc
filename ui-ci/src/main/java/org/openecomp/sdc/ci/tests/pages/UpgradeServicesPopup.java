package org.openecomp.sdc.ci.tests.pages;

public class UpgradeServicesPopup {
    static boolean isUpgradePopupShown;

    public static boolean isUpgradePopupShown() {
        return isUpgradePopupShown;
    }

    public static void setUpgradePopupShown(boolean upgradePopupShown) {
        isUpgradePopupShown = upgradePopupShown;
    }
}
