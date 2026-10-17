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

import {TileComponent} from './tile.component';

describe('TileComponent accessible name', () => {
    const sdcMenu: any = {};

    function tile(overrides: any): TileComponent {
        const component: any = Object.assign({
            name: 'basic_cnf_macro_gnb',
            version: '0.1',
            model: undefined,
            isResource: () => true,
            isService: () => false,
            getComponentSubType: () => 'VF',
            getStatus: () => 'In Design'
        }, overrides);
        const comp = new TileComponent(sdcMenu);
        comp.component = component;
        return comp;
    }

    it('names a resource by name, type, version and status', () => {
        expect(tile({}).accessibleName).toBe('Basic_cnf_macro_gnb, VF, version 0.1, In Design');
    });

    it('tells apart two assets that share a name', () => {
        const vfc = tile({version: '1.0', getComponentSubType: () => 'VFC', getStatus: () => 'Certified'});
        expect(vfc.accessibleName).toBe('Basic_cnf_macro_gnb, VFC, version 1.0, Certified');
        expect(vfc.accessibleName).not.toBe(tile({}).accessibleName);
    });

    it('spells out the service type the badge abbreviates to S', () => {
        expect(tile({name: 'vFW', isResource: () => false, isService: () => true, getComponentSubType: () => 'SERVICE'})
            .accessibleName).toBe('VFW, Service, version 0.1, In Design');
    });

    it('includes a non-default model', () => {
        expect(tile({model: 'ETSI SOL001 v2.5.1'}).accessibleName)
            .toBe('Basic_cnf_macro_gnb, VF, version 0.1, model ETSI SOL001 v2.5.1, In Design');
    });

    it('names a data type without version or status', () => {
        expect(tile({name: 'org.openecomp.datatypes.Foo', isResource: () => false, isService: () => false,
            getComponentSubType: () => 'DATATYPE', getStatus: undefined}).accessibleName)
            .toBe('Datatypes.Foo, DATATYPE');
    });
});

describe('TileComponent template', () => {
    // tslint:disable-next-line:no-var-requires
    const template: string = require('./tile.component.html');
    const doc = new DOMParser().parseFromString(`<body>${template}</body>`, 'text/html');

    it('names the icon button from the full description', () => {
        expect(doc.querySelector('button.sdc-tile-content-icon').getAttribute('[attr.aria-label]')).toBe('accessibleName');
    });

    it('hides the text the button name already carries', () => {
        ['.sdc-tile-header', '.sdc-tile-content-info', '.sdc-tile-footer'].forEach((selector) =>
            expect(doc.querySelector(selector).getAttribute('aria-hidden')).toBe('true'));
    });
});
