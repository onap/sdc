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

// Mirrors the markup of onap-ui-react 1.2.0's SVGIcon, with a span in place of the svg.
const SVGIcon = ({
    name,
    onClick,
    label,
    className = '',
    iconClassName = '',
    labelClassName = '',
    labelPosition = 'bottom',
    color = '',
    disabled,
    ariaLabel,
    ...other
}) => {
    const colorClass = color !== '' ? '__' + color : '';
    const classes = `svg-icon-wrapper ${iconClassName} ${className} ${colorClass} ${
        onClick ? 'clickable' : ''
    } ${labelPosition}`;
    const Tag = onClick ? 'button' : 'div';
    const tagProps = onClick
        ? { type: 'button', 'aria-label': ariaLabel }
        : { 'aria-hidden': label ? undefined : 'true' };
    return (
        <Tag
            {...tagProps}
            {...other}
            onClick={onClick}
            className={classes}
            disabled={disabled}>
            <span className={`svg-icon __${name}`} aria-hidden="true" />
            {label && (
                <span className={`svg-icon-label ${labelClassName}`}>
                    {label}
                </span>
            )}
        </Tag>
    );
};
export default SVGIcon;
