/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model;

import org.openecomp.sdc.be.model.jsontitan.datamodel.NodeType;

/**
 * Specifies an implementation artifact for interfaces or operations of a
 * {@link NodeType node type} or {@link RelationshipType relation type}.
 * 
 * @author esofer
 */
public class ImplementationArtifact {
	/**
	 * <p>
	 * Specifies the type of this artifact.
	 * </p>
	 */
	private String artifactType;

	/**
	 * <p>
	 * Identifies an Artifact Template to be used as implementation artifact.
	 * This Artifact Template can be defined in the same Definitions document or
	 * in a separate, imported document.
	 * </p>
	 * 
	 * <p>
	 * The type of Artifact Template referenced by the artifactRef attribute
	 * MUST be the same type or a sub-type of the type specified in the
	 * artifactType attribute.
	 * </p>
	 * 
	 * <p>
	 * Note: if no Artifact Template is referenced, the artifact type specific
	 * content of the ImplementationArtifact element alone is assumed to
	 * represent the actual artifact. For example, a simple script could be
	 * defined in place within the ImplementationArtifact element.
	 * </p>
	 */
	private String artifactRef;

	/**
	 * The name of the archive in which the artifact lies.
	 */
	private String archiveName;
	/**
	 * The version of the archive in which the artifact lies.
	 */
	private String archiveVersion;
}
