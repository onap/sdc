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

/**
 * Common operations.
 */
export default class Common {

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Retrieve and start a simple timer. Retrieve elapsed time by calling #ms().
   * @returns {*}
   */
  static timer() {
    const start = new Date().getTime();
    return {
      ms() {
        return (new Date().getTime() - start);
      },
    };
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get datatype, stripping '[object Boolean]' to just 'Boolean'.
   * @param o JS object.
   * @return String like String, Number, Date, Null, Undefined, stuff like that.
   */
  static getType(o) {
    const str = Object.prototype.toString.call(o);
    const prefix = '[object ';
    if (str.substr(str, prefix.length) === prefix) {
      return str.substr(prefix.length, str.length - (prefix.length + 1));
    }
    return str;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Assert that an argument was provided.
   * @param value to be checked.
   * @param message message on assertion failure.
   * @return value.
   */
  static assertNotNull(value, message = 'Unexpected null value') {
    if (!value) {
      throw new Error(message);
    }
    return value;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Assert argument type.
   * @param value to be checked.
   * @param expected expected type string, e,g. Number from [object Number].
   * @return value.
   */
  static assertType(value, expected) {
    const type = this.getType(value);
    if (type !== expected) {
      throw new Error(`Expected type ${expected}, got ${type}`);
    }
    return value;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Assert argument type.
   * @param value to be checked.
   * @param unexpected unexpected type string, e,g. Number from [object Number].
   * @return value.
   */
  static assertNotType(value, unexpected) {
    const type = this.getType(value);
    if (type === unexpected) {
      throw new Error(`Forbidden type "${unexpected}"`);
    }
    return value;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Assert argument is a simple JSON object, and specifically not (something like an) ES6 class.
   * @param value to be checked.
   * @return value.
   */
  static assertPlainObject(value) {
    Common.assertType(value, 'Object');
    // TODO
    /*
    if (!($.isPlainObject(value))) {
      throw new Error(`Expected plain object: ${value}`);
    }
    */
    return value;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Assert argument type.
   * @param value to be checked.
   * @param c expected class.
   * @return value.
   */
  static assertInstanceOf(value, c) {
    Common.assertNotNull(value);
    if (!(value instanceof c)) {
      throw new Error(`Expected instanceof ${c}: ${value}`);
    }
    return value;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Assert that a string matches a regex.
   * @param value value to be tested.
   * @param re pattern to be applied.
   * @return value.
   */
  static assertMatches(value, re) {
    this.assertType(value, 'String');
    this.assertType(re, 'RegExp');
    if (!re.test(value)) {
      throw new Error(`Value ${value} doesn't match pattern ${re}`);
    }
    return value;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Assert the value of a boolean.
   *
   * @param bool to be checked.
   * @param message optional message on assertion failure.
   * @return value.
   */
  static assertThat(bool, message) {
    if (!bool) {
      throw new Error(message || `Unexpected: ${bool}`);
    }
    return bool;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Verify that a value, generally a function arg, is a DOM element.
   * @param value to be checked.
   * @return value.
   */
  static assertHTMLElement(value) {
    if (!Common.isHTMLElement(value)) {
      throw new Error(`Expected HTMLElement: ${value}`);
    }
    return value;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Check whether a value, generally a function arg, is an HTML DOM element.
   * @param o to be checked.
   * @return true if DOM element.
   */
  static isHTMLElement(o) {
    if (typeof HTMLElement === 'object') {
      return o instanceof HTMLElement;
    }
    return o && typeof o === 'object' && o !== null
      && o.nodeType === 1 && typeof o.nodeName === 'string';
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Check if a string is non-empty.
   * @param s string to be checked.
   * @returns false if non-blank string, true otherwise.
   */
  static isBlank(s) {
    if (Common.getType(s) === 'String') {
      return (s.trim().length === 0);
    }
    return true;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Detect dates that are numbers, milli/seconds since epoch..
   *
   * @param n candidate number.
   * @returns {boolean}
   */
  static isNumber(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Parse the text output from a template to a DOM element.
   * @param txt input text.
   * @returns {Element}
   */
  static txt2dom(txt) {
    return new DOMParser().parseFromString(txt, 'image/svg+xml').documentElement;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Recursively convert a DOM element to an SVG (namespaced) element. Otherwise
   * you get HTML elements that *happen* to have SVG names, but which aren't actually SVG.
   *
   * @param node DOM node to be converted.
   * @param svg to be updated.
   * @returns {*} for chaining.
   */
  static dom2svg(node, svg) {

    Common.assertNotType(node, 'String');

    if (node.childNodes && node.childNodes.length > 0) {

      for (const c of node.childNodes) {
        switch (c.nodeType) {
          case document.TEXT_NODE:
            svg.text(c.nodeValue);
            break;
          default:
            break;
        }
      }

      for (const c of node.childNodes) {
        switch (c.nodeType) {
          case document.ELEMENT_NODE:
            Common.dom2svg(c, svg.append(`svg:${c.nodeName.toLowerCase()}`));
            break;
          default:
            break;
        }
      }
    }

    if (node.hasAttributes()) {
      for (let i = 0; i < node.attributes.length; i++) {
        const a = node.attributes.item(i);
        svg.attr(a.name, a.value);
      }
    }

    return svg;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Get the lines to be shown in the label.
   *
   * @param labelText original label text.
   * @param wordWrapAt chars at which to break words.
   * @param lineWrapAt chars at which to wrap.
   * @param maximumLines lines at which to truncate.
   * @returns {Array}
   */
  static tokenize(labelText = '', wordWrapAt, lineWrapAt, maximumLines) {

    let l = labelText;

    // Hyphenate and break long words.

    const regex = new RegExp(`(\\w{${wordWrapAt - 1}})(?=\\w)`, 'g');
    l = l.replace(regex, '$1- ');

    const labelTokens = l.split(/\s+/);
    const lines = [];
    let label = '';
    for (const labelToken of labelTokens) {
      if (label.length > 0) {
        const length = label.length + labelToken.length + 1;
        if (length > lineWrapAt) {
          lines.push(label.trim());
          label = labelToken;
          continue;
        }
      }
      label = `${label} ${labelToken}`;
    }

    if (label) {
      lines.push(label.trim());
    }

    const truncated = lines.slice(0, maximumLines);
    if (truncated.length < lines.length) {
      let finalLine = truncated[maximumLines - 1];
      if (finalLine.length > (lineWrapAt - 4)) {
        finalLine = finalLine.substring(0, lineWrapAt - 4);
      }
      finalLine = `${finalLine} ...`;
      truncated[maximumLines - 1] = finalLine;
    }

    return truncated;
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Brutally sanitize an input string. We have no syntax rules, and hence no specific
   * rules to apply, but we have very few unconstrained fields, so we can implement a
   * crude default and devolve the rest to options.
   * @param value value to be sanitized.
   * @param options control options including validation rules.
   * @param type validation type.
   * @returns {*} sanitized string.
   * @private
   */
  static sanitizeText(value, options, type) {
    const rules = Common.assertNotNull(options.validation[type]);
    let v = value || rules.defaultValue || '';
    if (rules.replace) {
      v = v.replace(rules.replace, '');
    }
    if (v.length > rules.maxLength) {
      v = `${v.substring(0, rules.maxLength)}...`;
    }
    return v;
  }

}
