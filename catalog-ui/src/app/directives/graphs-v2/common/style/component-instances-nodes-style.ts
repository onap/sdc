import {GraphColors} from "app/utils/constants";
/**
 * Created by obarda on 12/18/2016.
 */
export class ComponentInstanceNodesStyle {

    public static getCompositionGraphStyle = ():Array<Cy.Stylesheet>  => {
        return [
            {
                selector: 'core',
                css: {
                    'shape': 'rectangle',
                    'active-bg-size': 0,
                    'selection-box-color': 'rgb(0, 159, 219)',
                    'selection-box-opacity': 0.2,
                    'selection-box-border-color': '#009fdb',
                    'selection-box-border-width': 1

                }
            },
            {
                selector: 'node',
                css: {
                    'font-family': 'omnes-regular,sans-serif',
                    'font-size': 14,
                    'events': 'yes',
                    'text-events': 'yes',
                    'text-border-width': 15,
                    'text-border-color': GraphColors.NODE_UCPE,
                    'text-margin-y': 5
                }
            },
            {
                selector: '.vf-node',
                css: {
                    'background-color': 'transparent',
                    'shape': 'rectangle',
                    'label': 'data(displayName)',
                    'background-image': 'data(img)',
                    'width': 65,
                    'height': 65,
                    'background-opacity': 0,
                    "background-width": 65,
                    "background-height": 65,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-fit': 'cover',
                    'background-clip': 'node',
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            },

            {
                selector: '.service-node',
                css: {
                    'background-color': 'transparent',
                    'label': 'data(displayName)',
                    'events': 'yes',
                    'text-events': 'yes',
                    'background-image': 'data(img)',
                    'width': 64,
                    'height': 64,
                    "border-width": 0,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-opacity': 0,
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            },
            {
                selector: '.cp-node',
                css: {
                    'background-color': 'rgb(255,255,255)',
                    'shape': 'rectangle',
                    'label': 'data(displayName)',
                    'background-image': 'data(img)',
                    'background-width': 21,
                    'background-height': 21,
                    'width': 21,
                    'height': 21,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-opacity': 0,
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            },
            {
                selector: '.vl-node',
                css: {
                    'background-color': 'rgb(255,255,255)',
                    'shape': 'rectangle',
                    'label': 'data(displayName)',
                    'background-image': 'data(img)',
                    'background-width': 21,
                    'background-height': 21,
                    'width': 21,
                    'height': 21,
                    'text-valign': 'bottom',
                    'text-halign': 'center',
                    'background-opacity': 0,
                    'overlay-color': GraphColors.NODE_BACKGROUND_COLOR,
                    'overlay-opacity': 0
                }
            },
            {
                selector: '.ucpe-cp',
                css: {
                    'background-color': GraphColors.NODE_UCPE_CP,
                    'background-width': 15,
                    'background-height': 15,
                    'width': 15,
                    'height': 15,
                    'text-halign': 'center',
                    'overlay-opacity': 0,
                    'label': 'data(displayName)',
                    'text-valign': 'data(textPosition)',
                    'text-margin-y': (ele:Cy.Collection) => {
                        return (ele.data('textPosition') == 'top') ? -5 : 5;
                    },
                    'font-size': 12
                }
            },
            {
                selector: '.ucpe-node',
                css: {
                    'background-fit': 'cover',
                    'padding-bottom': 0,
                    'padding-top': 0
                }
            },
            {
                selector: '.simple-link',
                css: {
                    'width': 1,
                    'line-color': GraphColors.BASE_LINK,
                    'target-arrow-color': '#3b7b9b',
                    'target-arrow-shape': 'triangle',
                    'curve-style': 'bezier',
                    'control-point-step-size': 30
                }
            },
            {
                selector: '.vl-link',
                css: {
                    'width': 3,
                    'line-color': GraphColors.VL_LINK,
                    'curve-style': 'bezier',
                    'control-point-step-size': 30
                }
            },
            {
                selector: '.ucpe-host-link',
                css: {
                    'width': 0
                }
            },
            {
                selector: '.not-certified-link',
                css: {
                    'width': 1,
                    'line-color': GraphColors.NOT_CERTIFIED_LINK,
                    'curve-style': 'bezier',
                    'control-point-step-size': 30,
                    'line-style': 'dashed',
                    'target-arrow-color': '#3b7b9b',
                    'target-arrow-shape': 'triangle'

                }
            },

            {
                selector: '.not-certified',
                css: {
                    'shape': 'rectangle',
                    'background-image': (ele:Cy.Collection) => {
                        return ele.data().initImage(ele)
                    },
                    "border-width": 0
                }
            },
            {
                selector: 'node:selected',
                css: {
                    "border-width": 2,
                    "border-color": GraphColors.NODE_SELECTED_BORDER_COLOR,
                    'shape': 'rectangle'
                }
            },
            {
                selector: 'edge:selected',
                css: {
                    'line-color': GraphColors.ACTIVE_LINK

                }
            },
            {
                selector: 'edge:active',
                css: {
                    'overlay-opacity': 0
                }
            }
        ]
    }

    public static getBasicNodeHanlde = () => {
        return {
            positionX: "center",
            positionY: "top",
            offsetX: 15,
            offsetY: -20,
            color: "#27a337",
            type: "default",
            single: false,
            nodeTypeNames: ["basic-node"],
            imageUrl: '/assets/styles/images/resource-icons/' + 'canvasPlusIcon.png',
            lineWidth: 2,
            lineStyle: 'dashed'

        }
    }

    public static getBasicSmallNodeHandle = () => {
        return {
            positionX: "center",
            positionY: "top",
            offsetX: 3,
            offsetY: -25,
            color: "#27a337",
            type: "default",
            single: false,
            nodeTypeNames: ["basic-small-node"],
            imageUrl: '/assets/styles/images/resource-icons/' + 'canvasPlusIcon.png',
            lineWidth: 2,
            lineStyle: 'dashed'
        }
    }

    public static getUcpeCpNodeHandle = () => {
        return {
            positionX: "center",
            positionY: "center",
            offsetX: -8,
            offsetY: -10,
            color: "#27a337",
            type: "default",
            single: false,
            nodeTypeNames: ["ucpe-cp-node"],
            imageUrl: '/assets/styles/images/resource-icons/' + 'canvasPlusIcon.png',
            lineWidth: 2,
            lineStyle: 'dashed'
        }
    }
}
