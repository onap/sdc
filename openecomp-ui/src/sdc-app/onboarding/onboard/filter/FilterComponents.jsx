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
import Accordion from 'nfvo-components/accordion/Accordion.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { itemStatus as itemStatusConstants } from './FilterConstants.js';
import { itemStatus } from 'sdc-app/common/helpers/ItemsHelperConstants.js';

export const ItemStatus = ({ data, onDataChanged, byVendorView }) => (
    <Input
        type="select"
        className="catalog-filter-items-type"
        data-test-id="catalog-filter-items-type"
        disabled={byVendorView}
        value={data.itemStatus}
        onChange={e => onDataChanged({ itemStatus: e.target.value }, data)}>
        <option key={itemStatus.ACTIVE} value={itemStatus.ACTIVE}>
            Active Items
        </option>
        <option
            key={itemStatusConstants.ARCHIVED}
            value={itemStatusConstants.ARCHIVED}>
            Archived Items
        </option>
    </Input>
);

export const RecentlyUpdated = ({ data, onDataChanged }) => (
    <Input
        label={i18n('Recently Updated')}
        type="checkbox"
        checked={data.recentlyUpdated}
        onChange={recentlyUpdated => onDataChanged({ recentlyUpdated }, data)}
        data-test-id="filter-recently-updated"
        value=""
    />
);

export const ByVendorView = ({ data, onDataChanged }) => (
    <Input
        label={i18n('By Vendor View')}
        type="checkbox"
        disabled={data.itemsType === itemStatus.ARCHIVED}
        checked={data.byVendorView}
        onChange={byVendorView => onDataChanged({ byVendorView }, data)}
        data-test-id="filter-by-vendor-view"
        value=""
    />
);

export const EntityType = ({ data, onDataChanged }) => (
    <Accordion title={i18n('ENTITY TYPE')}>
        <Input
            label={i18n('VSP')}
            type="checkbox"
            checked={data.entityType.vsp}
            onChange={vsp =>
                onDataChanged({ entityType: { ...data.entityType, vsp } }, data)
            }
            data-test-id="filter-type-vsp"
            value=""
        />
        <Input
            label={i18n('VLM')}
            type="checkbox"
            checked={data.entityType.vlm}
            onChange={vlm =>
                onDataChanged({ entityType: { ...data.entityType, vlm } }, data)
            }
            data-test-id="filter-type-vlm"
            value=""
        />
    </Accordion>
);

export const Role = ({ data, onDataChanged }) => (
    <Accordion title={i18n('ROLE')}>
        <Input
            label={i18n('Owner')}
            type="checkbox"
            checked={data.permission.Owner}
            onChange={Owner =>
                onDataChanged(
                    { permission: { ...data.permission, Owner } },
                    data
                )
            }
            data-test-id="filter-role-owner"
            value=""
        />
        <Input
            label={i18n('Contributer')}
            type="checkbox"
            checked={data.permission.Contributor}
            onChange={Contributor =>
                onDataChanged(
                    { permission: { ...data.permission, Contributor } },
                    data
                )
            }
            data-test-id="filter-role-contributor"
            value=""
        />
    </Accordion>
);

export const OnboardingProcedure = ({ data, onDataChanged }) => (
    <Accordion title={i18n('ONBOARDING PROCEDURE')}>
        <Input
            label={i18n('Network Package')}
            type="checkbox"
            checked={data.onboardingMethod.NetworkPackage}
            onChange={NetworkPackage =>
                onDataChanged(
                    {
                        onboardingMethod: {
                            ...data.onboardingMethod,
                            NetworkPackage
                        }
                    },
                    data
                )
            }
            data-test-id="filter-procedure-network"
            value=""
        />
        <Input
            label={i18n('Manual')}
            type="checkbox"
            checked={data.onboardingMethod.Manual}
            onChange={Manual =>
                onDataChanged(
                    { onboardingMethod: { ...data.onboardingMethod, Manual } },
                    data
                )
            }
            data-test-id="filter-procedure-manual"
            value=""
        />
    </Accordion>
);
