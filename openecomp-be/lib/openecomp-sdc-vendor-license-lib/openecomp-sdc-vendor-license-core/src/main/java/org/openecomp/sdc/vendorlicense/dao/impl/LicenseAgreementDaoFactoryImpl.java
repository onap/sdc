/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
package org.openecomp.sdc.vendorlicense.dao.impl;

import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.LicenseAgreementDaoZusammenImpl;

public class LicenseAgreementDaoFactoryImpl extends LicenseAgreementDaoFactory {

    private static final LicenseAgreementDao INSTANCE = new LicenseAgreementDaoZusammenImpl(ZusammenAdaptorFactory.getInstance().createInterface());

    @Override
    public LicenseAgreementDao createInterface() {
        return INSTANCE;
    }
}
