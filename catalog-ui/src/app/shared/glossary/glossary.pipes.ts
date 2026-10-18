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
import {Pipe, PipeTransform} from '@angular/core';
import {GlossaryService} from './glossary.service';

/** Impure so that a badge rendered before en_US.json arrives switches to the long name once it does. */
@Pipe({name: 'glossaryName', pure: false})
export class GlossaryNamePipe implements PipeTransform {
    constructor(private glossary: GlossaryService) {
    }

    transform(term: string): string {
        return this.glossary.displayName(term);
    }
}

@Pipe({name: 'isGlossaryTerm'})
export class IsGlossaryTermPipe implements PipeTransform {
    constructor(private glossary: GlossaryService) {
    }

    transform(term: string): boolean {
        return this.glossary.isKnown(term);
    }
}
