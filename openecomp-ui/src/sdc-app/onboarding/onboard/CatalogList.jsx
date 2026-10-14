/*
 * Copyright © 2016-2018 European Support Limited
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
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
import classnames from 'classnames';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { SVGIcon } from 'onap-ui-react';

const SoftwareProductListHeader = ({ selectedVendor, onBack }) => (
    <div className="vendor-page-header">
        <SVGIcon name="back" onClick={onBack} />
        <div className="tab-separator" />
        <div className="vendor-name">{selectedVendor.name}</div>
    </div>
);

const CatalogList = ({
    children,
    onAddVLM,
    onAddVSP,
    canAddVSP = true,
    vendorPageOptions
}) => (
    <div className="catalog-list">
        {vendorPageOptions && (
            <SoftwareProductListHeader
                onBack={vendorPageOptions.onBack}
                selectedVendor={vendorPageOptions.selectedVendor}
            />
        )}
        <div className="catalog-items">
            {(onAddVLM || onAddVSP) && (
                <div className="create-catalog-item-wrapper">
                    {onAddVLM && (
                        <CreateItemTile
                            onClick={onAddVLM}
                            dataTestId={'catalog-add-new-vlm'}
                            className="vlm-type"
                            title={i18n('CREATE NEW VLM')}
                        />
                    )}
                    {onAddVSP && (
                        <CreateItemTile
                            onClick={() => onAddVSP()}
                            dataTestId={'catalog-add-new-vsp'}
                            className="vsp-type"
                            title={i18n('CREATE NEW VSP')}
                            disabledReason={
                                canAddVSP
                                    ? undefined
                                    : i18n('Create and submit a VLM first')
                            }
                        />
                    )}
                </div>
            )}
            {children}
        </div>
    </div>
);

class CreateItemTile extends React.Component {
    state = { showHint: false };

    showHint = () => this.setState({ showHint: true });

    hideHint = () => this.setState({ showHint: false });

    activate = () => {
        const { disabledReason, onClick } = this.props;
        if (disabledReason) {
            this.showHint();
        } else {
            onClick();
        }
    };

    onKeyDown = e => {
        if (e.key === 'Escape') {
            this.hideHint();
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            this.activate();
        }
    };

    render() {
        const {
            dataTestId,
            title,
            className = '',
            disabledReason
        } = this.props;
        const disabled = !!disabledReason;
        const hintId = `${dataTestId}-hint`;
        return (
            <div
                className={classnames('create-catalog-item tile', className, {
                    disabled
                })}
                role="button"
                tabIndex={0}
                aria-disabled={disabled || undefined}
                aria-describedby={disabled ? hintId : undefined}
                onClick={this.activate}
                onKeyDown={this.onKeyDown}
                onMouseEnter={disabled ? this.showHint : undefined}
                onMouseLeave={disabled ? this.hideHint : undefined}
                onFocus={disabled ? this.showHint : undefined}
                onBlur={disabled ? this.hideHint : undefined}
                data-test-id={dataTestId}>
                <div className="create-item-plus-icon">
                    <SVGIcon name="plus" />
                </div>
                <div className="create-item-text">{title}</div>
                {disabled && (
                    // aria-hidden keeps the hint out of the button's name;
                    // aria-describedby still reads it as the description.
                    <div
                        id={hintId}
                        aria-hidden="true"
                        className={classnames('create-item-hint', {
                            visible: this.state.showHint
                        })}>
                        {disabledReason}
                    </div>
                )}
            </div>
        );
    }
}

export default CatalogList;
