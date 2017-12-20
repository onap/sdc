package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.FileDataStructureDto;

public class MapFilesDataStructureToDto
    extends MappingBase<FilesDataStructure, FileDataStructureDto> {

  @Override
  public void doMapping(FilesDataStructure source, FileDataStructureDto target) {
    target.setModules(source.getModules());
    target.setArtifacts(source.getArtifacts());
    target.setNested(source.getNested());
    target.setUnassigned(source.getUnassigned());
  }
}
