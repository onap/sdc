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
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {TranslateService} from '../translator/translate.service';
import {descKey, GLOSSARY_TERMS, GlossaryTermDef, nameKey} from './glossary-terms';

export interface GlossaryEntry {
    key: string;
    abbr: string;
    name: string;
    description: string;
    descriptionId: string;
}

export function glossaryDescriptionId(key: string): string {
    return 'sdc-glossary-' + key;
}

export function formatLabel(entry: GlossaryEntry): string {
    return entry.abbr ? entry.name + ' (' + entry.abbr + ')' : entry.name;
}

export function formatTooltip(entry: GlossaryEntry): string {
    return formatLabel(entry) + ': ' + entry.description;
}

/**
 * Text is read from TranslateService on every call rather than cached: en_US.json loads
 * asynchronously, and a value captured before it arrives would stay blank.
 */
@Injectable()
export class GlossaryService {
    private byAlias: {[alias: string]: GlossaryTermDef} = {};

    constructor(private translateService: TranslateService) {
        GLOSSARY_TERMS.forEach((def) => def.aliases.forEach((alias) => this.byAlias[alias] = def));
    }

    get changed(): Observable<string> {
        return this.translateService.languageChangedObservable;
    }

    isKnown(term: string): boolean {
        return !!term && this.byAlias.hasOwnProperty(term);
    }

    lookup(term: string): GlossaryEntry {
        return this.isKnown(term) ? this.entryFor(this.byAlias[term]) : null;
    }

    displayName(term: string): string {
        const entry = this.lookup(term);
        return entry ? entry.name : term;
    }

    label(term: string): string {
        const entry = this.lookup(term);
        return entry ? formatLabel(entry) : term;
    }

    tooltipText(term: string): string {
        const entry = this.lookup(term);
        return entry ? formatTooltip(entry) : null;
    }

    descriptionId(term: string): string {
        return this.isKnown(term) ? glossaryDescriptionId(this.byAlias[term].key) : null;
    }

    all(): GlossaryEntry[] {
        const sortKey = (e: GlossaryEntry) => (e.abbr || e.name).toLowerCase();
        return GLOSSARY_TERMS
            .map((def) => this.entryFor(def))
            .filter((e) => e !== null)
            .sort((a, b) => sortKey(a) < sortKey(b) ? -1 : sortKey(a) > sortKey(b) ? 1 : 0);
    }

    private entryFor(def: GlossaryTermDef): GlossaryEntry {
        const name = this.translateService.translate(nameKey(def.key));
        const description = this.translateService.translate(descKey(def.key));
        if (!name || !description) {
            return null;
        }
        return {key: def.key, abbr: def.abbr, name: name, description: description,
            descriptionId: glossaryDescriptionId(def.key)};
    }
}
