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

import {async, ComponentFixture} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {SdcUiComponentsModule} from 'onap-ui-angular';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import {ConfigureFn, configureTests} from '../../../../../../../jest/test-config.helper';
import {TranslatePipe} from '../../../../../shared/translator/translate.pipe';
import {TranslateService} from '../../../../../shared/translator/translate.service';
import {RequirementsEditorComponent} from './requirements-editor.component';

describe('requirements editor component', () => {

    let fixture: ComponentFixture<RequirementsEditorComponent>;

    const nodeTypeName = 'org.openecomp.resource.abstract.nodes.VFC';
    const capabilityTypeName = 'tosca.capabilities.Node';

    const nodeTypesList = [{
        componentMetadataDefinition: {
            componentMetadataDataDefinition: {toscaResourceName: nodeTypeName}
        }
    }] as any;
    const capabilityTypesList = [{toscaPresentation: {type: capabilityTypeName}}] as any;

    // The translate pipe is resolved from the component's own element injector, so the mock has to
    // replace the component-level provider as well as the module-level one.
    const translateServiceMock = {
        activeLanguage: 'en_US',
        languageChangedObservable: Observable.of('en_US'),
        translate: (phrase: string) => phrase
    };

    const buildFixture = (requirement: any) => {
        const configure: ConfigureFn = testBed => {
            testBed.configureTestingModule({
                declarations: [RequirementsEditorComponent, TranslatePipe],
                imports: [SdcUiComponentsModule],
                providers: [{provide: TranslateService, useValue: translateServiceMock}]
            });
            testBed.overrideComponent(RequirementsEditorComponent, {
                set: {providers: [{provide: TranslateService, useValue: translateServiceMock}]}
            });
        };
        return configureTests(configure).then(testBed => {
            fixture = testBed.createComponent(RequirementsEditorComponent);
            fixture.componentInstance.input = {
                requirement,
                capabilityTypesList,
                nodeTypesList,
                relationshipTypesList: [],
                isReadonly: false
            };
            fixture.detectChanges();
            // sdc-input drives the visible field through ngModel, which writes on a microtask.
            return fixture.whenStable().then(() => fixture.detectChanges());
        });
    };

    // sdc-dropdown takes testId as an @Input and re-emits it on its inner div, not on its host.
    const dropdownOf = (testId: string) =>
        fixture.debugElement.queryAll(By.css('sdc-dropdown'))
            .map(element => element.componentInstance)
            .filter(instance => instance.testId === testId)[0];

    const fieldOf = (testId: string): HTMLInputElement =>
        fixture.nativeElement.querySelector(`div.sdc-dropdown[data-tests-id="${testId}"] input.sdc-input__input`);

    describe('when adding a requirement from scratch', () => {

        beforeEach(async(() => buildFixture(undefined)));

        it('shows a real placeholder on every dropdown instead of the unset field value', () => {
            expect(fieldOf('reqNode').getAttribute('placeholder')).toEqual('REQ_NODE_PLACEHOLDER');
            expect(fieldOf('reqRelatedCapability').getAttribute('placeholder'))
                .toEqual('REQ_RELATED_CAPABILITY_PLACEHOLDER');
            expect(fieldOf('reqRelationship').getAttribute('placeholder'))
                .toEqual('REQ_RELATIONSHIP_PLACEHOLDER');
        });

        it('leaves the node dropdown unselected', () => {
            expect(dropdownOf('reqNode').getValue()).toBeUndefined();
        });

        it('records the picked option as the requirement node', () => {
            dropdownOf('reqNode').selectOption({label: nodeTypeName, value: nodeTypeName});
            expect(fixture.componentInstance.requirementData.node).toEqual(nodeTypeName);
        });
    });

    describe('when editing a requirement that already has selections', () => {

        beforeEach(async(() => buildFixture({
            name: 'req1',
            capability: capabilityTypeName,
            node: nodeTypeName,
            minOccurrences: 1,
            maxOccurrences: '2'
        })));

        it('marks the stored value as the selected option, not as placeholder text', () => {
            expect(dropdownOf('reqNode').getValue()).toEqual(nodeTypeName);
            expect(dropdownOf('reqRelatedCapability').getValue()).toEqual(capabilityTypeName);
        });

        it('renders the stored value in the field itself', () => {
            expect(fieldOf('reqNode').value).toEqual(nodeTypeName);
            expect(fieldOf('reqRelatedCapability').value).toEqual(capabilityTypeName);
        });
    });
});
