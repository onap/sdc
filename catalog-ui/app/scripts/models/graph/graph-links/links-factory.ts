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
/// <reference path="../../../references"/>
module Sdc.Utils {
    'use strict';

    export class LinksFactory {

        constructor() {
        }

        public createGraphLink = (cy: Cy.Instance, relation:Models.RelationshipModel,  singleRelation:Models.Relationship):Models.CompositionCiLinkBase => {

            let newRelation:Models.CompositionCiLinkBase;

            let fromNode:Models.Graph.CompositionCiNodeBase = cy.getElementById(relation.fromNode).data();
            let toNode:Models.Graph.CompositionCiNodeBase = cy.getElementById(relation.toNode).data() ;

            if ((relation.fromNode && fromNode.isUcpePart) || (relation.toNode && toNode.isUcpePart )) { //Link from or to node inside ucpe

                if (singleRelation && singleRelation.relationship.type && singleRelation.relationship.type == 'tosca.relationships.HostedOn') {
                    newRelation = new Models.LinkUcpeHost(relation, singleRelation);
                } else if (singleRelation.relationship.type && _.includes(singleRelation.relationship.type.toLowerCase(), 'link')) {
                    newRelation = new Models.CompositionCiVlUcpeLink(relation, fromNode.isUcpePart, singleRelation);
                } else {
                    newRelation = new Models.CompositionCiUcpeLink(relation, fromNode.isUcpePart, singleRelation);
                }
            } else if (singleRelation.relationship.type && _.includes(singleRelation.relationship.type.toLowerCase(), 'link')) {
                newRelation = new Models.CompositionCiVLink(relation, singleRelation);
            } else {
                newRelation = new Models.CompositionCiSimpleLink(relation, singleRelation);
            }

            return newRelation;
        };

        public createUcpeHostLink = (relation:Models.RelationshipModel):Models.LinkUcpeHost => {
            return new Models.LinkUcpeHost(relation);
        };

        public createVLLink = (relation:Models.RelationshipModel):Models.CompositionCiVLink => {
            return new Models.CompositionCiVLink(relation);
        }


        public createModuleGraphLinks= (relation:Models.RelationshipModel,  singleRelation:Models.Relationship):Models.ModuleCiLinkBase => {

            let newRelation:Models.ModuleCiLinkBase;

             if (_.includes(singleRelation.relationship.type.toLowerCase(), 'link')) {
                newRelation = new Models.ModuleCiVlLink(relation, singleRelation);
            } else {
                newRelation = new Models.ModuleCiLinkBase(relation, singleRelation);
            }

            return newRelation;
        };

    }
}
