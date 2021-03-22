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
package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManager;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;

public class ImageManagerFactoryImpl extends ImageManagerFactory {

    private static final ImageManager INSTANCE = new ImageManagerImpl(VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
        ImageDaoFactory.getInstance().createInterface(), CompositionEntityDataManagerFactory.getInstance().createInterface());

    @Override
    public ImageManager createInterface() {
        return INSTANCE;
    }
}
