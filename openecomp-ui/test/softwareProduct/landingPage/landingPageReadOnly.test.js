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
import i18n from 'nfvo-utils/i18n/i18n.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';
import SoftwareProductLandingPageView from 'sdc-app/onboarding/softwareProduct/landingPage/SoftwareProductLandingPageView.jsx';

const renderLandingPage = ({ isReadOnlyMode, externalLicense }) =>
    render(
        <Provider store={storeCreator()}>
            <SoftwareProductLandingPageView
                {...CurrentScreenFactory.build().props}
                isReadOnlyMode={isReadOnlyMode}
                currentSoftwareProduct={VSPEditorFactory.build({
                    id: 'RTRTG454545',
                    onBoardingMethod: 'HEAT',
                    licenseType: 'INTERNAL',
                    licenseAgreementName: undefined
                })}
                componentsList={[]}
                features={[
                    { name: 'EXTERNAL_LICENSE', active: externalLicense }
                ]}
                fetchUploadStatus={() => Promise.resolve({ data: {} })}
            />
        </Provider>
    );

describe('VSP landing page - read-only license', () => {
    it('keeps the license radios enabled when editable', () => {
        const { getByLabelText } = renderLandingPage({
            isReadOnlyMode: false,
            externalLicense: true
        });
        expect(getByLabelText(i18n('Internal license')).disabled).toBe(false);
        expect(getByLabelText(i18n('External license')).disabled).toBe(false);
    });

    it('disables the license radios in read-only mode', () => {
        const { getByLabelText } = renderLandingPage({
            isReadOnlyMode: true,
            externalLicense: true
        });
        expect(getByLabelText(i18n('Internal license')).disabled).toBe(true);
        expect(getByLabelText(i18n('External license')).disabled).toBe(true);
    });

    it('disables the Missing license button in read-only mode', () => {
        const { getByRole } = renderLandingPage({
            isReadOnlyMode: true,
            externalLicense: false
        });
        expect(getByRole('button', { name: i18n('Missing') }).disabled).toBe(
            true
        );
    });
});
