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
import { storeCreator } from 'sdc-app/AppStore.js';
import mockRest from 'test-utils/MockRest.js';
import { cloneAndSet } from 'test-utils/Util.js';
import { actionTypes } from 'sdc-app/onboarding/onboard/filter/FilterConstants.js';
import {
    vspFactory,
    vlmFactory
} from 'test-utils/factories/common/ItemsHelperFactory.js';
import { FilterFactory } from 'test-utils/factories/onboard/FilterFactories.js';
import {
    itemStatus,
    versionStatus,
    itemPermissions
} from 'sdc-app/common/helpers/ItemsHelperConstants.js';
import OnboardActionHelper from 'sdc-app/onboarding/onboard/OnboardActionHelper.js';
import { tabsMapping } from 'sdc-app/onboarding/onboard/OnboardConstants.js';

const vsps = vspFactory.buildList(1);
const vlms = vlmFactory.buildList(1);

const timeoutPromise = new Promise(resolve => {
    setTimeout(function() {
        resolve();
    }, 100);
});

describe('Onboard Filter Tests', () => {
    it('basic test', done => {
        const store = storeCreator();

        mockRest.addHandler('fetch', ({ data, options, baseUrl }) => {
            expect(baseUrl).toEqual(
                `/onboarding-api/v1.0/items?&itemStatus=${
                    itemStatus.ACTIVE
                }&versionStatus=${versionStatus.DRAFT}&permission=${
                    itemPermissions.OWNER
                },${itemPermissions.CONTRIBUTOR}`
            );
            expect(data).toEqual(undefined);
            expect(options).toEqual(undefined);
            return { results: [] };
        });
        const expectedStore = store.getState();
        store.dispatch({
            type: actionTypes.FILTER_DATA_CHANGED,
            deltaData: {}
        });
        timeoutPromise.then(function() {
            expect(store.getState()).toEqual(expectedStore);
            done();
        });
    });

    it('load certifed data', done => {
        const store = storeCreator();

        mockRest.addHandler('fetch', ({ data, options, baseUrl }) => {
            expect(baseUrl).toEqual(
                `/onboarding-api/v1.0/items?&itemStatus=${
                    itemStatus.ACTIVE
                }&versionStatus=${versionStatus.CERTIFIED}`
            );
            expect(data).toEqual(undefined);
            expect(options).toEqual(undefined);
            return {
                results: [...vsps, ...vlms]
            };
        });

        const expectedStore = cloneAndSet(
            store.getState(),
            'onboard.filter',
            FilterFactory.build({ versionStatus: versionStatus.CERTIFIED })
        );

        const expectedFilteredItems = {
            vspList: [
                ...vsps.map(({ properties, ...other }) => ({
                    ...other,
                    ...properties
                }))
            ],
            vlmList: [...vlms]
        };
        const expectedStoreWithFilteredLists = cloneAndSet(
            expectedStore,
            'filteredItems',
            expectedFilteredItems
        );
        store.dispatch({
            type: actionTypes.FILTER_DATA_CHANGED,
            deltaData: { versionStatus: versionStatus.CERTIFIED }
        });

        timeoutPromise.then(function() {
            expect(store.getState()).toEqual(expectedStoreWithFilteredLists);
            done();
        });
    });

    it('onboarding tabs switching filter updates', done => {
        const store = storeCreator();

        mockRest.addHandler('fetch', ({ data, options, baseUrl }) => {
            expect(baseUrl).toEqual(
                `/onboarding-api/v1.0/items?&itemStatus=${
                    itemStatus.ACTIVE
                }&versionStatus=${versionStatus.CERTIFIED}`
            );
            expect(data).toEqual(undefined);
            expect(options).toEqual(undefined);
            return { results: [] };
        });

        expect(store.getState().onboard.filter.versionStatus).toEqual(
            versionStatus.DRAFT
        );

        OnboardActionHelper.changeActiveTab(
            store.dispatch,
            tabsMapping.CATALOG
        );

        timeoutPromise.then(function() {
            expect(store.getState().onboard.filter.versionStatus).toEqual(
                versionStatus.CERTIFIED
            );
            done();
        });
    });
});
