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

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ArtifactValidatorExecuter{

	 protected JanusGraphDao janusGraphDao;
	protected ToscaOperationFacade toscaOperationFacade;

	public ArtifactValidatorExecuter(JanusGraphDao janusGraphDao,
		ToscaOperationFacade toscaOperationFacade) {
		this.janusGraphDao = janusGraphDao;
		this.toscaOperationFacade = toscaOperationFacade;
	}

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
