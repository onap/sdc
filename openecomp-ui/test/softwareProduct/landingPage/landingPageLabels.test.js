/*
 * Copyright (C) 2026 Deutsche Telekom AG
 *
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
 */

import React from 'react';
import { render } from '@testing-library/react';
import { Provider } from 'react-redux';
import { storeCreator } from 'sdc-app/AppStore.js';
import { VSPEditorFactory } from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import { VSPComponentsFactory } from 'test-utils/factories/softwareProduct/SoftwareProductComponentsFactories.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';
import SoftwareProductLandingPageView from 'sdc-app/onboarding/softwareProduct/landingPage/SoftwareProductLandingPageView.jsx';
import SoftwareProductComponentsMonitoringView from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringView.jsx';
import SoftwareProductProcessesEditorView from 'sdc-app/onboarding/softwareProduct/processes/SoftwareProductProcessesEditorView.jsx';

const renderLandingPage = () =>
    render(
        <Provider store={storeCreator()}>
            <SoftwareProductLandingPageView
                {...CurrentScreenFactory.build().props}
                currentSoftwareProduct={VSPEditorFactory.build({
                    id: 'RTRTG454545',
                    onBoardingMethod: 'HEAT',
                    licenseType: 'INTERNAL'
                })}
                componentsList={VSPComponentsFactory.buildList(2)}
                features={[{ name: 'EXTERNAL_LICENSE', active: true }]}
                fetchUploadStatus={() => Promise.resolve({ data: {} })}
            />
        </Provider>
    );

describe('VSP landing page - license type', () => {
    it('labels both license radios', () => {
        const { getByLabelText } = renderLandingPage();
        const internal = getByLabelText(i18n('Internal license'));
        expect(internal.id).toBe('INTERNAL');
        expect(internal.getAttribute('type')).toBe('radio');
        expect(getByLabelText(i18n('External license')).id).toBe('EXTERNAL');
    });

    it('groups the radios under a name', () => {
        const { getByRole } = renderLandingPage();
        const group = getByRole('group', { name: 'License type' });
        expect(group.tagName).toBe('FIELDSET');
        expect(group.querySelectorAll('input[type="radio"]').length).toBe(2);
    });
});

describe('Dropzone file inputs', () => {
    it('names the VSP package input on the landing page', () => {
        const { getByLabelText } = renderLandingPage();
        expect(getByLabelText('Upload VSP package').getAttribute('type')).toBe(
            'file'
        );
    });

    it('names each monitoring file input', () => {
        const { getByLabelText } = render(
            <SoftwareProductComponentsMonitoringView
                isReadOnlyMode={false}
                filenames={{}}
                softwareProductId="vsp"
                onDropMibFileToUpload={() => {}}
                onDeleteSnmpFile={() => {}}
            />
        );
        ['VES', 'SNMP Trap', 'SNMP Poll'].forEach(type =>
            expect(
                getByLabelText(`Upload ${type} file`).getAttribute('type')
            ).toBe('file')
        );
    });

    it('names the process file input', () => {
        const { getByLabelText } = render(
            <SoftwareProductProcessesEditorView
                isReadOnlyMode={false}
                data={{}}
                previousData={{}}
                onDataChanged={() => {}}
                onSubmit={() => {}}
                onClose={() => {}}
                genericFieldInfo={{
                    name: { isValid: true, errorText: '' },
                    description: { isValid: true, errorText: '' },
                    type: { isValid: true, errorText: '' }
                }}
            />
        );
        expect(
            getByLabelText('Upload process file').getAttribute('type')
        ).toBe('file');
    });
});
