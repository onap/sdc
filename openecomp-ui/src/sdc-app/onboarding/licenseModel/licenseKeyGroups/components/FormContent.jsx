/*!
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {
    optionsInputValues as licenseKeyGroupOptionsInputValues,
    LKG_FORM_NAME
} from '../LicenseKeyGroupsConstants.js';
import { optionsInputValues as LicenseModelOptionsInputValues } from '../../LicenseModelConstants.js';
import { DATE_FORMAT } from 'sdc-app/onboarding/OnboardingConstants.js';
import UuId from 'sdc-app/onboarding/licenseModel/components/UuId.jsx';

const LicenseKeyGroupFormContent = ({
    data,
    onDataChanged,
    genericFieldInfo,
    validateName,
    validateStartDate,
    thresholdValueValidation
}) => {
    let {
        name,
        description,
        increments,
        type,
        thresholdUnits,
        thresholdValue,
        startDate,
        expiryDate,
        manufacturerReferenceNumber,
        id,
        versionUUID
    } = data;
    return (
        <GridSection hasLostColSet>
            <GridItem colSpan={2}>
                <Input
                    onChange={name =>
                        onDataChanged({ name }, LKG_FORM_NAME, {
                            name: validateName
                        })
                    }
                    label={i18n('Name')}
                    data-test-id="create-lkg-name"
                    value={name}
                    isValid={genericFieldInfo.name.isValid}
                    errorText={genericFieldInfo.name.errorText}
                    isRequired={true}
                    type="text"
                />
            </GridItem>
            <GridItem colSpan={2}>
                <Input
                    isRequired={true}
                    onChange={e => {
                        const selectedIndex = e.target.selectedIndex;
                        const val = e.target.options[selectedIndex].value;
                        onDataChanged({ type: val }, LKG_FORM_NAME);
                    }}
                    value={type}
                    label={i18n('Type')}
                    data-test-id="create-lkg-type"
                    isValid={genericFieldInfo.type.isValid}
                    errorText={genericFieldInfo.type.errorText}
                    groupClassName="bootstrap-input-options"
                    className="input-options-select"
                    overlayPos="bottom"
                    type="select">
                    {licenseKeyGroupOptionsInputValues.TYPE.map(type => (
                        <option key={type.enum} value={type.enum}>
                            {type.title}
                        </option>
                    ))}
                </Input>
            </GridItem>
            <GridItem colSpan={2} stretch>
                <Input
                    onChange={description =>
                        onDataChanged({ description }, LKG_FORM_NAME)
                    }
                    label={i18n('Description')}
                    data-test-id="create-lkg-description"
                    value={description}
                    isValid={genericFieldInfo.description.isValid}
                    errorText={genericFieldInfo.description.errorText}
                    type="textarea"
                    overlayPos="bottom"
                />
            </GridItem>
            <GridItem>
                <Input
                    onChange={e => {
                        // setting the unit to the correct value
                        const selectedIndex = e.target.selectedIndex;
                        const val = e.target.options[selectedIndex].value;
                        onDataChanged({ thresholdUnits: val }, LKG_FORM_NAME);
                        // TODO make sure that the value is valid too
                        onDataChanged(
                            { thresholdValue: thresholdValue },
                            LKG_FORM_NAME,
                            {
                                thresholdValue: thresholdValueValidation
                            }
                        );
                    }}
                    value={thresholdUnits}
                    label={i18n('Threshold Units')}
                    data-test-id="create-ep-threshold-units"
                    isValid={genericFieldInfo.thresholdUnits.isValid}
                    errorText={genericFieldInfo.thresholdUnits.errorText}
                    groupClassName="bootstrap-input-options"
                    className="input-options-select"
                    type="select">
                    {LicenseModelOptionsInputValues.THRESHOLD_UNITS.map(
                        mtype => (
                            <option key={mtype.enum} value={mtype.enum}>{`${
                                mtype.title
                            }`}</option>
                        )
                    )}
                </Input>
                <Input
                    type="date"
                    label={i18n('Start Date')}
                    value={startDate}
                    dateFormat={DATE_FORMAT}
                    startDate={startDate}
                    endDate={expiryDate}
                    onChange={startDate =>
                        onDataChanged(
                            {
                                startDate: startDate
                                    ? startDate.format(DATE_FORMAT)
                                    : ''
                            },
                            LKG_FORM_NAME,
                            { startDate: validateStartDate }
                        )
                    }
                    isValid={genericFieldInfo.startDate.isValid}
                    errorText={genericFieldInfo.startDate.errorText}
                    selectsStart
                />
            </GridItem>
            <GridItem>
                <Input
                    className="entitlement-pools-form-row-threshold-value"
                    onChange={thresholdValue =>
                        onDataChanged({ thresholdValue }, LKG_FORM_NAME, {
                            thresholdValue: thresholdValueValidation
                        })
                    }
                    label={i18n('Threshold Value')}
                    isValid={genericFieldInfo.thresholdValue.isValid}
                    errorText={genericFieldInfo.thresholdValue.errorText}
                    data-test-id="create-ep-threshold-value"
                    value={thresholdValue}
                    type="text"
                />
                <Input
                    type="date"
                    label={i18n('Expiry Date')}
                    value={expiryDate}
                    dateFormat={DATE_FORMAT}
                    startDate={startDate}
                    endDate={expiryDate}
                    onChange={expiryDate => {
                        onDataChanged(
                            {
                                expiryDate: expiryDate
                                    ? expiryDate.format(DATE_FORMAT)
                                    : ''
                            },
                            LKG_FORM_NAME
                        );
                        onDataChanged({ startDate }, LKG_FORM_NAME, {
                            startDate: validateStartDate
                        });
                    }}
                    isValid={genericFieldInfo.expiryDate.isValid}
                    errorText={genericFieldInfo.expiryDate.errorText}
                    selectsEnd
                />
            </GridItem>
            <GridItem colSpan={2}>
                <Input
                    onChange={manufacturerReferenceNumber =>
                        onDataChanged(
                            { manufacturerReferenceNumber },
                            LKG_FORM_NAME
                        )
                    }
                    label={i18n('Manufacturer Reference Number')}
                    value={manufacturerReferenceNumber}
                    data-test-id="create-ep-mrn"
                    type="text"
                    groupClassName="no-bottom-margin"
                />
            </GridItem>

            <GridItem colSpan={2}>
                <Input
                    onChange={increments =>
                        onDataChanged({ increments }, LKG_FORM_NAME)
                    }
                    label={i18n('Increments')}
                    value={increments}
                    data-test-id="create-ep-increments"
                    type="text"
                    groupClassName="no-bottom-margin"
                />
            </GridItem>
            {id && versionUUID && <UuId id={id} versionUUID={versionUUID} />}
        </GridSection>
    );
};

export default LicenseKeyGroupFormContent;
