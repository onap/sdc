/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2026 Deutsche Telekom AG
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
import {IconsModalComponent} from './icons-modal.component';

describe('IconsModalComponent', () => {
    let comp: IconsModalComponent;
    let availableIconsServiceMock: any;

    const makeComponent = (over: any = {}) => ({
        icon: 'router',
        iconSprite: 'sprite-resource-icons',
        componentType: 'RESOURCE',
        vendorName: 'ATT',
        categories: [{icons: ['catA'], subcategories: [{icons: ['subB']}]}],
        isResource: () => over.isResource !== undefined ? over.isResource : true,
        getComponentSubType: () => over.subType || 'VF',
        ...over
    });

    beforeEach(() => {
        availableIconsServiceMock = {getIcons: jest.fn(() => ['a', 'b', 'c', 'd', 'e', 'att', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'nokiasiemens', 'p', 'q', 'r', 's'])};
        comp = new IconsModalComponent(availableIconsServiceMock);
    });

    it('collects category + subcategory icons and appends the default icon', () => {
        comp.component = makeComponent({vendorName: 'unknownvendor'}) as any;
        comp.ngOnInit();
        expect(comp.icons).toContain('catA');
        expect(comp.icons).toContain('subB');
        expect(comp.icons[comp.icons.length - 1]).toBe('defaulticon');
        expect(comp.selectedIcon).toBe('router');
        expect(comp.iconSprite).toBe('sprite-resource-icons');
    });

    it('uses only [vl] for a VL resource and [cp] for a CP resource', () => {
        comp.component = makeComponent({subType: 'VL', vendorName: 'x'}) as any;
        comp.ngOnInit();
        expect(comp.icons).toEqual(['vl', 'defaulticon']);

        comp.component = makeComponent({subType: 'CP', vendorName: 'x'}) as any;
        comp.ngOnInit();
        expect(comp.icons).toEqual(['cp', 'defaulticon']);
    });

    it('resolves the vendor icon (att) and adds it when present', () => {
        comp.component = makeComponent({categories: [], vendorName: 'AT&T'}) as any;
        comp.ngOnInit();
        expect(comp.icons).toContain('att');
    });

    it('updateIcon returns true and mutates component.icon when the selection changed', () => {
        comp.component = makeComponent() as any;
        comp.ngOnInit();
        comp.changeIcon('newicon');
        const dirty = comp.updateIcon();
        expect(dirty).toBe(true);
        expect(comp.component.icon).toBe('newicon');
    });

    it('updateIcon returns false when the selection is unchanged', () => {
        comp.component = makeComponent() as any;
        comp.ngOnInit();
        expect(comp.updateIcon()).toBe(false);
    });
});
