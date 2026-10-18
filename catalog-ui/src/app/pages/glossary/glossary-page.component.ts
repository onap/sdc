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
import {CacheService} from '../../services/cache.service';
import {GlossaryEntry, GlossaryService} from '../../shared/glossary/glossary.service';

@Component({
    selector: 'glossary-page',
    templateUrl: './glossary-page.component.html',
    styleUrls: ['./glossary-page.component.less']
})
export class GlossaryPageComponent implements OnInit, OnDestroy {
    entries: GlossaryEntry[] = [];
    filterText: string = '';
    version: string;
    private subscription: Subscription;

    constructor(private glossary: GlossaryService, private cacheService: CacheService) {
    }

    ngOnInit(): void {
        this.version = this.cacheService.get('version');
        this.subscription = this.glossary.changed.subscribe(() => this.entries = this.glossary.all());
    }

    ngOnDestroy(): void {
        if (this.subscription) {
            this.subscription.unsubscribe();
        }
    }

    get visibleEntries(): GlossaryEntry[] {
        const needle = (this.filterText || '').trim().toLowerCase();
        if (!needle) {
            return this.entries;
        }
        return this.entries.filter((e) =>
            [e.abbr || '', e.name, e.description].some((text) => text.toLowerCase().indexOf(needle) !== -1));
    }
}
