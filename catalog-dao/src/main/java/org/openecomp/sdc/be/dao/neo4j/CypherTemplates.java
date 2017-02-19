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

package org.openecomp.sdc.be.dao.neo4j;

public interface CypherTemplates {

	public static final String CypherUrlTemplate = "http://$host$:$port$/db/data/transaction/commit";
	public static final String batchTemplate = "http://$host$:$port$/db/data/batch";
	public static final String getAllIndexsTemplate = "http://$host$:$port$/db/data/schema/index";

	public static final String CypherCreateNodeTemplate = "{\n\"statements\" : [ {\n \"statement\" : \"CREATE (n:$label$ { props } ) RETURN n\",\n  \"parameters\" : { \n  \"props\" : $props$ \n  }	  } ]	}";

	public static final String CypherMatchTemplate = "{\"statements\": [{\"statement\": \"MATCH (n:$label$ {$filter$}) RETURN ($type$) \" }]}";

	public static final String CypherUpdateTemplate = "{\"statements\": [{\"statement\": \"MATCH (n:$label$ {$filter$}) SET n += {props} RETURN ($type$) \",\"parameters\" : {\"props\" : {$props$}}}]}";
	public static final String CypherDeleteNodeTemplate = "{\"statements\": [{\"statement\": \"MATCH ( n:$label$ {$filter$} ) DELETE n \" }]}";

	public static final String BatchTemplate = "{  \"statements\" : [ $statementList$ ] }";

	public static final String RegularStatementTemplate = "{ \"statement\" : $statement$  }";

	public static final String CreateSingleNodeTemplate = " \"CREATE (n:$label$ { props } ) RETURN n, labels(n)\",  \"parameters\" : { \"props\" : $props$ }";

	public static final String CreateRelationTemplate = "\"MATCH (a:$labelFrom$),(b:$labelTo$) WHERE a.$idNameFrom$ = '$idValueFrom$' AND b.$idNameTo$ = '$idvalueTo$' CREATE (a)-[r:$type$ { props }  ]->(b) RETURN a, labels(a), b, labels(b), r, type(r)\",  \"parameters\": {\"props\": $props$ } ";

	public static final String CreateRelationTemplateNoProps = "\"MATCH (a:$labelFrom$),(b:$labelTo$) WHERE a.$idNameFrom$ = '$idValueFrom$' AND b.$idNameTo$ = '$idvalueTo$' CREATE (a)-[r:$type$ ]->(b) RETURN a,labels(a), b, labels(b), r, type(r)\"";

	public static final String UpdateNodeStatementTemplate = "\"MATCH (n:$label$ {$filter$}) SET n += {props} \",\"parameters\" : {\"props\" : $props$}";

	public static final String GetNodeRecursiveTemplate = "\"MATCH (m:$label$ {$filter$} )-[f$typesList$]->l RETURN m, labels(m), l, labels(l),f, type(f)\"";

	public static final String GetByRelationNodeRecursiveTemplate = "\"MATCH (n:$labelNode$ ($propsNode$} )-[r:$type$ {$propsRel$}]->(m:$labelSrc$)-[f$typesList$]->l RETURN n, labels(n), r, type(r), m, labels(m), l, labels(l),f, type(f)\"";

}
