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

/* eslint-disable no-console */

import Common from './Common';

/**
 * Logger, to allow calls to console.log during development, but
 * disable them for production.
 */
export default class Logger {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * No-op call so that we can leave imports in place,
   * even when there's no debugging.
   */
  static noop() {
    // Nothing.
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Set debug level.
   * @param level threshold.
   */
  static setLevel(level) {
    this.level = Logger.OFF;
    if (Common.getType(level) === 'Number') {
      this.level = level;
    } else {
      this.level = Logger[level];
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get debug level.
   * @returns {number|*}
   */
  static getLevel() {
    return this.level;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Write DEBUG-level log.
   * @param msg message or tokens.
   */
  static debug(...msg) {
    if (this.level >= Logger.DEBUG) {
      const out = this.serialize(msg);
      console.info(`ASDCS [DEBUG] ${out}`);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Write INFO-level log.
   * @param msg message or tokens.
   */
  static info(...msg) {
    if (this.level >= Logger.INFO) {
      const out = this.serialize(msg);
      console.info(`ASDCS [INFO] ${out}`);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Write debug.
   * @param msg message or tokens.
   */
  static warn(msg) {
    if (this.level >= Logger.WARN) {
      const out = this.serialize(msg);
      console.warn(`ASDCS [WARN] ${out}`);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Write error.
   * @param msg message or tokens.
   */
  static error(...msg) {
    if (this.level >= Logger.ERROR) {
      const out = this.serialize(msg);
      console.error(`ASDCS [ERROR] ${out}`);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Serialize msg.
   * @param msg message or tokens.
   * @returns {string}
   */
  static serialize(...msg) {
    let out = '';
    msg.forEach((token) => {
      out = `${out}${token}`;
    });
    return out;
  }
}

// /////////////////////////////////////////////////////////////////////////////////////////////////

Logger.OFF = 0;
Logger.ERROR = 1;
Logger.WARN = 2;
Logger.INFO = 3;
Logger.DEBUG = 4;
Logger.level = Logger.OFF;
