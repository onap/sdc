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
import Input from 'nfvo-components/input/validation/Input.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {
    itemStatus,
    itemPermissions,
    itemOnboardingProcedure
} from 'sdc-app/common/helpers/ItemsHelperConstants.js';
import { Accordion, Checklist, Checkbox } from 'onap-ui-react';

export const ItemStatus = ({ data, onDataChanged, byVendorView }) => (
    <Input
        type="select"
        className="catalog-filter-items-type"
        data-test-id="catalog-filter-items-type"
        disabled={byVendorView}
        value={data.itemStatus}
        onChange={e => onDataChanged({ itemStatus: e.target.value }, data)}>
        <option key={itemStatus.ACTIVE} value={itemStatus.ACTIVE}>
            {i18n('Active Items')}
        </option>
        <option key={itemStatus.ARCHIVED} value={itemStatus.ARCHIVED}>
            {i18n('Archived Items')}
        </option>
    </Input>
);

const FilterList = ({
    title,
    items,
    groupKey,
    onDataChanged,
    data,
    dataTestId
}) => {
    let onChange = value => {
        let obj = {};
        obj[groupKey] = { ...data[groupKey], ...value };
        onDataChanged(obj);
    };
    return (
        <Accordion defaultExpanded dataTestId={dataTestId} title={title}>
            <Checklist items={items} onChange={onChange} />
        </Accordion>
    );
};

export const ByVendorView = ({ data, onDataChanged }) => (
    <Checkbox
        label={i18n('By Vendor View')}
        className="catalog-filter-by-vendor-view"
        disabled={data.itemsType === itemStatus.ARCHIVED}
        checked={data.byVendorView}
        onChange={byVendorView => onDataChanged({ byVendorView }, data)}
        data-test-id="filter-by-vendor-view"
        value=""
    />
);

export const EntityType = ({ data, onDataChanged }) => {
    const items = [
        {
            label: i18n('VSP'),
            dataTestId: 'catalog-filter-type-vsp',
            value: 'vsp',
            checked: data.itemType && data.itemType.vsp
        },
        {
            label: i18n('VLM'),
            dataTestId: 'catalog-filter-type-vlm',
            value: 'vlm',
            checked: data.itemType && data.itemType.vlm
        }
    ];
    return (
        <FilterList
            dataTestId="catalog-filter-entity-type"
            title={i18n('ENTITY TYPE')}
            items={items}
            onDataChanged={onDataChanged}
            data={data}
            groupKey="itemType"
        />
    );
};

export const Permissions = ({ data, onDataChanged }) => {
    const items = [
        {
            label: i18n('Owner'),
            dataTestId: 'catalog-filter-permission-owner',
            value: itemPermissions.OWNER,
            checked: data.permission && data.permission.Owner
        },
        {
            label: i18n('Contributor'),
            dataTestId: 'catalog-filter-permission-contributor',
            value: itemPermissions.CONTRIBUTOR,
            checked: data.permission && data.permission.Contributor
        }
    ];

    return (
        <FilterList
            dataTestId="catalog-filter-permissions"
            title={i18n('PERMISSIONS')}
            items={items}
            onDataChanged={onDataChanged}
            data={data}
            groupKey="permission"
        />
    );
};

export const OnboardingProcedure = ({ data, onDataChanged }) => {
    const items = [
        {
            label: i18n('Network Package'),
            dataTestId: 'catalog-filter-procedure-network',
            value: itemOnboardingProcedure.NETWORK,
            checked:
                data.onboardingMethod && data.onboardingMethod.NetworkPackage
        },
        {
            label: i18n('Manual'),
            dataTestId: 'catalog-filter-procedure-manual',
            value: itemOnboardingProcedure.MANUAL,
            checked: data.onboardingMethod && data.onboardingMethod.Manual
        }
    ];

    return (
        <FilterList
            dataTestId="catalog-filter-onboarding-procedure"
            title={i18n('ONBOARDING PROCEDURE')}
            items={items}
            onDataChanged={onDataChanged}
            data={data}
            groupKey="onboardingMethod"
        />
    );
};
