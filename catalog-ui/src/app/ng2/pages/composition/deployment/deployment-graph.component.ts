import {Component, ElementRef, Inject, OnInit} from "@angular/core";
import {DeploymentGraphService} from "./deployment-graph.service";
import '@bardit/cytoscape-expand-collapse';
import * as _ from "lodash";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import {WorkspaceService} from "../../workspace/workspace.service";
import {NodesFactory} from "../../../../models/graph/nodes/nodes-factory";
import {CommonGraphUtils} from "../graph/common/common-graph-utils";
import {ISdcConfig, SdcConfigToken} from "../../../config/sdc-config.config";
import {Module} from "../../../../models/modules/base-module";
import {ComponentInstance} from "../../../../models/componentsInstances/componentInstance";
import {ComponentGenericResponse} from "../../../services/responses/component-generic-response";
import {ComponentInstanceFactory} from "../../../../utils/component-instance-factory";
import {ModulesNodesStyle} from "../graph/common/style/module-node-style";
import {ComponentInstanceNodesStyle} from "../graph/common/style/component-instances-nodes-style";
import {CompositionGraphLinkUtils} from "../graph/utils/composition-graph-links-utils";

@Component({
    selector: 'deployment-graph',
    templateUrl: './deployment-graph.component.html',
    styleUrls: ['./deployment-graph.component.less']
})

export class DeploymentGraphComponent implements OnInit {
    constructor(private elRef: ElementRef,
                private topologyTemplateService: TopologyTemplateService,
                private workspaceService: WorkspaceService,
                private deploymentService: DeploymentGraphService,
                private commonGraphUtils: CommonGraphUtils,
                private nodeFactory: NodesFactory,
                private commonGraphLinkUtils: CompositionGraphLinkUtils,
                @Inject(SdcConfigToken) private sdcConfig: ISdcConfig) {

    }

    public _cy: Cy.Instance;

    ngOnInit(): void {
        this.topologyTemplateService.getDeploymentGraphData(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId).subscribe((response: ComponentGenericResponse) => {
            this.deploymentService.componentInstances = response.componentInstances;
            this.deploymentService.componentInstancesRelations = response.componentInstancesRelations;
            this.deploymentService.modules = response.modules;
            this.loadGraph();
        });
    }

    public findInstanceModule = (groupsArray: Array<Module>, componentInstanceId: string): string => {
        let parentGroup: Module = _.find(groupsArray, (group: Module) => {
            return _.find(_.values(group.members), (member: string) => {
                return member === componentInstanceId;
            });
        });
        return parentGroup ? parentGroup.uniqueId : "";
    };

    public initGraphModules = () => {
        if (this.deploymentService.modules) { // Init module nodes
            _.each(this.deploymentService.modules, (groupModule: Module) => {
                let moduleNode = this.nodeFactory.createModuleNode(groupModule);
                this.commonGraphUtils.addNodeToGraph(this._cy, moduleNode);
            });
        }
    }

    public initGraphComponentInstances = () => {
        _.each(this.deploymentService.componentInstances, (instance: ComponentInstance) => { // Init component instance nodes
            let componentInstanceNode = this.nodeFactory.createNode(instance);
            componentInstanceNode.parent = this.findInstanceModule(this.deploymentService.modules, instance.uniqueId);
            if (componentInstanceNode.parent) { // we are not drawing instances that are not a part of a module
                this.commonGraphUtils.addComponentInstanceNodeToGraph(this._cy, componentInstanceNode);
            }
        });
    }

    public handleEmptyModule = () => {
        // This is a special functionality to pass the cytoscape default behavior - we can't create Parent module node without children's
        // so we must add an empty dummy child node
        _.each(this._cy.nodes('[?isGroup]'), (moduleNode: Cy.CollectionFirstNode) => {
            if (!moduleNode.isParent()) {
                let dummyInstance = ComponentInstanceFactory.createEmptyComponentInstance();
                let componentInstanceNode = this.nodeFactory.createNode(dummyInstance);
                componentInstanceNode.parent = moduleNode.id();
                let dummyNode = this.commonGraphUtils.addNodeToGraph(this._cy, componentInstanceNode, moduleNode.position());
                dummyNode.addClass('dummy-node');
            }
        })
    }

    public initGraphNodes = (): void => {
        this.initGraphModules();
        this.initGraphComponentInstances();
        this.handleEmptyModule();
    };

    private loadGraph = () => {

        let graphEl = this.elRef.nativeElement.querySelector('.sdc-deployment-graph-wrapper');
        this._cy = cytoscape({
            container: graphEl,
            style: ComponentInstanceNodesStyle.getCompositionGraphStyle().concat(ModulesNodesStyle.getModuleGraphStyle()),
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
            expandCueImage: this.sdcConfig.imagesPath + '/assets/styles/images/resource-icons/' + 'closeModule.png',
            collapseCueImage: this.sdcConfig.imagesPath + '/assets/styles/images/resource-icons/' + 'openModule.png',
            expandCollapseCueSensitivity: 2,
            cueOffset: -20
        });

        this.initGraphNodes(); //creating instances nodes
        this.commonGraphLinkUtils.initGraphLinks(this._cy, this.deploymentService.componentInstancesRelations);
        this._cy.collapseAll();
    };
}