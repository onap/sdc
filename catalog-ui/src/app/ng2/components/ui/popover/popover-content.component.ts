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

import { AfterViewInit, ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, OnDestroy, Output, Renderer, ViewChild } from '@angular/core';
import { ButtonsModelMap } from 'app/models';
import { PopoverComponent } from './popover.component';

@Component({
    selector: 'popover-content',
    templateUrl: './popover-content.component.html',
    styleUrls: ['popover-content.component.less']
})
export class PopoverContentComponent implements AfterViewInit, OnDestroy {

    @Input() public title: string;
    @Input() public buttons: ButtonsModelMap;

    @Input()
    content: string;

    @Input()
    placement: 'top'|'bottom'|'left'|'right'|'auto'|'auto top'|'auto bottom'|'auto left'|'auto right' = 'bottom';

    @Input()
    animation: boolean = true;

    @Input()
    closeOnClickOutside: boolean = false;

    @Input()
    closeOnMouseOutside: boolean = false;

    @Input()
    hideArrow: boolean = false;

    @ViewChild('popoverDiv')
    popoverDiv: ElementRef;

    buttonsNames: string[];

    popover: PopoverComponent;
    onCloseFromOutside = new EventEmitter();
    top: number = 250;
    left: number = 300;
    isIn: boolean = false;
    displayType: string = 'none';
    effectivePlacement: string;

    listenClickFunc: any;
    listenMouseFunc: any;

    constructor(protected element: ElementRef,
                protected cdr: ChangeDetectorRef,
                protected renderer: Renderer) {
    }

    onDocumentMouseDown = (event: any) => {
        const element = this.element.nativeElement;
        if (!element || !this.popover) { return; }
        if (element.contains(event.target) || this.popover.getElement().contains(event.target)) { return; }
        this.hide();
        this.onCloseFromOutside.emit(undefined);
    }

    ngAfterViewInit(): void {
        if ( this.buttons ) {
            this.buttonsNames = Object.keys(this.buttons);
        }
        if (this.closeOnClickOutside) {
            this.listenClickFunc = this.renderer.listenGlobal('document', 'mousedown', (event: any) => this.onDocumentMouseDown(event));
        }
        if (this.closeOnMouseOutside) {
            this.listenMouseFunc = this.renderer.listenGlobal('document', 'mouseover', (event: any) => this.onDocumentMouseDown(event));
        }

        this.cdr.detectChanges();
    }

    ngOnDestroy() {
        if (this.closeOnClickOutside) {
            this.listenClickFunc();
        }
        if (this.closeOnMouseOutside) {
            this.listenMouseFunc();
        }
    }

    // -------------------------------------------------------------------------
    // Public Methods
    // -------------------------------------------------------------------------

    show(): void {
        if (!this.popover || !this.popover.getElement()) {
            return;
        }

        const p = this.positionElements(this.popover.getElement(), this.popoverDiv.nativeElement, this.placement);
        this.displayType = 'block';
        this.top = p.top;
        this.left = p.left;
        this.isIn = true;
    }

    hide(): void {
        this.top = -10000;
        this.left = -10000;
        this.isIn = true;
        this.popover.hide();
    }

    hideFromPopover() {
        this.displayType = 'none';
        this.isIn = true;
    }

    // -------------------------------------------------------------------------
    // Protected Methods
    // -------------------------------------------------------------------------

    protected positionElements(hostEl: HTMLElement, targetEl: HTMLElement, positionStr: string, appendToBody: boolean = false): { top: number, left: number } {
        const positionStrParts = positionStr.split('-');
        let pos0 = positionStrParts[0];
        const pos1 = positionStrParts[1] || 'center';
        const hostElPos = appendToBody ? this.offset(hostEl) : this.position(hostEl);
        const targetElWidth = targetEl.offsetWidth;
        const targetElHeight = targetEl.offsetHeight;

        this.effectivePlacement = pos0 = this.getEffectivePlacement(pos0, hostEl, targetEl);

        const shiftWidth: any = {
            center(): number {
                return hostElPos.left + hostElPos.width / 2 - targetElWidth / 2;
            },
            left(): number {
                return hostElPos.left;
            },
            right(): number {
                return hostElPos.left + hostElPos.width - targetElWidth;
            }
        };

        const shiftHeight: any = {
            center(): number {
                return hostElPos.top + hostElPos.height / 2 - targetElHeight / 2;
            },
            top(): number {
                return hostElPos.top;
            },
            bottom(): number {
                return hostElPos.top + hostElPos.height - targetElHeight;
            }
        };

        let targetElPos: { top: number, left: number };
        switch (pos0) {
            case 'right':
                targetElPos = {
                    top: shiftHeight[pos1](),
                    left: hostElPos.left + hostElPos.width
                };
                break;

            case 'left':
                targetElPos = {
                    top: shiftHeight[pos1](),
                    left: hostElPos.left - targetElWidth
                };
                break;

            case 'bottom':
                targetElPos = {
                    top: hostElPos.top + hostElPos.height,
                    left: shiftWidth[pos1]()
                };
                break;

            default:
                targetElPos = {
                    top: hostElPos.top - targetElHeight,
                    left: shiftWidth[pos1]()
                };
                break;
        }

        return targetElPos;
    }

    protected position(nativeEl: HTMLElement): { width: number, height: number, top: number, left: number } {
        let offsetParentBCR = { top: 0, left: 0 };
        const elBCR = this.offset(nativeEl);
        const offsetParentEl = this.parentOffsetEl(nativeEl);
        if (offsetParentEl !== window.document) {
            offsetParentBCR = this.offset(offsetParentEl);
            offsetParentBCR.top += offsetParentEl.clientTop - offsetParentEl.scrollTop;
            offsetParentBCR.left += offsetParentEl.clientLeft - offsetParentEl.scrollLeft;
        }

        const boundingClientRect = nativeEl.getBoundingClientRect();
        return {
            width: boundingClientRect.width || nativeEl.offsetWidth,
            height: boundingClientRect.height || nativeEl.offsetHeight,
            top: elBCR.top - offsetParentBCR.top,
            left: elBCR.left - offsetParentBCR.left
        };
    }

    protected offset(nativeEl: any): { width: number, height: number, top: number, left: number } {
        const boundingClientRect = nativeEl.getBoundingClientRect();
        return {
            width: boundingClientRect.width || nativeEl.offsetWidth,
            height: boundingClientRect.height || nativeEl.offsetHeight,
            top: boundingClientRect.top + (window.pageYOffset || window.document.documentElement.scrollTop),
            left: boundingClientRect.left + (window.pageXOffset || window.document.documentElement.scrollLeft)
        };
    }

    protected getStyle(nativeEl: HTMLElement, cssProp: string): string {
        if ((nativeEl as any).currentStyle) { // IE
            return (nativeEl as any).currentStyle[cssProp];
        }

        if (window.getComputedStyle) {
            return (window.getComputedStyle as any)(nativeEl)[cssProp];
        }

        // finally try and get inline style
        return (nativeEl.style as any)[cssProp];
    }

    protected isStaticPositioned(nativeEl: HTMLElement): boolean {
        return (this.getStyle(nativeEl, 'position') || 'static' ) === 'static';
    }

    protected parentOffsetEl(nativeEl: HTMLElement): any {
        let offsetParent: any = nativeEl.offsetParent || window.document;
        while (offsetParent && offsetParent !== window.document && this.isStaticPositioned(offsetParent)) {
            offsetParent = offsetParent.offsetParent;
        }
        return offsetParent || window.document;
    }

    protected getEffectivePlacement(placement: string, hostElement: HTMLElement, targetElement: HTMLElement): string {
        const placementParts = placement.split(' ');
        if (placementParts[0] !== 'auto') {
            return placement;
        }

        const hostElBoundingRect = hostElement.getBoundingClientRect();

        const desiredPlacement = placementParts[1] || 'bottom';

        if (desiredPlacement === 'top' && hostElBoundingRect.top - targetElement.offsetHeight < 0) {
            return 'bottom';
        }
        if (desiredPlacement === 'bottom' && hostElBoundingRect.bottom + targetElement.offsetHeight > window.innerHeight) {
            return 'top';
        }
        if (desiredPlacement === 'left' && hostElBoundingRect.left - targetElement.offsetWidth < 0) {
            return 'right';
        }
        if (desiredPlacement === 'right' && hostElBoundingRect.right + targetElement.offsetWidth > window.innerWidth) {
            return 'left';
        }

        return desiredPlacement;
    }
}
