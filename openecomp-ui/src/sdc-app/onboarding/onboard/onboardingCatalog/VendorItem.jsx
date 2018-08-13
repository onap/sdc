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
import ClickOutsideWrapper from 'nfvo-components/clickOutsideWrapper/ClickOutsideWrapper.jsx';

import {
    Tile,
    TileInfo,
    TileInfoLine,
    TileFooter,
    TileFooterCell,
    Button
} from 'sdc-ui/lib/react';

import VSPOverlay from './VSPOverlay.jsx';
import { TooltipWrapper } from './Tooltip.jsx';

class VendorItem extends React.Component {
    static propTypes = {
        softwareProductList: PropTypes.array,
        vendor: PropTypes.object,
        shouldShowOverlay: PropTypes.bool,
        onSelectVSP: PropTypes.func,
        onVendorSelect: PropTypes.func,
        onAddVSP: PropTypes.func,
        onVSPButtonClick: PropTypes.func,
        activeTabName: PropTypes.string
    };
    handleSeeMore = () => {
        const { onVendorSelect, vendor, activeTabName } = this.props;
        onVendorSelect(vendor, activeTabName);
    };
    render() {
        let { vendor, onSelectVSP, shouldShowOverlay, onMigrate } = this.props;
        let { softwareProductList = [], name } = vendor;
        return (
            <Tile iconName="vendor" onClick={this.handleSeeMore}>
                <TileInfo align="center">
                    <TileInfoLine type="title">
                        <TooltipWrapper
                            className="with-overlay"
                            dataTestId="catalog-item-name">
                            {name}
                        </TooltipWrapper>
                    </TileInfoLine>
                    <TileInfoLine>
                        <Button
                            btnType="secondary"
                            className="venodor-tile-btn"
                            onClick={this.handleVspCountClick}
                            data-test-id="catalog-vsp-count"
                            disabled={!softwareProductList.length}>
                            {`${softwareProductList.length.toString()} ${i18n(
                                'VSPs'
                            )}`}
                        </Button>
                        {shouldShowOverlay &&
                            softwareProductList.length > 0 && (
                                <ClickOutsideWrapper
                                    onClose={this.handleClickOutside}>
                                    <VSPOverlay
                                        onMigrate={onMigrate}
                                        VSPList={softwareProductList}
                                        onSelectVSP={onSelectVSP}
                                        onSeeMore={this.handleSeeMore}
                                    />
                                </ClickOutsideWrapper>
                            )}
                    </TileInfoLine>
                </TileInfo>
                <TileFooter align="center">
                    <TileFooterCell dataTestId="catalog-create-new-vsp-from-vendor">
                        <Button
                            btnType="link"
                            color="primary"
                            iconName="plusThin"
                            onClick={this.onCreateVspClick}>
                            {i18n('Create new VSP')}
                        </Button>
                    </TileFooterCell>
                </TileFooter>
            </Tile>
        );
    }

    onCreateVspClick = e => {
        e.stopPropagation();
        e.preventDefault();
        const { onAddVSP, vendor: { id } } = this.props;
        onAddVSP(id);
    };
    handleClickOutside = () => {
        const { onVSPButtonClick, vlm } = this.props;
        onVSPButtonClick(false, vlm);
    };

    handleVspCountClick = e => {
        e.stopPropagation();
        e.preventDefault();
        const {
            onVSPButtonClick,
            vendor: { softwareProductList },
            vlm
        } = this.props;
        const hasVSP = Boolean(softwareProductList.length);
        onVSPButtonClick(hasVSP, vlm);
    };
}

export default VendorItem;
