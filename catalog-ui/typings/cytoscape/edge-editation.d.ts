/**
 * Created by obarda on 11/6/2016.
 */

interface CytoscapeEdgeEditation {
    new(): CytoscapeEdgeEditation;

    init(cy: Cy.Instance, handleSize?: number):void;
    registerHandle(handle: Handle): void;
}

interface Handle {
    positionX: string,
    positionY: string,
    offsetX?: number,
    offsetY?: number,
    color: string,
    type: string,
    single: boolean,
    nodeTypeNames: Array<string>;
    imageUrl: string;
    lineWidth: number;
    lineStyle: string;
}

declare var CytoscapeEdgeEditation: CytoscapeEdgeEditation;

declare function require(name:string);