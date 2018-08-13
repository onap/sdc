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
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import { SP_ENTITLEMENT_POOL_FORM } from '../EntitlementPoolsConstants.js';
import { DATE_FORMAT } from 'sdc-app/onboarding/OnboardingConstants.js';
import { optionsInputValues as LicenseModelOptionsInputValues } from '../../LicenseModelConstants.js';
import UuId from 'sdc-app/onboarding/licenseModel/components/UuId.jsx';

export const EntitlementPoolsFormContent = ({
    data,
    genericFieldInfo,
    onDataChanged,
    validateName,
    thresholdValueValidation,
    validateStartDate
}) => {
    let {
        name,
        description,
        thresholdUnits,
        thresholdValue,
        increments,
        startDate,
        expiryDate,
        manufacturerReferenceNumber,
        id,
        versionUUID
    } = data;
    return (
        <GridSection hasLastColSet>
            <GridItem colSpan={2}>
                <Input
                    onChange={name =>
                        onDataChanged({ name }, SP_ENTITLEMENT_POOL_FORM, {
                            name: validateName
                        })
                    }
                    isValid={genericFieldInfo.name.isValid}
                    isRequired={true}
                    errorText={genericFieldInfo.name.errorText}
                    label={i18n('Name')}
                    value={name}
                    data-test-id="create-ep-name"
                    type="text"
                />
            </GridItem>
            <GridItem colSpan={2} lastColInRow>
                <Input
                    onChange={e => {
                        // setting the unit to the correct value
                        const selectedIndex = e.target.selectedIndex;
                        const val = e.target.options[selectedIndex].value;
                        onDataChanged(
                            { thresholdUnits: val },
                            SP_ENTITLEMENT_POOL_FORM
                        );
                        // TODO make sure that the value is valid too
                        if (thresholdValue && thresholdValue !== '') {
                            onDataChanged(
                                { thresholdValue: thresholdValue },
                                SP_ENTITLEMENT_POOL_FORM,
                                { thresholdValue: thresholdValueValidation }
                            );
                        }
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
            </GridItem>
            <GridItem colSpan={2} stretch>
                <Input
                    onChange={description =>
                        onDataChanged({ description }, SP_ENTITLEMENT_POOL_FORM)
                    }
                    isValid={genericFieldInfo.description.isValid}
                    errorText={genericFieldInfo.description.errorText}
                    label={i18n('Description')}
                    value={description}
                    data-test-id="create-ep-description"
                    type="textarea"
                />
            </GridItem>
            <GridItem colSpan={2} lastColInRow>
                <Input
                    className="entitlement-pools-form-row-threshold-value"
                    onChange={thresholdValue =>
                        onDataChanged(
                            { thresholdValue },
                            SP_ENTITLEMENT_POOL_FORM,
                            {
                                thresholdValue: thresholdValueValidation
                            }
                        )
                    }
                    label={i18n('Threshold Value')}
                    isValid={genericFieldInfo.thresholdValue.isValid}
                    errorText={genericFieldInfo.thresholdValue.errorText}
                    data-test-id="create-ep-threshold-value"
                    value={thresholdValue}
                    type="text"
                />
                <Input
                    onChange={increments =>
                        onDataChanged({ increments }, SP_ENTITLEMENT_POOL_FORM)
                    }
                    label={i18n('Increments')}
                    value={increments}
                    data-test-id="create-ep-increments"
                    type="text"
                />
            </GridItem>

            <GridItem colSpan={2}>
                <Input
                    className="entitlement-pools-form-row-threshold-value"
                    onChange={manufacturerReferenceNumber =>
                        onDataChanged(
                            { manufacturerReferenceNumber },
                            SP_ENTITLEMENT_POOL_FORM
                        )
                    }
                    isValid={
                        genericFieldInfo.manufacturerReferenceNumber.isValid
                    }
                    isRequired={true}
                    errorText={
                        genericFieldInfo.manufacturerReferenceNumber.errorText
                    }
                    label={i18n('Manufacturer Reference Number')}
                    data-test-id="create-ep-manufacturerReferenceNumber-value"
                    value={manufacturerReferenceNumber}
                    type="text"
                    groupClassName="no-bottom-margin"
                />
            </GridItem>
            <GridItem colSpan={2} lastColInRow>
                <div className="date-section">
                    <Input
                        groupClassName="no-bottom-margin"
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
                                SP_ENTITLEMENT_POOL_FORM,
                                { startDate: validateStartDate }
                            )
                        }
                        isValid={genericFieldInfo.startDate.isValid}
                        errorText={genericFieldInfo.startDate.errorText}
                        selectsStart
                    />
                    <Input
                        groupClassName="no-bottom-margin"
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
                                SP_ENTITLEMENT_POOL_FORM
                            );
                            onDataChanged(
                                { startDate },
                                SP_ENTITLEMENT_POOL_FORM,
                                {
                                    startDate: validateStartDate
                                }
                            );
                        }}
                        isValid={genericFieldInfo.expiryDate.isValid}
                        errorText={genericFieldInfo.expiryDate.errorText}
                        selectsEnd
                    />
                </div>
            </GridItem>
            {id && versionUUID && <UuId id={id} versionUUID={versionUUID} />}
        </GridSection>
    );
};

EntitlementPoolsFormContent.propTypes = {
    data: PropTypes.object,
    genericFieldInfo: PropTypes.object,
    onDataChanged: PropTypes.func,
    validateName: PropTypes.func,
    thresholdValueValidation: PropTypes.func,
    validateStartDate: PropTypes.func
};

export default EntitlementPoolsFormContent;
