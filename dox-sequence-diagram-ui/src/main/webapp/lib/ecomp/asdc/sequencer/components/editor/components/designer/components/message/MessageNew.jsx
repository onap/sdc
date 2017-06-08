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
import { DragSource } from 'react-dnd';

import Icon from '../../../../../icons/Icon';
import iconPlus from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icons/plus.svg';
import iconHandle from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icons/handle.svg';

/**
 * LHS lifeline row view.
 */
class MessageNew extends React.Component {

  /**
   * Construct view.
   * @param props element properties.
   * @param context react context.
   */
  constructor(props, context) {
    super(props, context);
    this.onClickAdd = this.onClickAdd.bind(this);
  }

  /**
   * Handle add.
   */
  onClickAdd() {
    this.props.designer.addMessage();
  }

  /**
   * Render view.
   * @returns {*}
   */
  render() {
    const { connectDragSource } = this.props;
    return connectDragSource(
      <div className="asdcs-designer-message asdcs-designer-message-new">
        <table className="asdcs-designer-layout asdcs-designer-message-new">
          <tbody>
            <tr>
              <td>
                <div className="asdcs-designer-sort asdcs-designer-icon">
                  <Icon glyph={iconHandle} />
                </div>
              </td>
              <td>
                <div className="asdcs-designer-label" onClick={this.onClickAdd}>
                  Add Message
                </div>
              </td>
              <td>
                <div className="asdcs-designer-icon" onClick={this.onClickAdd}>
                  <Icon glyph={iconPlus} />
                </div>
              </td>
              <td>
                &nbsp;
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    );
  }
}

/** Element properties. */
MessageNew.propTypes = {
  designer: React.PropTypes.object.isRequired,
  messages: React.PropTypes.object.isRequired,
  connectDragSource: React.PropTypes.func.isRequired,
};

/** DND. */
const source = {
  beginDrag(props) {
    return { id: props.id };
  },
};

/** DND. */
const collect = function collection(connect, monitor) {
  return {
    connectDragSource: connect.dragSource(),
    isDragging: monitor.isDragging(),
  };
};

export default DragSource('message-new', source, collect)(MessageNew);

