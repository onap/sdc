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
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs';
import {formatTooltip, GlossaryEntry, GlossaryService} from './glossary.service';

/**
 * Rendered once in the app shell. `[sdcTerm]` points aria-describedby at these elements, so ids are
 * per term rather than per use — fifty catalog tiles share one description each instead of
 * producing fifty duplicate ids.
 */
@Component({
    selector: 'sdc-glossary-descriptions',
    template: `<div class="sdc-glossary-descriptions" hidden>
        <span *ngFor="let entry of entries; trackBy: byKey" [attr.id]="entry.descriptionId">{{ tooltip(entry) }}</span>
    </div>`
})
export class GlossaryDescriptionsComponent implements OnInit, OnDestroy {
    entries: GlossaryEntry[] = [];
    private subscription: Subscription;

    constructor(private glossary: GlossaryService) {
    }

    ngOnInit(): void {
        this.subscription = this.glossary.changed.subscribe(() => this.entries = this.glossary.all());
    }

    ngOnDestroy(): void {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    tooltip(entry: GlossaryEntry): string {
        return formatTooltip(entry);
    }

    byKey(index: number, entry: GlossaryEntry): string {
        return entry.key;
    }
}
