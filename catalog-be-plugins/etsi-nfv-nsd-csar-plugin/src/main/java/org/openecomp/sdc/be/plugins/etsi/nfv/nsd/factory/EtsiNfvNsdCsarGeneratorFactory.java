/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.factory;

import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.EtsiNfvNsdCsarGenerator;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.EtsiNfvNsdCsarGeneratorImpl;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.VnfDescriptorGenerator;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.EtsiVersion;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.NsDescriptorConfig;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.NsdCsarEtsiOption2Signer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class EtsiNfvNsdCsarGeneratorFactory {

    private final VnfDescriptorGenerator vnfDescriptorGenerator;
    private final NsDescriptorGeneratorFactory nsDescriptorGeneratorFactory;
    private final ArtifactCassandraDao artifactCassandraDao;
    private final ObjectProvider<EtsiNfvNsdCsarGeneratorImpl> etsiNfvNsdCsarGeneratorObjectProvider;
    private final NsdCsarEtsiOption2Signer nsdCsarEtsiOption2Signer;

    public EtsiNfvNsdCsarGeneratorFactory(final VnfDescriptorGenerator vnfDescriptorGenerator,
                                          final NsDescriptorGeneratorFactory nsDescriptorGeneratorFactory,
                                          final ArtifactCassandraDao artifactCassandraDao,
                                          final ObjectProvider<EtsiNfvNsdCsarGeneratorImpl> etsiNfvNsdCsarGeneratorObjectProvider,
                                          final NsdCsarEtsiOption2Signer nsdCsarEtsiOption2Signer) {
        this.vnfDescriptorGenerator = vnfDescriptorGenerator;
        this.nsDescriptorGeneratorFactory = nsDescriptorGeneratorFactory;
        this.artifactCassandraDao = artifactCassandraDao;
        this.etsiNfvNsdCsarGeneratorObjectProvider = etsiNfvNsdCsarGeneratorObjectProvider;
        this.nsdCsarEtsiOption2Signer = nsdCsarEtsiOption2Signer;
    }

    public EtsiNfvNsdCsarGenerator create(final EtsiVersion version) {
        final NsDescriptorConfig nsDescriptorConfig = new NsDescriptorConfig(version);
        return etsiNfvNsdCsarGeneratorObjectProvider
            .getObject(nsDescriptorConfig, vnfDescriptorGenerator, nsDescriptorGeneratorFactory, artifactCassandraDao
                , nsdCsarEtsiOption2Signer);
    }
}
