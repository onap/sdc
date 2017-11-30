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

import {Component, AfterViewInit, Input, ElementRef, ChangeDetectorRef} from "@angular/core";

@Component
({
    selector: "tooltip-content",
    templateUrl: "./tooltip-content.component.html",
    styleUrls: ["./tooltip-content.component.less"]
})

export class TooltipContentComponent implements AfterViewInit {

    // -------------------------------------------------------------------------
    // Inputs / Outputs
    // -------------------------------------------------------------------------

    @Input() hostElement: HTMLElement;
    @Input() content: string;
    @Input() placement: "top"|"bottom"|"left"|"right" = "bottom";
    @Input() animation: boolean = true;

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    top: number = -100000;
    left: number = -100000;
    isIn: boolean = false;
    isFade: boolean = false;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    constructor(private element: ElementRef,
                private cdr: ChangeDetectorRef) {
    }

    // -------------------------------------------------------------------------
    // Lifecycle callbacks
    // -------------------------------------------------------------------------

    ngAfterViewInit(): void {
        this.show();
        this.cdr.detectChanges();
    }

    // -------------------------------------------------------------------------
    // Public Methods
    // -------------------------------------------------------------------------

    show(): void {
        if(!this.hostElement) {
            return;
        }

        const position = this.positionElement(this.hostElement, this.element.nativeElement.children[0], this.placement);
        this.top = position.top;
        this.left = position.left;
        this.isIn = true;
        if (this.animation) {
            this.isFade = true;
        }
    }

    hide(): void {
        this.top = -100000;
        this.left = -100000;
        this.isIn = true;
        if(this.animation) {
            this.isFade = false;
        }
    }

    // -------------------------------------------------------------------------
    // Private Methods
    // -------------------------------------------------------------------------

    private positionElement(hostElem: HTMLElement, targetElem: HTMLElement, positionStr: string, appendToBody: boolean = false): {top: number, left: number} {
        let positionStrParts = positionStr.split("-");
        let pos0 = positionStrParts[0];
        let pos1 = positionStrParts[1] || "center";
        let hostElemPosition = appendToBody ? this.offset(hostElem) : this.position(hostElem);
        let targetElemWidth = targetElem.offsetWidth;
        let targetElemHeight = targetElem.offsetHeight;
        let shiftWidth: any = {
            center(): number {
                return hostElemPosition.left + hostElemPosition.width / 2 - targetElemWidth / 2;
            },
            left(): number {
                return hostElemPosition.left;
            },
            right(): number {
                return hostElemPosition.left + hostElemPosition.width;
            }
        };

        let shiftHeight: any = {
            center: function (): number {
                return hostElemPosition.top + hostElemPosition.height / 2 - targetElemHeight / 2;
            },
            top: function (): number {
                return hostElemPosition.top;
            },
            bottom: function (): number {
                return hostElemPosition.top + hostElemPosition.height;
            }
        }

        let targetElemPosition: {top: number, left: number};

        switch (pos0) {
            case "right":
                targetElemPosition = {
                    top: shiftHeight[pos1](),
                    left: shiftWidth[pos0]()
                };
                break;

            case "left":
                targetElemPosition = {
                    top: shiftHeight[pos1](),
                    left: hostElemPosition.left - targetElemWidth
                };
                break;

            case "bottom":
                targetElemPosition = {
                    top: shiftHeight[pos0](),
                    left: shiftWidth[pos1]()
                };
                break;

            default:
                targetElemPosition = {
                    top: hostElemPosition.top - targetElemHeight,
                    left: shiftWidth[pos1]()
                };
                break;
        }

        return targetElemPosition;
    }


    private position(nativeElem: HTMLElement): {width: number, height: number, top: number, left: number} {
        let offsetParentCBR = {top: 0, left: 0};
        const elemBCR = this.offset(nativeElem);
        const offsetParentElem = this.parentOffsetElem(nativeElem);
        if(offsetParentElem !== window.document) {
            offsetParentCBR = this.offset(offsetParentElem);
            offsetParentCBR.top += offsetParentElem.clientTop - offsetParentElem.scrollTop;
            offsetParentCBR.left += offsetParentElem.clientLeft - offsetParentElem.scrollTop;
        }

        const boundingClientRect = nativeElem.getBoundingClientRect();

        return {
            width: boundingClientRect.width || nativeElem.offsetWidth,
            height: boundingClientRect.height || nativeElem.offsetHeight,
            top: elemBCR.top - offsetParentCBR.top,
            left: elemBCR.left - offsetParentCBR.left
        };
    }

    private offset(nativeElem:any): {width: number, height: number, top: number, left: number} {
        const boundingClientRect = nativeElem.getBoundingClientRect();
        return {
            width: boundingClientRect.width || nativeElem.offsetWidth,
            height: boundingClientRect.height || nativeElem.offsetHeight,
            top: boundingClientRect.top + (window.pageYOffset || window.document.documentElement.scrollTop),
            left: boundingClientRect.left + (window.pageXOffset || window.document.documentElement.scrollLeft)
        };
    }

    private getStyle(nativeElem: HTMLElement, cssProperty: string): string {
        if(window.getComputedStyle) {
            return (window.getComputedStyle(nativeElem) as any)[cssProperty];
        }

        return (nativeElem.style as any)[cssProperty];
    }

    private isStaticPositioned(nativeElem: HTMLElement): boolean {
        return (this.getStyle(nativeElem, "position") || "static") === "static";
    }

    private parentOffsetElem(nativeElem: HTMLElement): any {
        let offsetParent: any = nativeElem.offsetParent || window.document;
        while (offsetParent && offsetParent !== window.document && this.isStaticPositioned(offsetParent)) {
            offsetParent = offsetParent.offsetParent;
        }

        return offsetParent || window.document;
    }
}
