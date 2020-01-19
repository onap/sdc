/**
 * Created by ob0695 on 6/28/2018.
 */
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

import { Component, HostListener } from '@angular/core';
import { Select } from '@ngxs/store';
import { LeftPaletteComponent, LeftPaletteMetadataTypes } from 'app/models/components/displayComponent';
import { Point } from 'app/models/graph/point';
import { WorkspaceState } from 'app/ng2/store/states/workspace.state';
import Dictionary = _.Dictionary;
import { EventListenerService } from 'app/services/event-listener-service';
import { GRAPH_EVENTS } from 'app/utils/constants';
import { DndDropEvent } from 'ngx-drag-drop/ngx-drag-drop';
import { CompositionPaletteService } from './services/palette.service';
import {PolicyMetadata} from "../../../../models/policy-metadata";
import {GenericBrowserDomAdapter} from "@angular/platform-browser/src/browser/generic_browser_adapter";

@Component({
    selector: 'composition-palette',
    templateUrl: './palette.component.html',
    styleUrls: ['./palette.component.less']
})
export class PaletteComponent {

    constructor(private compositionPaletteService: CompositionPaletteService, private eventListenerService: EventListenerService) {}

    @Select(WorkspaceState.isViewOnly) isViewOnly$: boolean;
    private paletteElements: Dictionary<Dictionary<LeftPaletteComponent[]>>;
    public numberOfElements: number = 0;
    public isPaletteLoading: boolean;
    private paletteDraggedElement: LeftPaletteComponent;
    public position: Point = new Point();

    ngOnInit() {
        this.isPaletteLoading = true;

        this.compositionPaletteService.subscribeToLeftPaletteElements((leftPaletteElementsResponse) => {
            this.paletteElements = leftPaletteElementsResponse;
            this.numberOfElements = this.countLeftPalleteElements(this.paletteElements);
            this.isPaletteLoading = false;
        }, () => {
            this.isPaletteLoading = false;
        });

    }

    public buildPaletteByCategories = (searchText?: string) => { // create nested by category & subcategory, filtered by search parans
        // Flat the object and run on its leaves
        if (searchText) {
            searchText = searchText.toLowerCase();
            const paletteElementsAfterSearch = {};
            this.paletteElements = this.compositionPaletteService.getLeftPaletteElements();
            for (const category in this.paletteElements) {
                for (const subCategory in this.paletteElements[category]) {
                    const subCategoryToCheck = this.paletteElements[category][subCategory];
                    const res = subCategoryToCheck.filter((item) => item.searchFilterTerms.toLowerCase().indexOf(searchText) >= 0)
                    if (res.length > 0) {
                        paletteElementsAfterSearch[category] = {};
                        paletteElementsAfterSearch[category][subCategory] = res;
                    }
                }
            }
            this.paletteElements = paletteElementsAfterSearch;
        } else {
            this.paletteElements = this.compositionPaletteService.getLeftPaletteElements();
        }
        this.numberOfElements = this.countLeftPalleteElements(this.paletteElements);
    }

    public onSearchChanged = (searchText: string) => {

        if (this.compositionPaletteService.getLeftPaletteElements()) {
            this.buildPaletteByCategories(searchText);
        }
    }

    private countLeftPalleteElements(leftPalleteElements: Dictionary<Dictionary<LeftPaletteComponent[]>>) {
        // Use _ & flat map
        let counter = 0;
        for (const category in leftPalleteElements) {
            for (const subCategory in leftPalleteElements[category]) {
                counter += leftPalleteElements[category][subCategory].length;
            }
        }
        return counter;
    }

    private isGroupOrPolicy(component: LeftPaletteComponent): boolean {
        if (component &&
            (component.categoryType === LeftPaletteMetadataTypes.Group ||
            component.categoryType === LeftPaletteMetadataTypes.Policy)) {
            return true;
        }
        return false;
    }
    @HostListener('document:dragover', ['$event'])
    public onDrag(event) {
        this.position.x = event.clientX;
        this.position.y = event.clientY;
    }
    
    //---------------------------------------Palette Events-----------------------------------------//

    public onDraggableMoved = (event:DragEvent) => {  
          let draggedElement = document.getElementById("draggable_element");
          draggedElement.style.top = (this.position.y - 80) + "px";
          draggedElement.style.left = (this.position.x - 30) + "px";
          this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DRAG_ACTION, this.position);
    }

    public onDragStart = (event, draggedElement:LeftPaletteComponent) => { // Applying the dragged svg component to the draggable element
        
        this.paletteDraggedElement = draggedElement;
        event.dataTransfer.dropEffect = "copy";
        let hiddenImg = document.createElement("span");
        event.dataTransfer.setDragImage(hiddenImg, 0, 0);
    }


    public onDrop = (event:DndDropEvent) => {
        let draggedElement = document.getElementById("draggable_element");
        draggedElement.style.top = "-9999px";
        if(draggedElement.classList.contains('valid-drag')) {
            if(!event.data){
                event.data = this.paletteDraggedElement;
            }
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_DROP, event);
        } else {
            console.log("INVALID drop");
        }
        this.paletteDraggedElement = undefined;
        
    }

    public onMouseOver = (sectionElem:MouseEvent, displayComponent:LeftPaletteComponent) => {
        console.debug("On palette element MOUSE HOVER: ", displayComponent);
        if (this.isGroupOrPolicy(displayComponent)) {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_SHOW_POPUP_PANEL, displayComponent, sectionElem.target);
        } else {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_IN, displayComponent);
        }
    };

    public onMouseOut = (displayComponent:LeftPaletteComponent) => {
        console.debug("On palette element MOUSE OUT: ", displayComponent);
        if (this.isGroupOrPolicy(displayComponent)) {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HIDE_POPUP_PANEL);
        } else {
            this.eventListenerService.notifyObservers(GRAPH_EVENTS.ON_PALETTE_COMPONENT_HOVER_OUT);
        }
    };

}
