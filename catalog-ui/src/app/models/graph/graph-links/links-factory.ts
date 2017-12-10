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
/**
 * Created by obarda on 5/1/2016.
 */
'use strict';
import {RelationshipModel, Relationship, CompositionCiLinkBase, CompositionCiNodeBase, LinkUcpeHost, CompositionCiUcpeLink,
    CompositionCiVlUcpeLink, CompositionCiSimpleLink, ModuleCiLinkBase, ModuleCiVlLink, CompositionCiVLink} from "../../../models";

export class LinksFactory {

  constructor() {
  }

  public createGraphLink = (cy:Cy.Instance, relation:RelationshipModel, singleRelation:Relationship):CompositionCiLinkBase => {

    let newRelation:CompositionCiLinkBase;

    // let fromNode:CompositionCiNodeBase = cy.getElementById(relation.fromNode).data();
    // let toNode:CompositionCiNodeBase = cy.getElementById(relation.toNode).data();
    //
    // if ((relation.fromNode && fromNode.isUcpePart) || (relation.toNode && toNode.isUcpePart )) { //Link from or to node inside ucpe
    //
    //   if (singleRelation && singleRelation.relationship.type && singleRelation.relationship.type == 'tosca.relationships.HostedOn') {
    //     newRelation = new LinkUcpeHost(relation, singleRelation);
    //   } else if (singleRelation.relationship.type && _.includes(singleRelation.relationship.type.toLowerCase(), 'link')) {
    //     newRelation = new CompositionCiVlUcpeLink(relation, fromNode.isUcpePart, singleRelation);
    //   } else {
    //     newRelation = new CompositionCiUcpeLink(relation, fromNode.isUcpePart, singleRelation);
    //   }
    // } else
     if (singleRelation.relation.relationship.type && _.includes(singleRelation.relation.relationship.type.toLowerCase(), 'link')) {
      newRelation = new CompositionCiVLink(relation, singleRelation);
    } else {
      newRelation = new CompositionCiSimpleLink(relation, singleRelation);
    }

    return newRelation;
  };

  public createUcpeHostLink = (relation:RelationshipModel):LinkUcpeHost => {
    return new LinkUcpeHost(relation);
  };

  public createVLLink = (relation:RelationshipModel):CompositionCiVLink => {
    return new CompositionCiVLink(relation);
  }


  public createModuleGraphLinks = (relation:RelationshipModel, singleRelation:Relationship):ModuleCiLinkBase => {

    let newRelation:ModuleCiLinkBase;

    if (_.includes(singleRelation.relation.relationship.type.toLowerCase(), 'link')) {
      newRelation = new ModuleCiVlLink(relation, singleRelation);
    } else {
      newRelation = new ModuleCiLinkBase(relation, singleRelation);
    }

    return newRelation;
  };

}
