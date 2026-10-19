/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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
import {AfterViewInit, Directive, ElementRef, HostListener, Input, OnChanges, OnDestroy, Renderer2} from '@angular/core';
import {Subscription} from 'rxjs/Subscription';
import {GlossaryService} from './glossary.service';

const SHOW_DELAY_MS = 300;
const FOCUSABLE = 'button, a[href], select, textarea, input:not([type="hidden"]), [tabindex]:not([tabindex="-1"])';
const UNDERLINE_CLASS = 'sdc-term';
const TOOLTIP_CLASS = 'sdc-glossary-tooltip';

/**
 * Explains a glossary term on the element that shows it. Contract the test suites depend on: the
 * host's text nodes are never touched and no tabindex is added (Selenium reads badge text exactly,
 * and keyboard-navigation.spec.ts pins the tab order). The tooltip is appended to <body> so tile
 * and table overflow cannot clip it.
 */
@Directive({selector: '[sdcTerm]'})
export class GlossaryTermDirective implements OnChanges, AfterViewInit, OnDestroy {
    @Input('sdcTerm') term: string;
    @Input() sdcTermUnderline: boolean = true;

    private viewReady: boolean = false;
    private describedElement: HTMLElement = null;
    private appliedId: string = null;
    private tooltipElement: HTMLElement = null;
    private showTimer: any = null;
    private removeListeners: Array<() => void> = [];
    private languageSubscription: Subscription = null;

    constructor(private host: ElementRef, private renderer: Renderer2, private glossary: GlossaryService) {
    }

    // The first ngOnChanges runs before a host component (e.g. sdc-button) has rendered its inner
    // <button>, so attributes are first applied in ngAfterViewInit.
    ngOnChanges(): void {
        if (this.viewReady) {
            this.hide();
            this.applyAttributes();
        }
    }

    ngAfterViewInit(): void {
        this.viewReady = true;
        this.applyAttributes();
        // Focus that lands before en_US.json has loaded finds no text to show.
        this.languageSubscription = this.glossary.changed.subscribe(() => {
            if (this.host.nativeElement.contains(document.activeElement)) {
                this.show();
            }
        });
    }

    ngOnDestroy(): void {
        if (this.languageSubscription) {
            this.languageSubscription.unsubscribe();
        }
        this.hide();
        this.removeAttributes();
    }

    @HostListener('mouseenter')
    onMouseEnter(): void {
        if (!this.glossary.isKnown(this.term) || this.tooltipElement || this.showTimer) {
            return;
        }
        this.showTimer = setTimeout(() => {
            this.showTimer = null;
            this.show();
        }, SHOW_DELAY_MS);
    }

    @HostListener('mouseleave')
    onMouseLeave(): void {
        this.hide();
    }

    @HostListener('focusin')
    onFocusIn(): void {
        this.show();
    }

    @HostListener('focusout')
    onFocusOut(): void {
        this.hide();
    }

    private show(): void {
        this.clearTimer();
        if (this.tooltipElement) {
            return;
        }
        const text = this.glossary.tooltipText(this.term);
        if (!text) {
            return;
        }
        const tooltip: HTMLElement = this.renderer.createElement('div');
        this.renderer.addClass(tooltip, TOOLTIP_CLASS);
        this.renderer.setAttribute(tooltip, 'role', 'tooltip');
        this.renderer.setAttribute(tooltip, 'aria-hidden', 'true');
        this.renderer.appendChild(tooltip, this.renderer.createText(text));
        this.renderer.appendChild(document.body, tooltip);
        this.tooltipElement = tooltip;
        this.position(tooltip);

        this.removeListeners.push(this.renderer.listen('document', 'keydown', (event: KeyboardEvent) => {
            if (event.key === 'Escape' || event.key === 'Esc') {
                this.hide();
            }
        }));
        const onScroll = () => this.hide();
        document.addEventListener('scroll', onScroll, true);
        this.removeListeners.push(() => document.removeEventListener('scroll', onScroll, true));
    }

    private hide(): void {
        this.clearTimer();
        this.removeListeners.forEach((remove) => remove());
        this.removeListeners = [];
        if (this.tooltipElement) {
            if (this.tooltipElement.parentNode) {
                this.tooltipElement.parentNode.removeChild(this.tooltipElement);
            }
            this.tooltipElement = null;
        }
    }

    private clearTimer(): void {
        if (this.showTimer) {
            clearTimeout(this.showTimer);
            this.showTimer = null;
        }
    }

    private position(tooltip: HTMLElement): void {
        const rect = (this.host.nativeElement as HTMLElement).getBoundingClientRect();
        const scrollX = window.pageXOffset;
        const scrollY = window.pageYOffset;
        const maxLeft = scrollX + document.documentElement.clientWidth - tooltip.offsetWidth - 4;
        const left = Math.max(scrollX + 4, Math.min(rect.left + scrollX, maxLeft));
        // <body> does not scroll in this app, so a tooltip below a host near the bottom edge is lost.
        const fitsBelow = rect.bottom + 4 + tooltip.offsetHeight <= document.documentElement.clientHeight;
        const top = fitsBelow || rect.top < tooltip.offsetHeight + 4 ? rect.bottom + 4 : rect.top - tooltip.offsetHeight - 4;
        this.renderer.setStyle(tooltip, 'top', (top + scrollY) + 'px');
        this.renderer.setStyle(tooltip, 'left', left + 'px');
    }

    private applyAttributes(): void {
        this.removeAttributes();
        const id = this.glossary.descriptionId(this.term);
        if (!id) {
            return;
        }
        const target = this.describedTarget();
        const existing = target.getAttribute('aria-describedby');
        this.renderer.setAttribute(target, 'aria-describedby', existing ? existing + ' ' + id : id);
        this.describedElement = target;
        this.appliedId = id;
        if (this.sdcTermUnderline) {
            this.renderer.addClass(this.host.nativeElement, UNDERLINE_CLASS);
        }
    }

    private removeAttributes(): void {
        if (this.describedElement) {
            const remaining = (this.describedElement.getAttribute('aria-describedby') || '')
                .split(' ')
                .filter((id) => id && id !== this.appliedId);
            if (remaining.length) {
                this.renderer.setAttribute(this.describedElement, 'aria-describedby', remaining.join(' '));
            } else {
                this.renderer.removeAttribute(this.describedElement, 'aria-describedby');
            }
            this.describedElement = null;
            this.appliedId = null;
        }
        this.renderer.removeClass(this.host.nativeElement, UNDERLINE_CLASS);
    }

    private describedTarget(): HTMLElement {
        const host: HTMLElement = this.host.nativeElement;
        if (host.matches(FOCUSABLE)) {
            return host;
        }
        return (host.querySelector(FOCUSABLE) as HTMLElement) || host;
    }
}
