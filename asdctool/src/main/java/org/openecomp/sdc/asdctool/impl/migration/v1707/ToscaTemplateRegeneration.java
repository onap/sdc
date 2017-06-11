package org.openecomp.sdc.asdctool.impl.migration.v1707;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.asdctool.impl.migration.Migration1707Task;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.EdgeLabelEnum;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.DaoStatusConverter;
import org.openecomp.sdc.be.resources.data.ESArtifactData;
import org.openecomp.sdc.be.tosca.ToscaError;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;
import org.openecomp.sdc.be.tosca.ToscaRepresentation;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("toscaTemplateRegeneration")
public class ToscaTemplateRegeneration implements Migration1707Task {

	private static Logger LOGGER = LoggerFactory.getLogger(ToscaTemplateRegeneration.class);
	
	@Autowired
	protected ArtifactCassandraDao artifactCassandraDao;
	
	@Autowired
	private ToscaExportHandler toscaExportUtils;

	@Autowired
    private ToscaOperationFacade toscaOperationFacade;
    
	@Override
	public boolean migrate() {
		boolean result = true;
		Either<List<GraphVertex>, StorageOperationStatus> getAllCertifiedComponentsRes;
		try{
			getAllCertifiedComponentsRes = getAllCertifiedComponents();
			if(getAllCertifiedComponentsRes.isRight()){
				result = false;
			}
			if(result && CollectionUtils.isNotEmpty(getAllCertifiedComponentsRes.left().value())){
				result = regenerateToscaTemplateArtifacts(getAllCertifiedComponentsRes.left().value());
			}
		} catch(Exception e){
			LOGGER.error("The exception {} has been occured upon tosca template regeneration migration. ", e);
			result = false;
		} finally {
			if(result){
				toscaOperationFacade.commit();
			} else {
				toscaOperationFacade.rollback();
			}
		}
		return result;
	}

	private boolean regenerateToscaTemplateArtifacts(List<GraphVertex> components) {
		boolean result = true;
		for(GraphVertex componentV : components){
			Either<org.openecomp.sdc.be.model.Component, StorageOperationStatus> getComponentsRes = toscaOperationFacade.getToscaElement(componentV);
			if (getComponentsRes.isRight()) {
				result = false;
				break;
			}
			if(getComponentsRes.left().value().getToscaArtifacts()!=null && getComponentsRes.left().value().getToscaArtifacts().containsKey(ToscaExportHandler.ASSET_TOSCA_TEMPLATE)){
				result = regenerateToscaTemplateArtifact(getComponentsRes.left().value(), componentV);
			}
			if(result){
				toscaOperationFacade.commit();
			} else {
				toscaOperationFacade.rollback();
				break;
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private boolean regenerateToscaTemplateArtifact(org.openecomp.sdc.be.model.Component parent, GraphVertex parentV) {
		boolean result = true;
		Either<GraphVertex, TitanOperationStatus> toscaDataVertexRes = null;
		ArtifactDataDefinition data = null;
		LOGGER.debug("tosca artifact generation");
		Either<ToscaRepresentation, ToscaError> exportComponent = toscaExportUtils.exportComponent(parent);
		if (exportComponent.isRight()) {
			LOGGER.debug("Failed export tosca yaml for component {} error {}", parent.getUniqueId(), exportComponent.right().value());
			result = false;
		}
		if(result){
			LOGGER.debug("Tosca yaml exported for component {} ", parent.getUniqueId());
			toscaDataVertexRes = toscaOperationFacade.getTitanDao().getChildVertex(parentV, EdgeLabelEnum.TOSCA_ARTIFACTS, JsonParseFlagEnum.ParseJson);
			if(toscaDataVertexRes.isRight()){
				LOGGER.debug("Failed to fetch tosca data vertex {} for component {}. Status is {}", EdgeLabelEnum.TOSCA_ARTIFACTS, parent.getUniqueId(), exportComponent.right().value());
				result = false;
			}
		}
		if(result){
			data = parent.getToscaArtifacts().get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
			data.setArtifactChecksum(GeneralUtility.calculateMD5ByByteArray(exportComponent.left().value().getMainYaml().getBytes()));
			
			((Map<String, ArtifactDataDefinition>) toscaDataVertexRes.left().value().getJson()).put(ToscaExportHandler.ASSET_TOSCA_TEMPLATE, data);
			
			Either<GraphVertex, TitanOperationStatus>  updateVertexRes = toscaOperationFacade.getTitanDao().updateVertex(toscaDataVertexRes.left().value());
			if(updateVertexRes.isRight()){
				result = false;
			}
		}
		if(result){
			ESArtifactData artifactData = new ESArtifactData(data.getEsId(), exportComponent.left().value().getMainYaml().getBytes());
			CassandraOperationStatus status = artifactCassandraDao.saveArtifact(artifactData);
			if(status != CassandraOperationStatus.OK){
				result = false;
			}
		}
		return result;
	}

	public Either<List<GraphVertex>, StorageOperationStatus> getAllCertifiedComponents() {

		Map<GraphPropertyEnum, Object> propertiesToMatch = new EnumMap<>(GraphPropertyEnum.class);
		propertiesToMatch.put(GraphPropertyEnum.STATE, LifecycleStateEnum.CERTIFIED.name());
		List<GraphVertex> components = null;
		Either<List<GraphVertex>, TitanOperationStatus> getVerticiesRes = toscaOperationFacade.getTitanDao().getByCriteria(null, propertiesToMatch,JsonParseFlagEnum.ParseMetadata);

		if (getVerticiesRes.isRight() && getVerticiesRes.right().value() != TitanOperationStatus.NOT_FOUND) {
			LOGGER.debug("Failed to fetch all certified components. Status is {}", getVerticiesRes.right().value());
			return Either.right(DaoStatusConverter.convertTitanStatusToStorageStatus(getVerticiesRes.right().value()));
		}
		if(getVerticiesRes.isLeft()){
			components = getVerticiesRes.left().value();
		}
		return Either.left(components);
	}
	
	@Override
	public String description() {
		return "toscaTemplateRegeneration";
	}
}
