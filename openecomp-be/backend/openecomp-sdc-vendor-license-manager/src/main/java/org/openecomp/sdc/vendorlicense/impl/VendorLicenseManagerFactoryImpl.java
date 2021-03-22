/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.vendorlicense.impl;

import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.LimitDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDaoFactory;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;

/**
 * Created by ayalaben on 8/3/2017
 */
public class VendorLicenseManagerFactoryImpl extends VendorLicenseManagerFactory {

    private static final VendorLicenseManager INSTANCE = new VendorLicenseManagerImpl(VendorLicenseFacadeFactory.getInstance().createInterface(),
        VendorLicenseModelDaoFactory.getInstance().createInterface(), LicenseAgreementDaoFactory.getInstance().createInterface(),
        FeatureGroupDaoFactory.getInstance().createInterface(), EntitlementPoolDaoFactory.getInstance().createInterface(),
        LicenseKeyGroupDaoFactory.getInstance().createInterface(), LimitDaoFactory.getInstance().createInterface(),
        UniqueValueDaoFactory.getInstance().createInterface());

    @Override
    public VendorLicenseManager createInterface() {
        return INSTANCE;
    }
}
