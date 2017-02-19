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
import Select from 'react-select';
import { DragSource, DropTarget } from 'react-dnd';

import Common from '../../../../../../common/Common';

import Icon from '../../../../../icons/Icon';
import iconDelete from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/delete.svg';
import iconHandle from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/handle.svg';
import iconNotes from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/notes.svg';
import iconSettings from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/settings.svg';
import iconRequestSync from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/arrow/request-sync.svg';
import iconRequestAsync from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/arrow/request-async.svg';
import iconResponse from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/arrow/response.svg';

/**
 * LHS message row view.
 */
class Message extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct view.
   * @param props element properties.
   * @param context react context.
   */
  constructor(props, context) {
    super(props, context);

    this.state = {
      active: false,
      name: props.message.name || '',
    };

    this.combinedOptions = [{
      value: 'REQUEST_SYNC',
    }, {
      value: 'REQUEST_ASYNC',
    }, {
      value: 'RESPONSE',
    }];

    // Bindings.

    this.onChangeName = this.onChangeName.bind(this);
    this.onBlurName = this.onBlurName.bind(this);
    this.onChangeType = this.onChangeType.bind(this);
    this.onChangeFrom = this.onChangeFrom.bind(this);
    this.onChangeTo = this.onChangeTo.bind(this);
    this.onClickDelete = this.onClickDelete.bind(this);
    this.onClickActions = this.onClickActions.bind(this);
    this.onClickNotes = this.onClickNotes.bind(this);
    this.onMouseEnter = this.onMouseEnter.bind(this);
    this.onMouseLeave = this.onMouseLeave.bind(this);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle name change.
   * @param event change event.
   */
  onChangeName(event) {
    this.setState({ name: event.target.value });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle name change.
   * @param event change event.
   */
  onBlurName(event) {
    const options = this.props.application.getOptions();
    const sanitized = Common.sanitizeText(event.target.value, options, 'message');
    const props = {
      id: this.props.message.id,
      name: sanitized,
    };
    this.props.designer.updateMessage(props);
    this.setState({ name: sanitized });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle delete.
   */
  onClickDelete() {
    this.props.designer.deleteMessage(this.props.message.id);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle menu click.
   */
  onClickActions(event) {
    this.props.designer.showActions(this.props.message.id, { x: event.pageX, y: event.pageY });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle menu click.
   */
  onClickNotes() {
    this.props.designer.showNotes(this.props.message.id);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle selection.
   * @param value selection.
   */
  onChangeFrom(value) {
    if (value.target) {
      this.updateMessage({ from: value.target.value });
    } else {
      this.updateMessage({ from: value.value });
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle selection.
   * @param value selection.
   */
  onChangeTo(value) {
    if (value.target) {
      this.updateMessage({ to: value.target.value });
    } else {
      this.updateMessage({ to: value.value });
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle selection.
   * @param selected selection.
   */
  onChangeType(selected) {

    const value = selected.target ? selected.target.value : selected.value;
    const props = {};
    if (value.indexOf('RESPONSE') !== -1) {
      props.type = 'response';
      props.asynchronous = false;
    } else {
      props.type = 'request';
      props.asynchronous = (value.indexOf('ASYNC') !== -1);
    }

    this.updateMessage(props);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle mouse event.
   */
  onMouseEnter() {
    this.setState({ active: true });
    this.props.designer.onMouseEnterMessage(this.props.message.id);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle mouse event.
   */
  onMouseLeave() {
    this.setState({ active: false });
    this.props.designer.onMouseLeaveMessage(this.props.message.id);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Update message properties.
   * @param props properties updates.
   */
  updateMessage(props) {
    const update = {
      id: this.props.message.id,
    };
    for (const k of Object.keys(props)) {
      update[k] = props[k];
    }
    this.props.designer.updateMessage(update);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render icon.
   * @param option selection.
   * @returns {XML}
   */
  renderOption(option) {
    if (option.value === 'RESPONSE') {
      return <Icon glyph={iconResponse} />;
    }
    if (option.value === 'REQUEST_ASYNC') {
      return <Icon glyph={iconRequestAsync} />;
    }
    return <Icon glyph={iconRequestSync} />;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get request/response and asynchronous combined constant.
   * @param message message whose properties define spec.
   * @returns {*}
   */
  getMessageSpec(message) {
    if (message.type === 'response') {
      return 'RESPONSE';
    }
    if (message.asynchronous) {
      return 'REQUEST_ASYNC';
    }
    return 'REQUEST_SYNC';
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @returns {*}
   * @private
   */
  renderHTMLSelect() {

    const message = this.props.message;
    const from = this.props.from;
    const to = Common.assertNotNull(this.props.to);
    const messageNotesActiveClass = message.notes && message.notes.length > 0 ? 'asdcs-active' : '';
    const combinedValue = this.getMessageSpec(message);

    const lifelineOptions = [];
    for (const lifeline of this.props.model.unwrap().diagram.lifelines) {
      lifelineOptions.push(<option
        key={lifeline.id}
        value={lifeline.id}
      >
        {lifeline.name}
      </option>);
    }

    const activeClass = (this.state.active || this.props.active) ? 'asdcs-active' : '';
    const { connectDragSource, connectDropTarget } = this.props;
    return connectDragSource(connectDropTarget(
      <div
        className={`asdcs-designer-message ${activeClass}`}
        data-id={message.id}
        onMouseEnter={this.onMouseEnter}
        onMouseLeave={this.onMouseLeave}
      >

        <table className="asdcs-designer-layout asdcs-designer-message-row1">
          <tbody>
            <tr>
              <td>
                <div className="asdcs-designer-sort asdcs-designer-icon">
                  <Icon glyph={iconHandle} />
                </div>
              </td>
              <td>
                <div className="asdcs-designer-message-index">{message.index}.</div>
              </td>
              <td>
                <div className="asdcs-designer-message-name">
                  <input
                    type="text"
                    className="asdcs-editable"
                    value={this.state.name}
                    placeholder="Unnamed"
                    onBlur={this.onBlurName}
                    onChange={this.onChangeName}
                  />
                </div>
              </td>
              <td>
                <div className="asdcs-designer-actions">
                  <div
                    className="asdcs-designer-settings asdcs-designer-icon"
                    onClick={this.onClickActions}
                  >
                    <Icon glyph={iconSettings} />
                  </div>
                  <div
                    className={`asdcs-designer-notes asdcs-designer-icon ${messageNotesActiveClass}`}
                    onClick={this.onClickNotes}
                  >
                    <Icon glyph={iconNotes} />
                  </div>
                  <div
                    className="asdcs-designer-delete asdcs-designer-icon"
                    onClick={this.onClickDelete}
                  >
                    <Icon glyph={iconDelete} />
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <table className="asdcs-designer-layout asdcs-designer-message-row2">
          <tbody>
            <tr>
              <td>
                <select
                  onChange={this.onChangeFrom}
                  className="asdcs-designer-select-message-from"
                  value={from.id}
                  onChange={this.onChangeFrom}
                >
                  options={lifelineOptions}
                </select>
              </td>
              <td>
                <select
                  onChange={this.onChangeFrom}
                  className="asdcs-designer-select-message-type"
                  value={combinedValue}
                  onChange={this.onChangeType}
                >
                  <option value="REQUEST_SYNC">⇾</option>
                  <option value="REQUEST_ASYNC">→</option>
                  <option value="RESPONSE">⇠</option>
                </select>
              </td>
              <td>
                <select
                  onChange={this.onChangeFrom}
                  className="asdcs-designer-select-message-to"
                  value={to.id}
                  onChange={this.onChangeTo}
                >
                  options={lifelineOptions}
                </select>
              </td>
            </tr>
          </tbody>
        </table>

      </div>
    ));
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render view.
   * @returns {*}
   * @private
   */
  renderReactSelect() {

    const message = this.props.message;
    const from = this.props.from;
    const to = Common.assertNotNull(this.props.to);
    const messageNotesActiveClass = message.notes && message.notes.length > 0 ? 'asdcs-active' : '';
    const combinedValue = this.getMessageSpec(message);

    const lifelineOptions = [];
    for (const lifeline of this.props.model.unwrap().diagram.lifelines) {
      lifelineOptions.push({
        value: lifeline.id,
        label: lifeline.name,
      });
    }

    const activeClass = (this.state.active || this.props.active) ? 'asdcs-active' : '';
    const { connectDragSource, connectDropTarget } = this.props;
    return connectDragSource(connectDropTarget(

      <div
        className={`asdcs-designer-message ${activeClass}`}
        data-id={message.id}
        onMouseEnter={this.onMouseEnter}
        onMouseLeave={this.onMouseLeave}
      >

        <table className="asdcs-designer-layout asdcs-designer-message-row1">
          <tbody>
            <tr>
              <td>
                <div className="asdcs-designer-sort asdcs-designer-icon">
                  <Icon glyph={iconHandle} />
                </div>
              </td>
              <td>
                <div className="asdcs-designer-message-index">{message.index}.</div>
              </td>
              <td>
                <div className="asdcs-designer-message-name">
                  <input
                    type="text"
                    className="asdcs-editable"
                    value={this.state.name}
                    placeholder="Unnamed"
                    onBlur={this.onBlurName}
                    onChange={this.onChangeName}
                  />
                </div>
              </td>
              <td>
                <div className="asdcs-designer-actions">
                  <div
                    className="asdcs-designer-settings asdcs-designer-icon"
                    onClick={this.onClickActions}
                  >
                    <Icon glyph={iconSettings} />
                  </div>
                  <div
                    className={`asdcs-designer-notes asdcs-designer-icon ${messageNotesActiveClass}`}
                    onClick={this.onClickNotes}
                  >
                    <Icon glyph={iconNotes} />
                  </div>
                  <div
                    className="asdcs-designer-delete asdcs-designer-icon"
                    onClick={this.onClickDelete}
                  >
                    <Icon glyph={iconDelete} />
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>

        <table className="asdcs-designer-layout asdcs-designer-message-row2">
          <tbody>
            <tr>
              <td>
                <Select
                  className="asdcs-editable-select asdcs-designer-editable-message-from"
                  openOnFocus
                  clearable={false}
                  searchable={false}
                  value={from.id}
                  onChange={this.onChangeFrom}
                  options={lifelineOptions}
                />
              </td>
              <td>
                <Select
                  className="asdcs-editable-select asdcs-designer-editable-message-type"
                  openOnFocus
                  clearable={false}
                  searchable={false}
                  value={combinedValue}
                  onChange={this.onChangeType}
                  options={this.combinedOptions}
                  optionRenderer={this.renderOption}
                  valueRenderer={this.renderOption}
                />
              </td>
              <td>
                <Select
                  className="asdcs-editable-select asdcs-designer-editable-message-to"
                  openOnFocus
                  clearable={false}
                  searchable={false}
                  value={to.id}
                  onChange={this.onChangeTo}
                  options={lifelineOptions}
                />
              </td>

            </tr>
          </tbody>
        </table>

      </div>
    ));
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  render() {
    const options = this.props.application.getOptions();
    if (options.useHtmlSelect) {
      return this.renderHTMLSelect();
    }
    return this.renderReactSelect();
  }
}

/**
 * Declare properties.
 * @type {{designer: *, message: *, from: *, to: *, model: *, connectDragSource: *}}
 */
Message.propTypes = {
  application: React.PropTypes.object.isRequired,
  designer: React.PropTypes.object.isRequired,
  message: React.PropTypes.object.isRequired,
  active: React.PropTypes.bool.isRequired,
  from: React.PropTypes.object.isRequired,
  to: React.PropTypes.object.isRequired,
  model: React.PropTypes.object.isRequired,
  index: React.PropTypes.number.isRequired,
  messages: React.PropTypes.object.isRequired,
  connectDragSource: React.PropTypes.func.isRequired,
  connectDropTarget: React.PropTypes.func.isRequired,
};

/** DND. */
const source = {
  beginDrag(props) {
    return {
      id: props.id,
      index: props.index,
    };
  },
};

/** DND. */
const sourceCollect = function collection(connect, monitor) {
  return {
    connectDragSource: connect.dragSource(),
    isDragging: monitor.isDragging(),
  };
};


/** DND. */
const target = {
  drop(props, monitor, component) {
    Common.assertNotNull(props);
    Common.assertNotNull(monitor);
    const decorated = component.getDecoratedComponentInstance();
    if (decorated) {
      const messages = decorated.props.messages;
      if (messages) {
        const dragIndex = monitor.getItem().index;
        const hoverIndex = messages.getHoverIndex();
        messages.onDrop(dragIndex, hoverIndex);
      }
    }
  },
  hover(props, monitor, component) {
    Common.assertNotNull(props);
    Common.assertNotNull(monitor);
    if (component) {
      const decorated = component.getDecoratedComponentInstance();
      if (decorated) {
        decorated.props.messages.setHoverIndex(decorated.props.index);
      }
    }
  },
};

/** DND. */
function targetCollect(connect, monitor) {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver(),
  };
}

const wrapper = DragSource('message', source, sourceCollect)(Message);
export default DropTarget(['message', 'message-new'], target, targetCollect)(wrapper);
