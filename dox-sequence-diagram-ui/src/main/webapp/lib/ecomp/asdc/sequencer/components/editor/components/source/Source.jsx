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

/**
 * Editor view, aggregating the designer, the code editor, the toolbar.
 */
export default class Source extends React.Component {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct view.
   */
  constructor(props, context) {
    super(props, context);
    this.demo = this.props.application.getOptions().demo;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Set JSON mode.
   * @param json JSON (stringified) code.
   */
  setJSON(json = '') {
    if (this.textarea) {
      this.textarea.value = json;
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Set YAML mode.
   * @param yaml YAML code.
   */
  setYAML(yaml = '') {
    if (this.textarea) {
      this.textarea.value = yaml;
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  componentDidMount() {
    /*
    this.cm = CodeMirror.fromTextArea(this.textarea, {
      lineNumbers: true,
      readOnly: true,
    });
    */
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Render to DOM.
   */
  render() {
    return (
      <div className="asdcs-editor-code">
        <textarea ref={(r) => { this.textarea = r; }}></textarea>
      </div>
    );
  }
}

Source.propTypes = {
  application: React.PropTypes.object.isRequired,
};

