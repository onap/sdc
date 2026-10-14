/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import { AfterViewChecked, Directive, ElementRef, HostListener, Input, Renderer2 } from '@angular/core';

const SCROLLING = ['auto', 'scroll'];

/**
 * Keyboard and screen-reader support for an ngx-datatable 14, which has none of its own and cannot
 * be upgraded on Angular 5. It patches the library's DOM, so it only sets attributes.
 *
 * aria-sort would need grid or table roles on every row and cell, which v14 re-renders, so the sort
 * state goes into the header button's name instead.
 */
@Directive({selector: 'ngx-datatable[sdcDatatableA11y]'})
export class DatatableA11yDirective implements AfterViewChecked {
    /** Name of the table body while it is a scrollable region. */
    @Input() sdcDatatableA11y: string;

    constructor(private el: ElementRef, private renderer: Renderer2) {
    }

    ngAfterViewChecked(): void {
        const host: HTMLElement = this.el.nativeElement;
        Array.from(host.querySelectorAll('.datatable-header-cell.sortable')).forEach((cell: HTMLElement) => {
            const label = cell.querySelector('.datatable-header-cell-label') as HTMLElement;
            if (label && label.textContent.trim()) {
                this.decorateLabel(label, cell);
            }
            const sortBtn = cell.querySelector('.sort-btn');
            if (sortBtn && !sortBtn.hasAttribute('aria-hidden')) {
                this.renderer.setAttribute(sortBtn, 'aria-hidden', 'true');
            }
        });
        const body = host.querySelector('datatable-body') as HTMLElement;
        if (body) {
            this.decorateBody(body);
        }
    }

    @HostListener('keydown', ['$event'])
    onKeydown(event: KeyboardEvent): void {
        const target = event.target as HTMLElement;
        if (event.repeat || !target.classList.contains('datatable-header-cell-label')
            || target.getAttribute('role') !== 'button') {
            return;
        }
        if (event.key === 'Enter' || event.key === ' ' || event.key === 'Spacebar') {
            event.preventDefault();
            target.click();
        }
    }

    private decorateLabel(label: HTMLElement, cell: HTMLElement): void {
        if (label.getAttribute('role') !== 'button') {
            this.renderer.setAttribute(label, 'role', 'button');
            this.renderer.setAttribute(label, 'tabindex', '0');
        }
        const dir = cell.classList.contains('sort-asc') ? 'ascending'
            : cell.classList.contains('sort-desc') ? 'descending' : null;
        const name = dir ? `${label.textContent.trim()}, sorted ${dir}` : null;
        this.setOrRemove(label, 'aria-label', name);
    }

    private decorateBody(body: HTMLElement): void {
        const style = window.getComputedStyle(body);
        const scrolls = (SCROLLING.indexOf(style.overflowY) !== -1 && body.scrollHeight > body.clientHeight)
            || (SCROLLING.indexOf(style.overflowX) !== -1 && body.scrollWidth > body.clientWidth);
        this.setOrRemove(body, 'tabindex', scrolls ? '0' : null);
        this.setOrRemove(body, 'role', scrolls ? 'region' : null);
        this.setOrRemove(body, 'aria-label', scrolls ? (this.sdcDatatableA11y || 'Table') : null);
    }

    private setOrRemove(el: HTMLElement, attr: string, value: string): void {
        if (value === null) {
            if (el.hasAttribute(attr)) {
                this.renderer.removeAttribute(el, attr);
            }
        } else if (el.getAttribute(attr) !== value) {
            this.renderer.setAttribute(el, attr, value);
        }
    }
}
