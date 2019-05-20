package org.openecomp.sdc.asdctool.impl.validator.executers;

import fj.data.Either;
import org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ArtifactValidatorExecuter{
	
	 @Autowired
	 protected JanusGraphDao janusGraphDao;

	 @Autowired
	 private ToscaOperationFacade toscaOperationFacade;
	 private static Logger log = Logger.getLogger(ArtifactValidatorExecuter.class.getName());
	 
	 protected String name;

	    public void setName(String name) {
	        this.name = name;
	    }

	    public String getName() {
	        return name;
	    }

	 
	
	   public Map<String, List<Component>> getVerticesToValidate(VertexTypeEnum type, Map<GraphPropertyEnum, Object> hasProps){
		   Map<String, List<Component>> result = new HashMap<>();
	        Either<List<GraphVertex>, JanusGraphOperationStatus> resultsEither = janusGraphDao
              .getByCriteria(type, hasProps);
	        if (resultsEither.isRight()) {
	        	log.error("getVerticesToValidate failed "+ resultsEither.right().value());
	            return result;
	        }
	        System.out.println("getVerticesToValidate: "+resultsEither.left().value().size()+" vertices to scan");
	        List<GraphVertex> componentsList = resultsEither.left().value();
	        componentsList.forEach(vertex -> {
	        	String ivariantUuid = (String)vertex.getMetadataProperty(GraphPropertyEnum.INVARIANT_UUID);
	        	if(!result.containsKey(ivariantUuid)){
	        		List<Component> compList = new ArrayList<Component>();
	        		result.put(ivariantUuid, compList);
	        	}
	        	List<Component> compList = result.get(ivariantUuid);
	        	
	        	ComponentParametersView filter = new ComponentParametersView(true);				
				filter.setIgnoreArtifacts(false);
				
				Either<Component, StorageOperationStatus> toscaElement = toscaOperationFacade.getToscaElement(vertex.getUniqueId(), filter);
				if (toscaElement.isRight()) {
					log.error("getVerticesToValidate: failed to find element"+ vertex.getUniqueId()+" staus is" + toscaElement.right().value());
				}else{
					compList.add(toscaElement.left().value());
				}
	        	 
	        });	        
	      
			return result;
	    }
	    
	   public boolean validate( Map<String, List<Component>> vertices) {
		   boolean result = true;
		   long time = System.currentTimeMillis();
		   String fileName = ValidationConfigManager.getOutputFilePath() + this.getName() + "_"+ time + ".csv";
		   try(Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"))) {
			writer.write("name, UUID, invariantUUID, state, version\n");
			Collection<List<Component>> collection = vertices.values();
			for(List<Component> compList: collection ){
				Set<String> artifactEsId = new HashSet<>();
				for(Component component: compList ){
					Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
					Optional<ArtifactDefinition> op = toscaArtifacts.values().
							stream().filter(a -> artifactEsId.contains(a.getEsId())).findAny();
					if(op.isPresent()){
						result = false;
						writeModuleResultToFile(writer, compList);
						writer.flush();
						break;
					}else{
						artifactEsId.addAll(toscaArtifacts.values().stream().map(ArtifactDefinition::getEsId).collect(Collectors.toList()))	;
					}
				}
				
			}
			
		   } catch (Exception e) {
				log.error("Failed to fetch vf resources ", e);
				return false;
			} finally {
				janusGraphDao.commit();
			}
			return result;
	    }
	   
	   private void writeModuleResultToFile(Writer writer, List<Component> components) {
			try {
				// "service name, service id, state, version
				for(Component component: components ){
					StringBuffer sb = new StringBuffer(component.getName());
					sb.append(",").append(component.getUniqueId()).append(",").append(component.getInvariantUUID()).append(",").append(component.getLifecycleState()).append(",").append(component.getVersion());
					
					sb.append("\n");
					writer.write(sb.toString());
				}
			} catch (IOException e) {
				log.error("Failed to write module result to file ", e);
			}
		}

}
