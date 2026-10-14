/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

import { Component, ContentChildren, QueryList, AfterContentInit, Input, Output, EventEmitter } from '@angular/core';
import { Tab } from './tab/tab.component';
import { ViewEncapsulation } from '@angular/core';
import { trigger, state, style, transition, animate, keyframes } from '@angular/animations';

export {Tab};

@Component({
    selector: 'tabs',
    templateUrl: './tabs.component.html',
    styleUrls: ['./tabs.component.less'],
    encapsulation: ViewEncapsulation.None,
    animations: [
        trigger('indicatorAnimation', [
            transition(':enter', [style({ transform: 'translateY(-50%)', opacity: 0 }), animate('250ms', style({ transform: 'translateY(0)', opacity: 1 })) ]),
            transition(':leave', [style({ opacity: 1 }), animate('500ms', style({ opacity: 0 })) ])
        ])
    ]
})
export class Tabs implements AfterContentInit {

    @Input() tabStyle: string;
    @Input() hideIndicationOnTabChange?: boolean = false;
    @ContentChildren(Tab) tabs: QueryList<Tab>;
    @Output() tabChanged: EventEmitter<Tab> = new EventEmitter<Tab>();


    ngAfterContentInit() {  
        //after contentChildren are set, determine active tab. If no active tab set, activate the first one
        let activeTabs = this.tabs.filter((tab) => tab.active);
 
        if (activeTabs.length === 0) {
            this.selectTab(this.tabs.first);
        }
    }

    selectTab(tab: Tab) {
        //activate the tab the user clicked.
        this.tabs.toArray().forEach(tab => {
            tab.active = false;
            if (this.hideIndicationOnTabChange && tab.indication) {
                tab.indication = null;
            }
        });
        tab.active = true;
        this.tabChanged.emit(tab);
    }

    /**
     * Manual activation: arrows, Home and End only move focus; Enter or Space selects through the
     * native button's click, except when held or on the active tab. Selecting on arrow or re-selecting
     * would run the consumers' (tabChanged) guards, which open an unsaved-changes modal.
     */
    onTabKeydown(event: KeyboardEvent, tab: Tab) {
        if (event.target !== event.currentTarget || event.altKey || event.ctrlKey || event.metaKey) {
            return;
        }
        if ((event.key === 'Enter' || event.key === ' ' || event.key === 'Spacebar') && (event.repeat || tab.active)) {
            event.preventDefault();
            return;
        }
        const visible: Tab[] = this.tabs.filter((t) => t.show);
        const current: number = visible.indexOf(tab);
        let target: number;
        switch (event.key) {
            case 'ArrowRight':
            case 'Right':
                target = (current + 1) % visible.length;
                break;
            case 'ArrowLeft':
            case 'Left':
                target = (current - 1 + visible.length) % visible.length;
                break;
            case 'Home':
                target = 0;
                break;
            case 'End':
                target = visible.length - 1;
                break;
            default:
                return;
        }
        event.preventDefault();
        const tabList: HTMLElement = (event.currentTarget as HTMLElement).parentElement;
        (tabList.children[target] as HTMLElement).focus();
    }

    triggerTabChange(tabTitle) {
        this.tabs.toArray().forEach(tab => {
            tab.active = (tab.title == tabTitle) ? true : false;
        });
    }

    setTabIndication(tabTitle:string, indication?:number) {
        let selectedTab: Tab = this.tabs.toArray().find(tab => tab.title == tabTitle);
        selectedTab.indication = indication || null;
    }

}
