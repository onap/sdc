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

import _merge from 'lodash/merge';

import Logger from './Logger';

/**
 * A wrapper for an options object. User-supplied options are merged with defaults,
 * and the result -- runtime options -- are available by calling #getOptions().
 */
export default class Options {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Construct options, applying defaults.
   * @param options optional override options.
   */
  constructor(options = {}) {
    this.options = _merge({}, Options.DEFAULTS, options);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Unwrap options.
   * @returns {*}
   */
  unwrap() {
    return this.options;
  }
}

// /////////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Default options, overridden by anything of the same name.
 */
Options.DEFAULTS = {
  log: {
    level: Logger.WARN,
  },
  demo: false,
  useHtmlSelect: true,
  diagram: {
    svg: {
      x: 0,
      y: 0,
      width: 1600,
      height: 1200,
      margin: 50,
      floodColor: '#009fdb',
      scale: {
        height: true,
        width: true,
        minimum: 0.25,
      },
    },
    title: {
      height: 0,
    },
    metadata: false,
    lifelines: {
      header: {
        height: 225,
        width: 350,
        wrapWords: 14,
        wrapLines: 18,
        maxLines: 5,
      },
      occurrences: {
        marginTop: 50,
        marginBottom: 75,
        foreshortening: 5,
        width: 50,
      },
      spacing: {
        horizontal: 400,
        vertical: 400,
      },
    },
    messages: {
      label: {
        wrapWords: 14,
        wrapLines: 18,
        maxLines: 4,
      },
    },
    fragments: {
      leftMargin: 150,
      topMargin: 200,
      widthMargin: 300,
      heightMargin: 350,
      label: {
        wrapWords: 50,
        wrapLines: 50,
        maxLines: 2,
      },
    },
  },
  validation: {
    lifeline: {
      maxLength: 100,
      defaultValue: '',
      replace: /[^\-\.\+ &%#@\?\(\)\[\]<>\w\d]/g,
    },
    message: {
      maxLength: 100,
      defaultValue: '',
      replace: /[^\-\.\+ &%#@\?\(\)\[\]<>\w\d]/g,
    },
    notes: {
      maxLength: 255,
      defaultValue: '',
    },
    guard: {
      maxLength: 80,
      defaultValue: '',
      replace: /[^\-\.\+ &%#@\?\(\)\[\]<>\w\d]/g,
    },
  },
};
