package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.core.enrichment.factory.EnrichmentManagerFactory;
import org.openecomp.core.factory.impl.AbstractFactoryBase;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.mock.CandidateServiceFactoryMock;
import org.openecomp.sdc.vendorsoftwareproduct.impl.mock.NoSqlDbFactoryMock;
import org.openecomp.sdc.vendorsoftwareproduct.impl.mock.PackageInfoDaoFactoryImplMock;
import org.openecomp.sdc.vendorsoftwareproduct.impl.mock.VendorSoftwareProductDaoFactoryMock;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process.OrchestrationTemplateProcessCsarHandler;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertFalse;
public class OrchestrationTemplateProcessCsarHandlerTest {


    @Before
    public void insertMocks(){
        EnrichmentManagerFactory.getInstance();
        AbstractFactoryBase.registerFactory(NoSqlDbFactory.class, NoSqlDbFactoryMock.class);
        AbstractFactoryBase.registerFactory(PackageInfoDaoFactory.class, PackageInfoDaoFactoryImplMock.class);
        AbstractFactoryBase.registerFactory(PackageInfoDaoFactory.class, PackageInfoDaoFactoryImplMock.class);
        AbstractFactoryBase.registerFactory(VendorSoftwareProductDaoFactory.class, VendorSoftwareProductDaoFactoryMock.class);
        AbstractFactoryBase.registerFactory(CandidateServiceFactory.class, CandidateServiceFactoryMock.class);
    }



    @Test
    public void validateErrorHandling(){
        VspDetails vspDetails = new VspDetails("dummyId", new Version(1, 0));
        OrchestrationTemplateCandidateData orchestrationTemplateCandidateData = new OrchestrationTemplateCandidateData(ByteBuffer.wrap("".getBytes()),"");
        OrchestrationTemplateProcessCsarHandler handler = new OrchestrationTemplateProcessCsarHandler();
        OrchestrationTemplateActionResponse response = handler.process(vspDetails, orchestrationTemplateCandidateData, "007");
        assertFalse(response.getErrors().isEmpty());
    }
}
