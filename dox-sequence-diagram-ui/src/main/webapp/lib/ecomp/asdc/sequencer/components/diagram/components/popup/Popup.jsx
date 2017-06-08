/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import Icon from '../../../icons/Icon';
import iconEdit from '../../../../../../../../res/ecomp/asdc/sequencer/sprites/icons/edit.svg';

/**
 * A hover-over popup. It shows notes, but perhaps will be put to other uses.
 * @param props React properties.
 * @returns {XML}
 * @constructor
 */
export default class Popup extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct react view.
   * @param props element properties (of which there are none).
   * @param context react context.
   */
  constructor(props, context) {
    super(props, context);
    this.state = {
      top: 0,
      left: 0,
      visible: false,
      notes: '',
    };
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render view.
   * @returns {XML}
   */
  render() {

    // Build CSS + styles to position and configure popup.

    let top = this.state.top;
    let left = this.state.left;

    const popupHeight = 200;
    const popupWidth = 320;

    let auxCssVertical = 'top';
    let auxCssHorizontal = 'left';

    if (this.state.top > (window.innerHeight - popupHeight)) {
      top -= (popupHeight + 50);
      auxCssVertical = 'bottom';
    }

    if (this.state.left > (window.innerWidth - popupWidth)) {
      left -= (popupWidth - 80);
      auxCssHorizontal = 'right';
    }

    const auxCss = `asdcs-diagram-popup-${auxCssVertical}${auxCssHorizontal}`;
    const styles = {
      top,
      left,
      display: (this.state.visible ? 'block' : 'none'),
    };

    // Render element.

    let notes = this.state.notes || '';
    if (notes.length > 255) {
      notes = notes.substring(0, 255);
      notes = `${notes} ...`;
    }

    return (
      <div className={`asdcs-diagram-popup ${auxCss}`} style={styles}>
        <div className="asdcs-diagram-popup-header">Notes</div>
        <div className="asdcs-diagram-popup-body">
          <div className="asdcs-icon-popup">
            <Icon glyph={iconEdit} />
          </div>
          <div className="asdcs-diagram-notes">
            <div className="asdcs-diagram-note">
              {notes}
            </div>
          </div>
        </div>
        <div className="asdcs-diagram-popup-footer"></div>
      </div>
    );
  }
}
