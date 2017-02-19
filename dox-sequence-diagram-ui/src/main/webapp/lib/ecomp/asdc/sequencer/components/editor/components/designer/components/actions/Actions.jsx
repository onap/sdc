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

import Common from '../../../../../../common/Common';
import Logger from '../../../../../../common/Logger';

import Icon from '../../../../../icons/Icon';
import iconSettings from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/settings.svg';
import iconExpanded from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/expanded.svg';
import iconCollapsed from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/collapsed.svg';
import iconOccurrenceDefault from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/occurrence-default.svg';
import iconOccurrenceStart from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/occurrence-start.svg';
import iconOccurrenceStop from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/occurrence-stop.svg';
import iconFragmentDefault from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/fragment-default.svg';
import iconFragmentStart from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/fragment-start.svg';
import iconFragmentStop from '../../../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/fragment-stop.svg';

/**
 * Action menu view.
 */
export default class Actions extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct view.
   * @param props element properties.
   * @param context react context.
   */
  constructor(props, context) {
    super(props, context);

    Logger.noop();

    this.state = {
      id: undefined,
      visible: false,
    };

    // Bindings.

    this.show = this.show.bind(this);

    this.onClickOccurrenceToggle = this.onClickOccurrenceToggle.bind(this);
    this.onClickOccurrenceFrom = this.onClickOccurrenceFrom.bind(this);
    this.onClickOccurrenceTo = this.onClickOccurrenceTo.bind(this);

    this.onClickFragmentToggle = this.onClickFragmentToggle.bind(this);
    this.onChangeFragmentGuard = this.onChangeFragmentGuard.bind(this);
    this.onChangeFragmentOperator = this.onChangeFragmentOperator.bind(this);

    this.onMouseOut = this.onMouseOut.bind(this);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Show for message.
   * @param id message ID.
   * @param position xy coordinates.
   */
  show(id, position) {
    const message = this.props.model.getMessageById(id);

    let occurrencesToggle = false;
    let fragmentToggle = false;
    if (message) {

      message.occurrences = message.occurrences || { start: [], stop: [] };
      message.occurrences.start = message.occurrences.start || [];
      message.occurrences.stop = message.occurrences.stop || [];
      message.fragment = message.fragment || {};
      message.fragment.start = message.fragment.start || false;
      message.fragment.stop = message.fragment.stop || false;
      message.fragment.guard = message.fragment.guard || '';
      message.fragment.operator = message.fragment.operator || '';

      const mo = message.occurrences;
      occurrencesToggle = (mo.start.length > 0 || mo.stop.length > 0);

      const mf = message.fragment;
      fragmentToggle = (mf.start || mf.stop);
    }

    this.setState({
      id,
      message,
      occurrencesToggle,
      fragmentToggle,
      visible: true,
      x: position.x,
      y: position.y,
    });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Toggle occurrence state.
   */
  onClickOccurrenceToggle() {
    const message = this.state.message;
    if (message) {
      const oFromState = Actions.getOccurrenceState(message.occurrences, message.from);
      const oToState = Actions.getOccurrenceState(message.occurrences, message.to);
      const oExpanded = oFromState > 0 || oToState > 0;
      if (oExpanded) {
        this.setState({ occurrencesExpanded: true });
      } else {
        const occurrencesExpanded = !this.state.occurrencesExpanded;
        this.setState({ occurrencesExpanded });
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle menu click.
   */
  onClickOccurrenceFrom() {
    const message = this.state.message;
    if (message) {
      Actions._toggleOccurrence(message.occurrences, message.from);
    }
    this.setState({ message });
    this.props.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle menu click.
   */
  onClickOccurrenceTo() {
    const message = this.state.message;
    if (message) {
      Actions._toggleOccurrence(message.occurrences, message.to);
    }
    this.setState({ message });
    this.props.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Toggle fragment.
   */
  onClickFragmentToggle() {
    const message = this.state.message;
    if (message) {
      Actions._toggleFragment(message.fragment);
    }
    this.setState({ message });
    this.props.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle menu click.
   * @param event update event.
   */
  onChangeFragmentGuard(event) {
    const message = this.state.message;
    if (message) {
      const options = this.props.application.getOptions();
      message.fragment.guard = Common.sanitizeText(event.target.value, options, 'guard');
    }
    this.setState({ message });
    this.props.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle menu click.
   * @param value updated value.
   */
  onChangeFragmentOperator(value) {
    const message = this.state.message;
    if (message) {
      message.fragment.operator = value.value;
    }
    this.setState({ message });
    this.props.application.renderDiagram();
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Handle mouse movement.
   */
  onMouseOut() {
    this.setState({ id: -1, visible: false, x: 0, y: 0 });
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render view.
   * @returns {XML}
   */
  render() {

    const actionsStyles = { };
    const message = this.state.message;
    if (!message || !this.state.visible) {

      // Invisible.

      return (<div className="asdcs-actions" ></div>);
    }

    // Position and display.

    actionsStyles.display = 'block';
    actionsStyles.left = this.state.x - 10;
    actionsStyles.top = this.state.y - 10;

    const oFromState = Actions.getOccurrenceState(message.occurrences, message.from);
    const oToState = Actions.getOccurrenceState(message.occurrences, message.to);
    const fState = Actions.getFragmentState(message.fragment);

    const oExpanded = this.state.occurrencesExpanded || (oFromState > 0) || (oToState > 0);
    const oAuxClassName = oExpanded ? '' : 'asdcs-hidden';

    const fExpanded = fState !== 0;
    const fAuxClassName = fExpanded ? '' : 'asdcs-hidden';

    const fragmentOperatorOptions = [{
      value: 'alt',
      label: 'Alternate',
    }, {
      value: 'opt',
      label: 'Optional',
    }, {
      value: 'loop',
      label: 'Loop',
    }];

    const operator = message.fragment.operator || 'alt';

    return (
      <div
        className="asdcs-actions"
        style={actionsStyles}
        onMouseLeave={this.onMouseOut}
      >
        <div className="asdcs-actions-header">
          <div className="asdcs-actions-icon">
            <Icon glyph={iconSettings} />
          </div>
        </div>

        <div className="asdcs-actions-options">

          <div className="asdcs-actions-optiongroup asdcs-actions-optiongroup-occurrence">
            <div
              className="asdcs-actions-option asdcs-actions-option-occurrence-toggle"
              onClick={this.onClickOccurrenceToggle}
            >
              <span className="asdcs-label">Occurrence</span>
              <div className="asdcs-actions-state">
                <Icon glyph={iconCollapsed} className={oExpanded ? 'asdcs-hidden' : ''} />
                <Icon glyph={iconExpanded} className={oExpanded ? '' : 'asdcs-hidden'} />
              </div>
            </div>
          </div>

          <div
            className={`asdcs-actions-option asdcs-actions-option-occurrence-from ${oAuxClassName}`}
            onClick={this.onClickOccurrenceFrom}
          >
            <span className="asdcs-label">From</span>
            <div className="asdcs-actions-state">
              <span className="asdcs-annotation"></span>
              <Icon glyph={iconOccurrenceDefault} className={oFromState === 0 ? '' : 'asdcs-hidden'} />
              <Icon glyph={iconOccurrenceStart} className={oFromState === 1 ? '' : 'asdcs-hidden'} />
              <Icon glyph={iconOccurrenceStop} className={oFromState === 2 ? '' : 'asdcs-hidden'} />
            </div>
          </div>

          <div
            className={`asdcs-actions-option asdcs-actions-option-occurrence-to ${oAuxClassName}`}
            onClick={this.onClickOccurrenceTo}
          >
            <span className="asdcs-label">To</span>
            <div className="asdcs-actions-state">
              <span className="asdcs-annotation"></span>
              <Icon glyph={iconOccurrenceDefault} className={oToState === 0 ? '' : 'asdcs-hidden'} />
              <Icon glyph={iconOccurrenceStart} className={oToState === 1 ? '' : 'asdcs-hidden'} />
              <Icon glyph={iconOccurrenceStop} className={oToState === 2 ? '' : 'asdcs-hidden'} />
            </div>
          </div>

          <div className="asdcs-actions-optiongroup asdcs-actions-optiongroup-fragment">
            <div
              className="asdcs-actions-option asdcs-actions-fragment-toggle"
              onClick={this.onClickFragmentToggle}
            >
              <span className="asdcs-label">Fragment</span>
              <div className="asdcs-actions-state">
                <span className="asdcs-annotation"></span>
                <Icon glyph={iconFragmentDefault} className={fState === 0 ? '' : 'asdcs-hidden'} />
                <Icon glyph={iconFragmentStart} className={fState === 1 ? '' : 'asdcs-hidden'} />
                <Icon glyph={iconFragmentStop} className={fState === 2 ? '' : 'asdcs-hidden'} />
              </div>
            </div>
          </div>

          <div className={`asdcs-actions-option asdcs-actions-fragment-operator ${fAuxClassName}`}>
            <div className="asdcs-label">Operator</div>
            <div className="asdcs-value">
              <Select
                className="asdcs-editable-select"
                openOnFocus
                clearable={false}
                searchable={false}
                value={operator}
                onChange={this.onChangeFragmentOperator}
                options={fragmentOperatorOptions}
              />
            </div>
          </div>

          <div className={`asdcs-actions-option asdcs-actions-fragment-guard ${fAuxClassName}`}>
            <div className="asdcs-label">Guard</div>
            <div className="asdcs-value">
              <input
                className="asdcs-editable"
                type="text"
                size="20"
                maxLength="80"
                value={message.fragment.guard}
                placeholder="Condition"
                onChange={this.onChangeFragmentGuard}
              />
            </div>
          </div>

        </div>

        <div className="asdcs-actions-footer"></div>

      </div>

    );
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Toggle through three occurrence states on click.
   * @param occurrence occurrences state, updated as side-effect.
   * @param lifelineId message end that's being toggled.
   * @private
   */
  static _toggleOccurrence(occurrence, lifelineId) {
    const o = occurrence;

    const rm = function rm(array, value) {
      const index = array.indexOf(value);
      if (index !== -1) {
        array.splice(index, 1);
      }
    };

    const add = function add(array, value) {
      if (array.indexOf(value) === -1) {
        array.push(value);
      }
    };

    if (o.start && o.start.indexOf(lifelineId) !== -1) {
      // Start -> stop.
      rm(o.start, lifelineId);
      add(o.stop, lifelineId);
    } else if (o.stop && o.stop.indexOf(lifelineId) !== -1) {
      // Stop -> default.
      rm(o.start, lifelineId);
      rm(o.stop, lifelineId);
    } else {
      // Default -> start.
      add(o.start, lifelineId);
      rm(o.stop, lifelineId);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Toggle fragment setting on click.
   * @param fragment
   * @private
   **/
  static _toggleFragment(fragment) {
    const f = fragment;
    if (f.start === true) {
      f.start = false;
      f.stop = true;
    } else if (f.stop === true) {
      f.stop = false;
      f.start = false;
    } else {
      f.start = true;
      f.stop = false;
    }
    f.guard = '';
    f.operator = '';
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get ternary occurrences state.
   * @param o occurrences.
   * @param lifelineId from/to lifeline ID.
   * @returns {number}
   * @private
   */
  static getOccurrenceState(o, lifelineId) {
    if (o.start.indexOf(lifelineId) !== -1) {
      return 1;
    }
    if (o.stop.indexOf(lifelineId) !== -1) {
      return 2;
    }
    return 0;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get ternary fragment state.
   * @param f fragment.
   * @returns {number}
   * @private
   */
  static getFragmentState(f) {
    if (f.start) {
      return 1;
    }
    if (f.stop) {
      return 2;
    }
    return 0;
  }
}

/** Element properties. */
Actions.propTypes = {
  application: React.PropTypes.object.isRequired,
  model: React.PropTypes.object.isRequired,
};
