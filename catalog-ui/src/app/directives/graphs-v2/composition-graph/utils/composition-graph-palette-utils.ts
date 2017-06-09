import {EventListenerService, LoaderService} from "app/services";
import {CapabilitiesGroup, NodesFactory, ComponentInstance, Component, CompositionCiNodeBase, RequirementsGroup} from "app/models";
import {ComponentFactory, ComponentInstanceFactory, GRAPH_EVENTS, GraphUIObjects} from "app/utils";
import {CompositionGraphGeneralUtils} from "./composition-graph-general-utils";
import {CommonGraphUtils} from "../../common/common-graph-utils";
import 'angular-dragdrop';
import {LeftPaletteComponent} from "../../../../models/components/displayComponent";

export class CompositionGraphPaletteUtils {

    constructor(private ComponentFactory:ComponentFactory,
                private $filter:ng.IFilterService,
                private loaderService:LoaderService,
                private generalGraphUtils:CompositionGraphGeneralUtils,
                private componentInstanceFactory:ComponentInstanceFactory,
                private nodesFactory:NodesFactory,
                private commonGraphUtils:CommonGraphUtils,
                private eventListenerService:EventListenerService) {
    }

    /**
     * Calculate the dragged element (html element) position on canvas
     * @param cy
     * @param event
     * @param position
     * @returns {Cy.BoundingBox}
     * @private
     */
    private _getNodeBBox(cy:Cy.Instance, event:IDragDropEvent, position?:Cy.Position) {
        let bbox = <Cy.BoundingBox>{};
        if (!position) {
            position = this.commonGraphUtils.getCytoscapeNodePosition(cy, event);
        }
        let cushionWidth:number = 40;
        let cushionHeight:number = 40;

        bbox.x1 = position.x - cushionWidth / 2;
        bbox.y1 = position.y - cushionHeight / 2;
        bbox.x2 = position.x + cushionWidth / 2;
        bbox.y2 = position.y + cushionHeight / 2;
        return bbox;
    }

    /**
     * Create the component instance, update data from parent component in the left palette and notify on_insert_to_ucpe if component was dragg into ucpe
     * @param cy
     * @param fullComponent
     * @param event
     * @param component
     */
    private _createComponentInstanceOnGraphFromPaletteComponent(cy:Cy.Instance, fullComponent:LeftPaletteComponent, event:IDragDropEvent, component:Component) {

        let componentInstanceToCreate:ComponentInstance = this.componentInstanceFactory.createComponentInstanceFromComponent(fullComponent);
        let cytoscapePosition:Cy.Position = this.commonGraphUtils.getCytoscapeNodePosition(cy, event);

        componentInstanceToCreate.posX = cytoscapePosition.x;
        componentInstanceToCreate.posY = cytoscapePosition.y;


        let onFailedCreatingInstance:(error:any) => void = (error:any) => {
            this.loaderService.hideLoader('composition-graph');
        };

        //on success - update node data
        let onSuccessCreatingInstance = (createInstance:ComponentInstance):void => {

            this.loaderService.hideLoader('composition-graph');

            createInstance.name = this.$filter('resourceName')(createInstance.name);
            createInstance.requirements = new RequirementsGroup(createInstance.requirements);
            createInstance.capabilities = new CapabilitiesGroup(createInstance.capabilities);
            createInstance.componentVersion = fullComponent.version;
            createInstance.icon = fullComponent.icon;
            createInstance.setInstanceRC();

            let newNode:CompositionCiNodeBase = this.nodesFactory.createNode(createInstance);
            let cyNode:Cy.CollectionNodes = this.commonGraphUtils.addComponentInstanceNodeToGraph(cy, newNode);

            //check if node was dropped into a UCPE
            let ucpe:Cy.CollectionElements = this.commonGraphUtils.isInUcpe(cy, cyNode.boundingbox());
            if (ucpe.length > 0) {
                this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_INSERT_NODE_TO_UCPE, cyNode, ucpe, false);
            }
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_CREATE_COMPONENT_INSTANCE);

        };

        this.loaderService.showLoader('composition-graph');

        // Create the component instance on server
        this.generalGraphUtils.getGraphUtilsServerUpdateQueue().addBlockingUIAction(() => {
            component.createComponentInstance(componentInstanceToCreate).then(onSuccessCreatingInstance, onFailedCreatingInstance);
        });
    }

    /**
     * Thid function applay red/green background when component dragged from palette
     * @param cy
     * @param event
     * @param dragElement
     * @param dragComponent
     */
    public onComponentDrag(cy:Cy.Instance, event:IDragDropEvent, dragElement:JQuery, dragComponent:ComponentInstance) {

        if (event.clientX < GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET || event.clientY < GraphUIObjects.DIAGRAM_HEADER_OFFSET) { //hovering over palette. Dont bother computing validity of drop
            dragElement.removeClass('red');
            return;
        }

        let offsetPosition = {
            x: event.clientX - GraphUIObjects.DIAGRAM_PALETTE_WIDTH_OFFSET,
            y: event.clientY - GraphUIObjects.DIAGRAM_HEADER_OFFSET
        };
        let bbox = this._getNodeBBox(cy, event, offsetPosition);

        if (this.generalGraphUtils.isPaletteDropValid(cy, bbox, dragComponent)) {
            dragElement.removeClass('red');
        } else {
            dragElement.addClass('red');
        }
    }

    /**
     *  This function is called when after dropping node on canvas
     *  Check if the capability & requirements fulfilled and if not get from server
     * @param cy
     * @param event
     * @param component
     */
    public addNodeFromPalette(cy:Cy.Instance, event:IDragDropEvent, component:Component) {
        this.loaderService.showLoader('composition-graph');

        let draggedComponent:LeftPaletteComponent = event.dataTransfer.component;

        if (this.generalGraphUtils.componentRequirementsAndCapabilitiesCaching.containsKey(draggedComponent.uniqueId)) {
            let fullComponent = this.generalGraphUtils.componentRequirementsAndCapabilitiesCaching.getValue(draggedComponent.uniqueId);
            draggedComponent.capabilities = fullComponent.capabilities;
            draggedComponent.requirements = fullComponent.requirements;
            this._createComponentInstanceOnGraphFromPaletteComponent(cy, draggedComponent, event, component);

        } else {

            this.ComponentFactory.getComponentFromServer(draggedComponent.getComponentSubType(), draggedComponent.uniqueId)
                .then((fullComponent:Component) => {
                    draggedComponent.capabilities = fullComponent.capabilities;
                    draggedComponent.requirements = fullComponent.requirements;
                    this._createComponentInstanceOnGraphFromPaletteComponent(cy, draggedComponent, event, component);
                });
        }
    }
}


CompositionGraphPaletteUtils.$inject = [
    'ComponentFactory',
    '$filter',
    'LoaderService',
    'CompositionGraphGeneralUtils',
    'ComponentInstanceFactory',
    'NodesFactory',
    'CommonGraphUtils',
    'EventListenerService'
];
