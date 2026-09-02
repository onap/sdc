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

/**
 * Characterization spec pinning ValidationUtils' behaviour across the AngularJS -> Angular
 * migration (SDC-4829 Phase 11). The six regex patterns used to be injected as AngularJS
 * `.value()` tokens (registered once in app.ts); they are now inlined class constants and the
 * service is a plain @Injectable with a no-arg constructor. These tests lock the exact matching
 * behaviour of each inlined pattern so the migration cannot silently change validation.
 */
import {ValidationUtils} from './validation-utils';

describe('ValidationUtils', () => {

    let service: ValidationUtils;

    beforeEach(() => {
        // No-arg constructor: proves the @Injectable no longer depends on injected regex tokens.
        service = new ValidationUtils();
    });

    describe('getValidationPattern', () => {

        it('returns the integer (no-leading-zero) pattern for "integer"', () => {
            const p = service.getValidationPattern('integer');
            expect(p.test('0')).toBe(true);
            expect(p.test('123')).toBe(true);
            expect(p.test('-42')).toBe(true);
            expect(p.test('0x1A')).toBe(true);
            expect(p.test('0o17')).toBe(true);
            expect(p.test('01')).toBe(false);      // leading zero rejected
            expect(p.test('1.5')).toBe(false);
        });

        it('returns the float pattern for "float"', () => {
            const p = service.getValidationPattern('float');
            expect(p.test('1.5')).toBe(true);
            expect(p.test('-0.3')).toBe(true);
            expect(p.test('2e10')).toBe(true);
            expect(p.test('3f')).toBe(true);
            expect(p.test('abc')).toBe(false);
        });

        it('returns the number pattern for "number"', () => {
            const p = service.getValidationPattern('number');
            expect(p.test('42')).toBe(true);
            expect(p.test('-7')).toBe(true);
            expect(p.test('0x1F')).toBe(true);
            expect(p.test('1.25')).toBe(true);
            expect(p.test('1.2.3')).toBe(false);
        });

        it('returns the comment/string pattern for "string" and accepts the U+0000..U+00BF range', () => {
            const p = service.getValidationPattern('string');
            expect(p.test('hello world')).toBe(true);
            expect(p.test('')).toBe(true);
            // Lower bound U+0000 and upper bound U+00BF must both be accepted (regression guard for
            // the CommentValidationPattern range copied verbatim from app.ts).
            expect(p.test(String.fromCharCode(0x00))).toBe(true);
            expect(p.test(String.fromCharCode(0xBF))).toBe(true);
            // Just past the upper bound is rejected.
            expect(p.test(String.fromCharCode(0xC0))).toBe(false);
        });

        it('returns the plain boolean pattern for "boolean" without a parameterType', () => {
            const p = service.getValidationPattern('boolean');
            expect(p.test('true')).toBe(true);
            expect(p.test('FALSE')).toBe(true);
            expect(p.test('yes')).toBe(false);
            expect(p.test('1')).toBe(false);
        });

        it('returns the plain boolean pattern for "boolean" when parameterType is not "heat"', () => {
            const p = service.getValidationPattern('boolean', 'tosca');
            expect(p.test('true')).toBe(true);
            expect(p.test('y')).toBe(false);
        });

        it('returns the permissive heat-boolean pattern for "boolean" when parameterType === "heat"', () => {
            const p = service.getValidationPattern('boolean', 'heat');
            // Heat accepts the broader true/false vocabulary (DE197437).
            expect(p.test('true')).toBe(true);
            expect(p.test('y')).toBe(true);
            expect(p.test('yes')).toBe(true);
            expect(p.test('on')).toBe(true);
            expect(p.test('1')).toBe(true);
            expect(p.test('n')).toBe(true);
            expect(p.test('off')).toBe(true);
            expect(p.test('0')).toBe(true);
            expect(p.test('maybe')).toBe(false);
        });

        it('returns the label pattern for "label" and "category"', () => {
            const label = service.getValidationPattern('label');
            const category = service.getValidationPattern('category');
            expect(label).toEqual(category); // both map to LabelValidationPattern
            expect(label.test('My Label 1')).toBe(true);
            expect(label.test('a'.repeat(26))).toBe(false); // >25 chars rejected
            expect(label.test('has_underscore')).toBe(false); // underscore not allowed
        });

        it('returns null for an unknown validation type', () => {
            expect(service.getValidationPattern('nonsense')).toBeNull();
        });
    });

    describe('validateJson', () => {
        it('returns true for valid JSON', () => {
            expect(service.validateJson('{"a":1}')).toBe(true);
            expect(service.validateJson('[1,2,3]')).toBe(true);
        });
        it('returns false for invalid JSON', () => {
            expect(service.validateJson('{a:1}')).toBe(false);
            expect(service.validateJson('not json')).toBe(false);
        });
    });

    describe('validateIntRange', () => {
        it('accepts integers within the signed-32-bit range', () => {
            expect(service.validateIntRange('100')).toBe(true);
            expect(service.validateIntRange('-100')).toBe(true);
            expect(service.validateIntRange('0x7F')).toBe(true);
        });
        it('rejects integers outside the signed-32-bit range', () => {
            expect(service.validateIntRange('9999999999')).toBe(false);
        });
    });

    describe('stripAndSanitize', () => {
        it('returns null for empty input', () => {
            expect(service.stripAndSanitize('')).toBeNull();
            expect(service.stripAndSanitize(null)).toBeNull();
        });
        it('collapses whitespace, strips percent-escapes and HTML-escapes special chars', () => {
            expect(service.stripAndSanitize('a   b')).toBe('a b');
            expect(service.stripAndSanitize('a%20b')).toBe('ab');
            expect(service.stripAndSanitize('<b>&"\'')).toBe('&lt;b&gt;&amp;&quot;&apos;');
            expect(service.stripAndSanitize('  trim  ')).toBe('trim');
        });
    });

    describe('static pattern helpers', () => {
        it('getPropertyListPatterns pins list-style validators', () => {
            const pats = ValidationUtils.getPropertyListPatterns();
            expect(pats.integer.test('1,2,3')).toBe(true);
            expect(pats.integer.test('')).toBe(true);
            expect(pats.boolean.test('true,false')).toBe(true);
            expect(pats.float.test('1.1,2.2')).toBe(true);
        });

        it('getPropertyMapPatterns pins map-style validators', () => {
            const pats = ValidationUtils.getPropertyMapPatterns();
            expect(pats.integer.test('"a":1')).toBe(true);
            expect(pats.boolean.test('"a":true')).toBe(true);
            expect(pats.integer.test('')).toBe(true);
        });

        it('validateUniqueKeys accepts unique keys and rejects duplicates', () => {
            expect(ValidationUtils.validateUniqueKeys('')).toBe(true); // empty allowed
            expect(ValidationUtils.validateUniqueKeys('"a":1,"b":2')).toBe(true);
            expect(ValidationUtils.validateUniqueKeys('"a":1,"a":2')).toBe(false); // duplicate key
            expect(ValidationUtils.validateUniqueKeys('not valid json')).toBe(false);
        });
    });
});
