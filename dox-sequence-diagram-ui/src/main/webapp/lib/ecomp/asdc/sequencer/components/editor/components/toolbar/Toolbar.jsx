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

import Common from '../../../../common/Common';

import iconPlus from '../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/plus.svg';
import iconOpen from '../../../../../../../../res/ecomp/asdc/sequencer/sprites/icon/open.svg';

/**
 * Toolbar view. Buttons offered in the toolbar depend on the mode. Unless in demo mode,
 * all you get are the buttons for toggling between JSON/YAML/Designer.
 */
export default class Toolbar extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct view.
   */
  constructor(props, context) {
    super(props, context);
    this.application = Common.assertType(this.props.application, 'Object');
    this.editor = Common.assertType(this.props.editor, 'Object');
    this.mode = 'design';
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Set editor mode, one of {design, json, yaml}.
   * @param mode
   */
  setMode(mode = 'design') {
    this.mode = mode;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render into the DOM.
   */
  render() {

    const demo = this.application.getOptions().demo;
    const demoCss = demo ? '' : 'asdc-hide';

    return (
      <div className={`asdcs-editor-toolbar ${demoCss}`}>
        <div className="asdcs-editor-toolbar-demo">
          <button className="asdcs-button-new" data-title="New sequence">
            <svg>
              <use xlinkHref={iconPlus} className="asdcs-icon" />
            </svg>
          </button>
          <button className="asdcs-button-open" data-title="Open sequence">
            <svg>
              <use xlinkHref={iconOpen} className="asdcs-icon" />
            </svg>
          </button>
          <button className="asdcs-button-save" data-title="Save checkpoint">
            <svg>
              <use xlinkHref="#icon--save" className="asdcs-icon" />
            </svg>
          </button>
          <button className="asdcs-button-validate" data-title="Validate">
            <svg>
              <use xlinkHref="#icon--validate" className="asdcs-icon" />
            </svg>
          </button>
          <button className="asdcs-button-download" data-title="Download">
            <svg>
              <use xlinkHref="#icon--download" className="asdcs-icon" />
            </svg>
          </button>
          <button className="asdcs-button-upload" data-title="Upload">
            <svg>
              <use xlinkHref="#icon--upload" className="asdcs-icon" />
            </svg>
          </button>
        </div>
        <div className="asdcs-editor-toolbar-toggle">
          <button className="asdcs-button-design asdcs-button-mode asdcs-button-toggle-left">
            Design
          </button>
          <button className="asdcs-button-json asdcs-button-mode asdcs-button-toggle-center">
            JSON
          </button>
          <button className="asdcs-button-yaml asdcs-button-mode asdcs-button-toggle-right">
            YAML
          </button>
        </div>
      </div>
    );
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Initialize eventhandlers.
   * @private
   *
  _initEvents() {

    $('button.asdcs-button-open', this.$el).click(() => {
      this._doDemoOpen();
    });

    $('button.asdcs-button-new', this.$el).click(() => {
      this._doDemoNew();
    });

    $('button.asdcs-button-save', this.$el).click(() => {
      this._doDemoSave();
    });

    $('button.asdcs-button-upload', this.$el).click(() => {
      this._doDemoUpload();
    });

    $('button.asdcs-button-download', this.$el).click(() => {
      this._doDemoDownload();
    });

    $('button.asdcs-button-validate', this.$el).click(() => {
      this._doDemoValidate();
    });

    $('button.asdcs-button-json', this.$el).click((e) => {
      if ($(e.target).hasClass('asdcs-active')) {
        return;
      }
      this.editor.toggleToJSON();
    });

    $('button.asdcs-button-yaml', this.$el).click((e) => {
      if ($(e.target).hasClass('asdcs-active')) {
        return;
      }
      this.editor.toggleToYAML();
    });

    $('button.asdcs-button-design', this.$el).click((e) => {
      if ($(e.target).hasClass('asdcs-active')) {
        return;
      }
      this.editor.toggleToDesign();
    });
  }
  */

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Demo action.
   *
  _doDemoOpen() {
    const complete = function complete() {
      const sequencer = this.application.getSequencer();
      const scenarios = sequencer.getDemoScenarios();
      sequencer.setModel(scenarios.getECOMP());
    };
    this.application.showConfirmDialog('[DEMO MODE] Open a canned DEMO sequence ' +
      'via the public #setModel() API?', complete);

  }
  */

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Demo action.
   *
  _doDemoNew() {
    const complete = function complete() {
      const sequencer = this.application.getSequencer();
      sequencer.newModel();
    };
    this.application.showConfirmDialog('[DEMO MODE] Create an empty sequence via the ' +
      'public #newModel() API?', complete);
  }
  */

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Demo action.
   *
  _doDemoSave() {
    const sequencer = this.application.getSequencer();
    Logger.info(`[DEMO MODE] model:\n${JSON.stringify(sequencer.getModel(), null, 4)}`);
    this.application.showInfoDialog('[DEMO MODE] Retrieved model via the public #getModel ' +
      'API and logged its JSON to the console.');
  }
  */

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Demo action.
   *
  _doDemoUpload() {
    const sequencer = this.application.getSequencer();
    const svg = sequencer.getSVG();
    // console.log(`[DEMO MODE] SVG:\n${svg}`);
    const $control = this.$el.closest('.asdcs-control');
    Logger.info(`parent: ${$control.length}`);
    const $form = $('form.asdcs-export', $control);
    Logger.info(`form: ${$form.length}`);
    $('input[name=svg]', $form).val(svg);
    try {
      $form.submit();
    } catch (e) {
      Logger.error(e);
      this.application.showErrorDialog('[DEMO MODE] Export service not available. Retrieved ' +
        'SVG via the public #getSVG API and dumped it to the console.');
    }
  }
  */

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Demo action.
   *
  _doDemoDownload() {
    const json = JSON.stringify(this.application.getSequencer().getModel());
    const $control = this.$el.closest('.asdcs-control');
    const $a = $('<a download="model.json" style="display:none">').appendTo($control);
    $a.attr('href', `data:application/json;charset=utf-8,${encodeURIComponent(json)}`);
    $a[0].click();
    $a.remove();
  }
  */

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Demo action.
   *
  _doDemoValidate() {
    this.application.showInfoDialog('[DEMO MODE] Dumping validation result to the console.');
    const errors = this.application.getModel().validate();
    Logger.info(`[DEMO MODE] Validation: ${JSON.stringify(errors, null, 4)}`);
  }
  */
}

Toolbar.propTypes = {
  application: React.PropTypes.object.isRequired,
  editor: React.PropTypes.object.isRequired,
};
