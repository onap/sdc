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

import { Component, Input, ContentChildren, SimpleChanges, QueryList } from '@angular/core';
import { MenuItemComponent } from "./menu-item.component";
import { Point } from "app/models";

@Component({
    selector: 'menu-list',
    templateUrl: './menu-list.component.html',
    styleUrls:['./menu-list.component.less']
})
export class MenuListComponent {
    @Input('open') inputOpen:boolean = false;
    @Input('position') inputPosition:Point = new Point();
    @Input() styleClass:any;

    @ContentChildren(MenuItemComponent) menuItems:QueryList<MenuItemComponent>;

    private position:Point;
    private isOpen:boolean = false;

    constructor() {
    }

    ngOnChanges(changes:SimpleChanges) {
        if (changes.inputOpen) {
            (changes.inputOpen.currentValue) ? this.open() : this.close();
        }
        if (changes.inputPosition) {
            this.changePosition(changes.inputPosition.currentValue);
        }
    }

    ngAfterContentInit() {
        this.menuItems.forEach((c) => c.parentMenu = this);
        this.menuItems.changes.subscribe((list) => {
            list.forEach((c) => c.parentMenu = this);
        });
    }

    open(): void {
        this.isOpen = true;
    }

    close(): void {
        this.isOpen = false;
    }

    changePosition(position:Point) {
        this.position = position;
    }
}
