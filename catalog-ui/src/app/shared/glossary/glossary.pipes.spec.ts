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
import {GlossaryService} from './glossary.service';
import {GlossaryNamePipe, IsGlossaryTermPipe} from './glossary.pipes';
import {translateServiceStub} from './glossary.testing';

describe('glossary pipes', () => {
    const service = new GlossaryService(translateServiceStub());

    it('glossaryName shows the long name, or the raw value', () => {
        const pipe = new GlossaryNamePipe(service);
        expect(pipe.transform('VF')).toBe('Virtual Function');
        expect(pipe.transform('Data Type')).toBe('Data Type');
    });

    it('isGlossaryTerm tells glossary terms from other values', () => {
        const pipe = new IsGlossaryTermPipe(service);
        expect(pipe.transform('VF')).toBe(true);
        expect(pipe.transform('Network L1-3')).toBe(false);
    });
});
