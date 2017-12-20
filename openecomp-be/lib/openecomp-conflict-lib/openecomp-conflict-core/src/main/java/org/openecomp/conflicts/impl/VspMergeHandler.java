package org.openecomp.conflicts.impl;

import org.openecomp.conflicts.ItemMergeHandler;
import org.openecomp.conflicts.dao.ConflictsDao;
import org.openecomp.conflicts.types.Conflict;
import org.openecomp.conflicts.types.ConflictInfo;
import org.openecomp.conflicts.types.ConflictResolution;
import org.openecomp.conflicts.types.ItemVersionConflict;
import org.openecomp.conflicts.types.Resolution;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeDao;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openecomp.sdc.datatypes.model.ElementType.NetworkPackage;
import static org.openecomp.sdc.datatypes.model.ElementType.OrchestrationTemplate;
import static org.openecomp.sdc.datatypes.model.ElementType.OrchestrationTemplateCandidate;
import static org.openecomp.sdc.datatypes.model.ElementType.OrchestrationTemplateCandidateContent;
import static org.openecomp.sdc.datatypes.model.ElementType.OrchestrationTemplateValidationData;
import static org.openecomp.sdc.datatypes.model.ElementType.VspModel;

public class VspMergeHandler implements ItemMergeHandler {

  private static final String VSP_MODEL_CONFLICT_ID = "vspModelConflictId";
  private static final String ELEMENT_CONFLICT_NOT_EXIST_ERR_ID = "ELEMENT_CONFLICT_NOT_EXIST";
  private static final String ELEMENT_CONFLICT_NOT_EXISTS_MSG =
      "Item Id %s, version Id %s, element conflict with Id %s does not exists.";
  private static final Set<ElementType> FILTERED_OUT_TYPES = Stream
      .of(OrchestrationTemplateCandidateContent, OrchestrationTemplateValidationData)
      .collect(Collectors.toSet());
  private static final Map<ElementType, Set<ElementType>> ELEMENT_TYPE_TO_CONFLICT_DEPENDANT_TYPES =
      new HashMap<>();

  static {
    ELEMENT_TYPE_TO_CONFLICT_DEPENDANT_TYPES.put(OrchestrationTemplateCandidate,
        Collections.singleton(OrchestrationTemplateCandidateContent));
    ELEMENT_TYPE_TO_CONFLICT_DEPENDANT_TYPES.put(OrchestrationTemplate,
        Collections.singleton(OrchestrationTemplateValidationData));
  }

  private ConflictsDao conflictsDao;
  private VspMergeDao vspMergeDao;

  public VspMergeHandler(ConflictsDao conflictsDao, VspMergeDao vspMergeDao) {
    this.conflictsDao = conflictsDao;
    this.vspMergeDao = vspMergeDao;
  }

  @Override
  public boolean isConflicted(String itemId, Version version) {
    return vspMergeDao.isVspModelConflicted(itemId, version);
  }

  @Override
  public void finalizeMerge(String itemId, Version version) {
    if (!conflictsDao.isConflicted(itemId, version)) {
      vspMergeDao.applyVspModelConflictResolution(itemId, version);
    }
  }

  @Override
  public void postListConflicts(String itemId, Version version, ItemVersionConflict conflicts) {
    List<ConflictInfo> elementConflicts = new ArrayList<>();

    boolean vspModelConflicted = false;
    for (ConflictInfo elementConflict : conflicts.getElementConflicts()) {
      if (elementConflict.getType() == VspModel) {
        elementConflicts.add(
            new ConflictInfo(elementConflict.getId(), NetworkPackage, NetworkPackage.name()));
        vspModelConflicted = true;
        continue;
      }
      if (!FILTERED_OUT_TYPES.contains(elementConflict.getType())) {
        elementConflicts.add(elementConflict);
      }
    }

    if (!vspModelConflicted && vspMergeDao.isVspModelConflicted(itemId, version)) {
      elementConflicts
          .add(new ConflictInfo(VSP_MODEL_CONFLICT_ID, NetworkPackage, NetworkPackage.name()));
    }

    conflicts.setElementConflicts(elementConflicts);
  }

  @Override
  public Optional<Conflict> getConflict(String itemId, Version version, String conflictId) {
    return VSP_MODEL_CONFLICT_ID.equals(conflictId)
        ? Optional.of(buildVspModelConflict(conflictId))
        : Optional.empty();
  }

  @Override
  public void postGetConflict(String itemId, Version version, Conflict conflict) {
    if (conflict.getType() == VspModel) {
      Conflict vspModelConflict = buildVspModelConflict(null);
      conflict.setType(vspModelConflict.getType());
      conflict.setName(vspModelConflict.getName());
      conflict.setYours(vspModelConflict.getYours());
      conflict.setTheirs(vspModelConflict.getTheirs());
    }
  }

  @Override
  public void preResolveConflict(String itemId, Version version, String conflictId,
                                 ConflictResolution resolution) {
    if (VSP_MODEL_CONFLICT_ID.equals(conflictId)) {
      return;
    }
    resolveDependantConflicts(itemId, version, conflictId, resolution);
  }

  @Override
  public boolean resolveConflict(String itemId, Version version, String conflictId,
                                 ConflictResolution resolution) {
    if (VSP_MODEL_CONFLICT_ID.equals(conflictId)) {
      vspMergeDao.updateVspModelConflictResolution(itemId, version,
          com.amdocs.zusammen.datatypes.item.Resolution.valueOf(resolution.getResolution().name()));
      return true;
    }
    Conflict conflict = conflictsDao.getConflict(itemId, version, conflictId);
    if (conflict == null) {
      throw getConflictNotExistException(itemId, version, conflictId);
    }
    if (conflict.getType() == VspModel) {
      vspMergeDao.updateVspModelConflictResolution(itemId, version,
          com.amdocs.zusammen.datatypes.item.Resolution.valueOf(resolution.getResolution().name()));

      conflictsDao.resolveConflict(itemId, version, conflictId, new ConflictResolution(
          conflict.getTheirs() == null ? Resolution.YOURS : Resolution.THEIRS));
      return true;
    }
    return false;
  }

  private void resolveDependantConflicts(String itemId, Version version, String conflictId,
                                         ConflictResolution resolution) {
    ItemVersionConflict conflicts = conflictsDao.getConflict(itemId, version);

    Set<ElementType> conflictDependantTypes =
        ELEMENT_TYPE_TO_CONFLICT_DEPENDANT_TYPES
            .get(findConflictById(conflicts, conflictId).getType());

    if (conflictDependantTypes == null) {
      return;
    }

    findConflictsByTypes(conflicts, conflictDependantTypes)
        .forEach(dependantConflict ->
            conflictsDao.resolveConflict(itemId, version, dependantConflict.getId(), resolution));
  }

  private ConflictInfo findConflictById(ItemVersionConflict versionConflicts,
                                        String conflictId) {
    return versionConflicts.getElementConflicts().stream()
        .filter(elementConflict -> conflictId.equals(elementConflict.getId()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            String.format("Conflict Id %s does not exist on conflicts list", conflictId)));
  }

  private Collection<ConflictInfo> findConflictsByTypes(ItemVersionConflict versionConflicts,
                                                        Set<ElementType> elementTypes) {
    return versionConflicts.getElementConflicts().stream()
        .filter(elementConflict -> elementTypes.contains(elementConflict.getType()))
        .collect(Collectors.toList());
  }

  private Conflict buildVspModelConflict(String conflictId) {
    Conflict conflict = new Conflict(conflictId, NetworkPackage, NetworkPackage.name());

    Map<String, String> yours = new HashMap<>();
    yours.put("File", "Local (Me)");
    conflict.setYours(yours);

    Map<String, String> theirs = new HashMap<>();
    theirs.put("File", "Last Committed");
    conflict.setTheirs(theirs);
    return conflict;
  }

  private CoreException getConflictNotExistException(String itemId, Version version,
                                                     String conflictId) {
    return new CoreException(new ErrorCode.ErrorCodeBuilder()
        .withCategory(ErrorCategory.APPLICATION)
        .withId(ELEMENT_CONFLICT_NOT_EXIST_ERR_ID)
        .withMessage(
            String.format(ELEMENT_CONFLICT_NOT_EXISTS_MSG, itemId, version.getId(), conflictId))
        .build());
  }
}
