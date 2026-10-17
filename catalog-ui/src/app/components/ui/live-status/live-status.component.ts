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

import {Component, ElementRef, Input, NgZone, OnChanges, OnDestroy, ViewChild} from '@angular/core';

export function resultCount(count: number): string {
    return count === 0 ? 'No results' : count === 1 ? '1 result' : count + ' results';
}

/**
 * A persistent polite live region. It has to exist before its text changes — a region inserted
 * together with its message is not announced — so it is always rendered and only its text moves.
 *
 * `message` changes are debounced by `delay` so typing into a filter announces the settled result
 * once. A null message is "not known yet" and is never announced; with `announceInitial` false the
 * first known message is written silently, so a page load is not read out as if it were a search.
 * The timer runs outside the Angular zone and writes the text directly: an in-zone timer would hold
 * Testability unstable, and the Selenium suite's waitForAngular would wait it out on every keystroke.
 */
@Component({
    selector: 'sdc-live-status',
    template: '<div class="sr-only" role="status" #region></div>'
})
export class LiveStatusComponent implements OnChanges, OnDestroy {
    @Input() message: string = null;
    @Input() delay: number = 0;
    @Input() announceInitial: boolean = true;
    @ViewChild('region') region: ElementRef;

    private known: boolean = false;
    private timer: any = null;

    constructor(private zone: NgZone) {
    }

    ngOnChanges(): void {
        if (this.message === null || this.message === undefined) {
            return;
        }
        if (!this.known && !this.announceInitial) {
            this.known = true;
            return;
        }
        this.known = true;
        this.cancel();
        const message = this.message;
        this.zone.runOutsideAngular(() => {
            this.timer = setTimeout(() => {
                this.timer = null;
                this.write(message);
            }, this.delay);
        });
    }

    ngOnDestroy(): void {
        this.cancel();
    }

    /** A repeated message gets a trailing no-break space, because a region whose text does not change is not re-read. */
    private write(text: string): void {
        if (!this.region) { return; }
        const element: HTMLElement = this.region.nativeElement;
        element.textContent = element.textContent === text ? text + '\u00a0' : text;
    }

    private cancel(): void {
        if (this.timer) {
            clearTimeout(this.timer);
            this.timer = null;
        }
    }
}
