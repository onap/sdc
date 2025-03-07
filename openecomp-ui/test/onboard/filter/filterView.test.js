/*
 * Copyright © 2016-2018 European Support Limited
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
import { mount } from 'enzyme';
import { Provider } from 'react-redux';
import { storeCreator } from 'sdc-app/AppStore.js';
import Filter from 'sdc-app/onboarding//onboard/filter/Filter.jsx';
import Adapter from 'enzyme-adapter-react-16';
import Enzyme from 'enzyme';

describe('Filter component view Tests', () => {
    it('simple jsx test', () => {
        Enzyme.configure({ adapter: new Adapter() })
        const store = storeCreator();
        const wrapper = mount(
            <Provider store={store}>
                <Filter />
            </Provider>
        );
        const filter = wrapper.find('.catalog-filter').hostNodes();
        expect(filter.hasClass('catalog-filter')).toBeTruthy();
    });
});
