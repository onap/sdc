package org.openecomp.sdc.asdctool.configuration.mocks.es;

import fj.data.Either;
import org.openecomp.sdc.be.dao.api.ICatalogDAO;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;
import org.openecomp.sdc.be.resources.data.ESArtifactData;

import java.util.List;

public class ESCatalogDAOMock implements ICatalogDAO {

    @Override
    public void addToIndicesMap(String typeName, String indexName) {

    }

    @Override
    public void writeArtifact(ESArtifactData artifactData) {

    }

    @Override
    public Either<ESArtifactData, ResourceUploadStatus> getArtifact(String id) {
        return null;
    }

    @Override
    public Either<List<ESArtifactData>, ResourceUploadStatus> getArtifacts(String[] ids) {
        return null;
    }

    @Override
    public void deleteArtifact(String id) {

    }

    @Override
    public void deleteAllArtifacts() {

    }
}
