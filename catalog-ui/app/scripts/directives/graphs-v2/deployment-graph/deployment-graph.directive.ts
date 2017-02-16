/**
 * Created by obarda on 12/19/2016.
 */
/// <reference path="../../../references"/>
module Sdc.Directives {

    import Util = jasmine.Util;

    interface IDeploymentGraphScope extends ng.IScope {
        component:Models.Components.Component;
    }

    export class DeploymentGraph implements ng.IDirective {
        private _cy:Cy.Instance;

        constructor(private NodesFactory:Utils.NodesFactory, private commonGraphUtils:Graph.Utils.CommonGraphUtils,
                    private deploymentGraphGeneralUtils:Graph.Utils.DeploymentGraphGeneralUtils, private ComponentInstanceFactory: Sdc.Utils.ComponentInstanceFactory) {
        }

        restrict = 'E';
        templateUrl = '/app/scripts/directives/graphs-v2/deployment-graph/deployment-graph.html';
        scope = {
            component: '=',
            isViewOnly: '='
        };

        link = (scope:IDeploymentGraphScope, el:JQuery) => {
            if(scope.component.isResource()) {
                this.loadGraph(scope, el);
                this.registerGraphEvents();
            }
        };


        public initGraphNodes = (cy:Cy.Instance, component:Models.Components.Component):void => {
            if (component.groups) { // Init module nodes
                _.each(component.groups, (groupModule:Models.Module) => {
                    let moduleNode = this.NodesFactory.createModuleNode(groupModule);
                    this.commonGraphUtils.addNodeToGraph(cy, moduleNode);

                });
            }
            _.each(component.componentInstances, (instance:Models.ComponentsInstances.ComponentInstance) => { // Init component instance nodes
                let componentInstanceNode = this.NodesFactory.createNode(instance);
                componentInstanceNode.parent = this.deploymentGraphGeneralUtils.findInstanceModule(component.groups, instance.uniqueId);
                if (componentInstanceNode.parent) { // we are not drawing instances that are not a part of a module
                   this.commonGraphUtils.addComponentInstanceNodeToGraph(cy, componentInstanceNode);
                }
            });

            // This is a special functionality to pass the cytoscape default behavior - we can't create Parent module node without children's
            // so we must add an empty dummy child node
            _.each(this._cy.nodes('[?isGroup]'), (moduleNode: Cy.CollectionFirstNode) => {
                if (!moduleNode.isParent()) {
                    let dummyInstance = this.ComponentInstanceFactory.createEmptyComponentInstance();
                    let componentInstanceNode = this.NodesFactory.createNode(dummyInstance);
                    componentInstanceNode.parent = moduleNode.id();
                    let dummyNode = this.commonGraphUtils.addNodeToGraph(cy, componentInstanceNode, moduleNode.position());
                    dummyNode.addClass('dummy-node');
                }
            })
        };

        private registerGraphEvents() {

            this._cy.on('afterExpand', (event) => {
                event.cyTarget.qtip({});
            });

            this._cy.on('afterCollapse', (event) => {
                this.commonGraphUtils.initNodeTooltip(event.cyTarget);
            });
        }

        private loadGraph = (scope:IDeploymentGraphScope, el:JQuery) => {

            let graphEl = el.find('.sdc-deployment-graph-wrapper');
            this._cy = cytoscape({
                container: graphEl,
                style: Sdc.Graph.Utils.ComponentIntanceNodesStyle.getCompositionGraphStyle().concat(Sdc.Graph.Utils.ModulesNodesStyle.getModuleGraphStyle()),
                zoomingEnabled: false,
                selectionType: 'single',

            });

            //adding expand collapse extension 
            this._cy.expandCollapse({
                layoutBy: {
                    name: "grid",
                    animate: true,
                    randomize: false,
                    fit: true
                },
                fisheye: false,
                undoable: false,
                expandCollapseCueSize: 18,
                expandCueImage: Sdc.Utils.Constants.IMAGE_PATH + '/styles/images/resource-icons/' + 'closeModule.png',
                collapseCueImage: Sdc.Utils.Constants.IMAGE_PATH + '/styles/images/resource-icons/' + 'openModule.png',
                expandCollapseCueSensitivity: 2,
                cueOffset: -20
            });

            this.initGraphNodes(this._cy, scope.component); //creating instances nodes
            this.commonGraphUtils.initGraphLinks(this._cy, scope.component.componentInstancesRelations);
            this._cy.collapseAll();
        };

        public static factory = (NodesFactory:Utils.NodesFactory, CommonGraphUtils:Graph.Utils.CommonGraphUtils, DeploymentGraphGeneralUtils:Graph.Utils.DeploymentGraphGeneralUtils, ComponentInstanceFactory: Utils.ComponentInstanceFactory) => {
            return new DeploymentGraph(NodesFactory, CommonGraphUtils, DeploymentGraphGeneralUtils, ComponentInstanceFactory)
        }
    }

    DeploymentGraph.factory.$inject = ['NodesFactory', 'CommonGraphUtils', 'DeploymentGraphGeneralUtils', 'ComponentInstanceFactory'];
}