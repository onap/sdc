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
import Enzyme, { mount } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { ItemStatus } from 'sdc-app/onboarding/onboard/filter/FilterComponents.jsx';

describe('Filter components', () => {
    beforeAll(() => {
        Enzyme.configure({ adapter: new Adapter() });
    });

    it('ItemStatus select has an accessible name', () => {
        const wrapper = mount(
            <ItemStatus
                data={{ itemStatus: 'ACTIVE' }}
                onDataChanged={() => {}}
            />
        );
        expect(wrapper.find('select').prop('aria-label')).toBe(
            i18n('Item status')
        );
    });
});
