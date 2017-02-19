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
/// <reference path="../../references"/>
module Sdc.Directives {
    'use strict';


    export interface IStructureTreeScope extends ng.IScope {

        component: Models.Components.Component;
        structureTree: StructureTree;
    }

    class StructureTree {

        serviceRoot:ResourceInstanceNode;

        constructor(private uniqueId:string, private resourceInstanceName:string, private resourceInstanceIcon:string, private certified:boolean) {
            this.serviceRoot = new ResourceInstanceNode(uniqueId, resourceInstanceName, resourceInstanceIcon, certified);
        }

    }

    class ResourceInstanceNode {
        id:string;
        icon:string;
        name:string;
        resourceInstancesList:Array<ResourceInstanceNode>;
        isAlreadyInTree:boolean;
        certified:boolean;


        constructor(private uniqueId:string, private resourceInstanceName:string, private resourceInstanceIcon:string, certified:boolean) {
            this.id = uniqueId;
            this.name = resourceInstanceName;
            this.icon = resourceInstanceIcon;
            this.resourceInstancesList = [];
            this.isAlreadyInTree = false;
            this.certified = certified;
        }
    }

    export class StructureTreeDirective implements ng.IDirective {


        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            component: '=',
        };
        restrict = 'E';
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/structure-tree/structure-tree-directive.html');
        };

        link = (scope:IStructureTreeScope, $elem:any) => {

            let RESOURCE_INSTANCE_LIST:string = "resourceInstancesChildesList";
            let resourceInstanceMap:Utils.Dictionary<string, ResourceInstanceNode>;
            let relations:Array<Models.RelationshipModel>;
            //************* Start Building Tree Functions *******************//

            //remove unnecessary instances
            let initResourceInstanceMap = ():void => {

                resourceInstanceMap = new Utils.Dictionary<string, ResourceInstanceNode>();

                _.forEach(scope.component.componentInstances, (resourceInstance:Models.ComponentsInstances.ComponentInstance)=> {
                    if (_.some(Object.keys(resourceInstance.capabilities), (key:string)=> {
                            return 'tosca.capabilities.container' == key.toLowerCase();
                        }) || _.some(Object.keys(resourceInstance.requirements),(key:string)=> {
                            return 'tosca.capabilities.container' == key.toLowerCase();
                        })) {

                    let isCertified = 0 === (parseFloat(resourceInstance.componentVersion) % 1);
                        let node:ResourceInstanceNode = new ResourceInstanceNode(resourceInstance.uniqueId,
                            resourceInstance.name,
                            resourceInstance.icon,
                            isCertified);
                        resourceInstanceMap.setValue(resourceInstance.uniqueId, node);
                    }
                });
            };

            //remove unnecessary relations
            let initRelations = ():void => {
                relations = _.filter(scope.component.componentInstancesRelations, (relation:Models.RelationshipModel)=> {
                    return resourceInstanceMap.containsKey(relation.fromNode) && resourceInstanceMap.containsKey(relation.toNode);
                });
            };

            let buildTree = ():void => {
                if (scope.component) {
                    scope.structureTree = new StructureTree(scope.component.uniqueId, scope.component.name, scope.component.icon, 'CERTIFIED' === scope.component.lifecycleState);
                    initResourceInstanceMap();
                    initRelations();

                    let parentNodesList = _.groupBy(relations, (node:any)=> {
                        return node.fromNode;
                    });

                    for (let parent in parentNodesList) {
                        _.forEach(parentNodesList[parent], (childNode)=> {
                            parentNodesList[parent][RESOURCE_INSTANCE_LIST] = [];
                            parentNodesList[parent][RESOURCE_INSTANCE_LIST].push(mergeAllSubtrees(childNode, parentNodesList));
                        });
                    }

                    //add the resourceInstanceList for the service root node
                    for (let parent in parentNodesList) {
                        let resourceInstanceNode:ResourceInstanceNode = resourceInstanceMap.getValue(parent);
                        resourceInstanceNode.resourceInstancesList = parentNodesList[parent];
                        resourceInstanceNode.resourceInstancesList = parentNodesList[parent][RESOURCE_INSTANCE_LIST];
                        resourceInstanceNode.isAlreadyInTree = true;
                        scope.structureTree.serviceRoot.resourceInstancesList.push(resourceInstanceNode);
                    }

                    // Add all node that have no connection to the rootNode
                    resourceInstanceMap.forEach((key:string, value:ResourceInstanceNode) => {
                        if (!value.isAlreadyInTree) {
                            scope.structureTree.serviceRoot.resourceInstancesList.push(value);
                        }
                    });
                }
            };

            //this recursion is merging all the subtrees
            let mergeAllSubtrees = (connectionData:any, parentNodesList:any):ResourceInstanceNode => {
                let resourceInstanceNode:ResourceInstanceNode = resourceInstanceMap.getValue(connectionData.toNode);
                resourceInstanceNode.isAlreadyInTree = true;
                if (parentNodesList[resourceInstanceNode.id]) {
                    if (parentNodesList[resourceInstanceNode.id][RESOURCE_INSTANCE_LIST]) {
                        resourceInstanceNode.resourceInstancesList = parentNodesList[resourceInstanceNode.id][RESOURCE_INSTANCE_LIST];
                    }
                    else {
                        _.forEach(parentNodesList[resourceInstanceNode.id], (children)=> {
                            resourceInstanceNode.resourceInstancesList.push(mergeAllSubtrees(children, parentNodesList));
                        });
                    }
                    delete parentNodesList[resourceInstanceNode.id];
                }
                return resourceInstanceNode;
            };
            //************* End Building Tree Functions *******************//

            //************* Start Watchers *******************//
            scope.$watch('component.name', ():void => {
                if (scope.structureTree)
                    scope.structureTree.serviceRoot.name = scope.component.name;
            });

            scope.$watch('component.icon', ():void => {
                if (scope.structureTree)
                    scope.structureTree.serviceRoot.icon = scope.component.icon;
            });

            scope.$watchCollection('component.componentInstancesRelations', ():void => {
                buildTree();
            });

            scope.$watchCollection('component.componentInstances', ():void => {
                buildTree();
            });

            //************* End  Watchers *******************//

            buildTree();

        };


        public static factory = ($templateCache:ng.ITemplateCacheService) => {
            return new StructureTreeDirective($templateCache);
        };
    }

    StructureTreeDirective.factory.$inject = ['$templateCache'];

}
