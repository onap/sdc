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
import {of, throwError} from 'rxjs';
import {WorkspaceMode} from 'app/utils/constants';
import {PropertyModel} from 'app/models';
import {WorkspacePropertiesTabComponent} from '../properties-tab.component';

function makeProperty(name: string, overrides: any = {}): PropertyModel {
    return new PropertyModel(Object.assign({
        uniqueId: 'prop-' + name,
        name,
        type: 'string',
        description: name + ' desc'
    }, overrides));
}

function makeComponent(properties?: PropertyModel[]): any {
    return {
        uniqueId: 'comp-1',
        properties,
        // model deleteProperty: removes from component.properties, resolves a thenable (ng.IPromise-like)
        deleteProperty: jest.fn(function (this: any, id: string) {
            this.properties = (this.properties || []).filter((p: PropertyModel) => p.uniqueId !== id);
            return Promise.resolve();
        })
    };
}

function createComp(opts: any = {}) {
    const component = 'component' in opts ? opts.component : makeComponent(opts.properties);
    const workspaceService: any = {
        component,
        getComponentMode: jest.fn(() => opts.mode || WorkspaceMode.EDIT)
    };
    const componentService: any = {
        getComponentProperties: jest.fn(() => of({properties: opts.fetched || []}))
    };
    // openEditPropertyModal returns a thenable; the test can push onto component.properties to
    // simulate the modal's Save (the real modal mutates component.properties via the BE round-trip).
    const modalsHandler: any = {
        openEditPropertyModal: jest.fn(() => Promise.resolve())
    };
    const modalService: any = {openInfoModal: jest.fn()};
    const translateService: any = {translate: jest.fn((k: string) => k)};
    const cdr: any = {detectChanges: jest.fn()};

    const comp = new WorkspacePropertiesTabComponent(
        workspaceService, componentService, modalsHandler, modalService, translateService, cdr);
    return {comp, component, workspaceService, componentService, modalsHandler, modalService, translateService, cdr};
}

describe('WorkspacePropertiesTabComponent', () => {

    it('fetches properties when the component has none cached, then populates the list', () => {
        const fetched = [makeProperty('a'), makeProperty('b')];
        const {comp, componentService, component} = createComp({properties: undefined, fetched});
        comp.ngOnInit();
        expect(componentService.getComponentProperties).toHaveBeenCalledTimes(1);
        expect(component.properties).toBe(fetched);
        expect(comp.filteredProperties).toHaveLength(2);
        expect(comp.isLoading).toBe(false);
    });

    it('does NOT fetch when properties are already present', () => {
        const {comp, componentService} = createComp({properties: [makeProperty('a')]});
        comp.ngOnInit();
        expect(componentService.getComponentProperties).not.toHaveBeenCalled();
        expect(comp.filteredProperties).toHaveLength(1);
    });

    it('clears the loader when the fetch fails', () => {
        const {comp, componentService} = createComp({properties: undefined});
        componentService.getComponentProperties.mockReturnValue(throwError('boom'));
        comp.ngOnInit();
        expect(comp.isLoading).toBe(false);
        expect(comp.filteredProperties).toHaveLength(0);
    });

    it('does nothing when there is no working component', () => {
        const {comp, componentService} = createComp({component: null});
        comp.ngOnInit();
        expect(componentService.getComponentProperties).not.toHaveBeenCalled();
        expect(comp.filteredProperties).toHaveLength(0);
    });

    it('opens the AngularJS edit-property modal via ModalsHandler', () => {
        const {comp, modalsHandler, component} = createComp({properties: [makeProperty('a')]});
        comp.ngOnInit();
        const prop = component.properties[0];
        comp.addOrUpdateProperty(prop);
        expect(modalsHandler.openEditPropertyModal).toHaveBeenCalledTimes(1);
        const args = modalsHandler.openEditPropertyModal.mock.calls[0];
        expect(args[0]).toBe(prop);            // property
        expect(args[1]).toBe(component);       // component
        expect(args[4]).toBe('component');     // propertyOwnerType
        expect(args[5]).toBe('comp-1');        // propertyOwnerId
    });

    // The make-or-break behavior: after the (AngularJS) modal closes on Save, the OnPush component must
    // RELOAD component.properties from the backend (the modal persists via the BE but does not mutate
    // component.properties — see the by-reference note in the component) and detectChanges so the new
    // row shows without a page reload. This guards the Selenium ImportVFCAsset/ImportDCAE add contract.
    it('reloads properties from the backend and detects changes after the modal closes', (done) => {
        const {comp, modalsHandler, componentService, component, cdr} = createComp({properties: [makeProperty('a')]});
        comp.ngOnInit();
        cdr.detectChanges.mockClear();
        componentService.getComponentProperties.mockClear();
        // The modal persisted a new property to the BE (so a re-fetch returns two), then resolves.
        componentService.getComponentProperties.mockReturnValue(of({properties: [makeProperty('a'), makeProperty('b')]}));
        modalsHandler.openEditPropertyModal.mockReturnValue(Promise.resolve());
        comp.addOrUpdateProperty();
        // resolve the modal promise, then the getComponentProperties observable (synchronous `of`)
        Promise.resolve().then(() => {
            expect(componentService.getComponentProperties).toHaveBeenCalledTimes(1);
            expect(component.properties).toHaveLength(2);
            expect(comp.filteredProperties).toHaveLength(2);
            expect(comp.propertiesCount).toBe(2);
            expect(cdr.detectChanges).toHaveBeenCalled();
            done();
        });
    });

    it('does not reload when the modal is dismissed (rejected)', (done) => {
        const {comp, modalsHandler, componentService} = createComp({properties: [makeProperty('a')]});
        comp.ngOnInit();
        componentService.getComponentProperties.mockClear();
        modalsHandler.openEditPropertyModal.mockReturnValue(Promise.reject('dismissed'));
        comp.addOrUpdateProperty();
        Promise.resolve().then(() => {
            expect(componentService.getComponentProperties).not.toHaveBeenCalled();
            done();
        });
    });

    it('deletes a property (confirm callback → deleteProperty) and refreshes the list', (done) => {
        const props = [makeProperty('a'), makeProperty('b')];
        const {comp, modalService, component, cdr} = createComp({properties: props});
        comp.ngOnInit();
        cdr.detectChanges.mockClear();
        comp.delete(component.properties[0]);
        // openInfoModal was called with a 'delete-modal' + an OK button whose callback runs the delete.
        expect(modalService.openInfoModal).toHaveBeenCalledTimes(1);
        const buttons = modalService.openInfoModal.mock.calls[0][3];
        expect(buttons[0].testId).toBe('OK');
        buttons[0].callback();
        expect(component.deleteProperty).toHaveBeenCalledWith('prop-a');
        Promise.resolve().then(() => {
            expect(comp.filteredProperties).toHaveLength(1);
            expect(comp.filteredProperties[0].name).toBe('b');
            expect(cdr.detectChanges).toHaveBeenCalled();
            done();
        });
    });

    it('sorts ascending by name on load (default sortBy)', () => {
        const {comp} = createComp({properties: [makeProperty('b'), makeProperty('a'), makeProperty('c')]});
        comp.ngOnInit();
        expect(comp.sortBy).toBe('name');
        expect(comp.reverse).toBe(false);
        expect(comp.filteredProperties.map((p) => p.name)).toEqual(['a', 'b', 'c']);
    });

    it('sort toggles reverse on repeated clicks of the same column and reorders rows', () => {
        const {comp} = createComp({properties: [makeProperty('b'), makeProperty('a'), makeProperty('c')]});
        comp.ngOnInit();
        // Switch to a different column first: a new column always resets to ascending (reverse=false),
        // mirroring the old PropertiesViewModel.sort.
        comp.sort('type');
        expect(comp.reverse).toBe(false);
        // Back to name (a different column again) → ascending.
        comp.sort('name');
        expect(comp.reverse).toBe(false);
        expect(comp.filteredProperties.map((p) => p.name)).toEqual(['a', 'b', 'c']);
        // Same column again → toggles to descending.
        comp.sort('name');
        expect(comp.reverse).toBe(true);
        expect(comp.filteredProperties.map((p) => p.name)).toEqual(['c', 'b', 'a']);
    });

    it('filters the list by the free-text term against filterTerm', () => {
        const {comp} = createComp({properties: [
            makeProperty('alpha'), makeProperty('beta'), makeProperty('gamma')]});
        comp.ngOnInit();
        comp.filterTerms = 'bet';
        comp.onFilterChange();
        expect(comp.filteredProperties.map((p) => p.name)).toEqual(['beta']);
        comp.filterTerms = '';
        comp.onFilterChange();
        expect(comp.filteredProperties).toHaveLength(3);
    });

    it('reports view mode from WorkspaceService.getComponentMode', () => {
        const {comp} = createComp({properties: [makeProperty('a')], mode: WorkspaceMode.VIEW});
        comp.ngOnInit();
        expect(comp.isViewMode()).toBe(true);
        expect(comp.isDisableMode()).toBe(true);
    });

    it('is not in view mode for an EDIT checkout', () => {
        const {comp} = createComp({properties: [makeProperty('a')], mode: WorkspaceMode.EDIT});
        comp.ngOnInit();
        expect(comp.isViewMode()).toBe(false);
    });

    it('strips the heat datatype prefix for display', () => {
        const {comp} = createComp({properties: [makeProperty('a')]});
        expect(comp.stripHeatPrefix('org.openecomp.datatypes.heat.network.Foo')).toBe('network.Foo');
        expect(comp.stripHeatPrefix(undefined)).toBe('');
    });
});
