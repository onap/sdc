/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

/**

 This code was copy from collections.ts lib
 https://github.com/basarat/typescript-collections
 **/
'use strict';

// Used internally by dictionary
interface IDictionaryPair<K, V> {
    key:K;
    value:V;
}

export class Dictionary<K, V> {

    /**
     * Object holding the key-value pairs.
     * @type {Object}
     * @private
     */
    private table:{ [key:string]:IDictionaryPair<K, V> };
    //: [key: K] will not work since indices can only by strings in javascript and typescript enforces this.

    /**
     * Number of elements in the list.
     * @type {number}
     * @private
     */
    private nElements:number;

    /**
     * Function used to convert keys to strings.
     * @type {function(Object):string}
     * @private
     */
    private toStr:(key:K) => string;


    /**
     * Creates an empty dictionary.
     * @class <p>Dictionaries map keys to values; each key can map to at most one value.
     * This implementation accepts any kind of objects as keys.</p>
     *
     * <p>If the keys are custom objects a function which converts keys to unique
     * strings must be provided. Example:</p>
     * <pre>
     * function petToString(pet) {
       *  return pet.name;
       * }
     * </pre>
     * @constructor
     * @param {function(Object):string=} toStrFunction optional function used
     * to convert keys to strings. If the keys aren"t strings or if toString()
     * is not appropriate, a custom function which receives a key and returns a
     * unique string must be provided.
     */
    constructor(toStrFunction?:(key:K) => string) {
        this.table = {};
        this.nElements = 0;
        this.toStr = toStrFunction || this.defaultToString;
    }


    /**
     copy from angular.js  isUndefined
     */
    private isUndefined = (value:any):boolean => {
        return typeof value === 'undefined';
    }

    defaultToString = (item:any):string => {
        return item.toString();
    }

    /**
     * Returns the value to which this dictionary maps the specified key.
     * Returns undefined if this dictionary contains no mapping for this key.
     * @param {Object} key key whose associated value is to be returned.
     * @return {*} the value to which this dictionary maps the specified key or
     * undefined if the map contains no mapping for this key.
     */
    getValue = (key:K):V => {
        let pair:IDictionaryPair<K, V> = this.table[this.toStr(key)];
        if (this.isUndefined(pair)) {
            return undefined;
        }
        return pair.value;
    }


    /**
     * Associates the specified value with the specified key in this dictionary.
     * If the dictionary previously contained a mapping for this key, the old
     * value is replaced by the specified value.
     * @param {Object} key key with which the specified value is to be
     * associated.
     * @param {Object} value value to be associated with the specified key.
     * @return {*} previous value associated with the specified key, or undefined if
     * there was no mapping for the key or if the key/value are undefined.
     */
    setValue = (key:K, value:V):V => {

        if (this.isUndefined(key) || this.isUndefined(value)) {
            return undefined;
        }

        let ret:V;
        let k = this.toStr(key);
        let previousElement:IDictionaryPair<K, V> = this.table[k];
        if (this.isUndefined(previousElement)) {
            this.nElements++;
            ret = undefined;
        } else {
            ret = previousElement.value;
        }
        this.table[k] = {
            key: key,
            value: value
        };
        return ret;
    }

    /**
     * Removes the mapping for this key from this dictionary if it is present.
     * @param {Object} key key whose mapping is to be removed from the
     * dictionary.
     * @return {*} previous value associated with specified key, or undefined if
     * there was no mapping for key.
     */
    remove = (key:K):V => {
        let k = this.toStr(key);
        let previousElement:IDictionaryPair<K, V> = this.table[k];
        if (!this.isUndefined(previousElement)) {
            delete this.table[k];
            this.nElements--;
            return previousElement.value;
        }
        return undefined;
    }

    /**
     * Returns an array containing all of the keys in this dictionary.
     * @return {Array} an array containing all of the keys in this dictionary.
     */
    keys = ():K[] => {
        let array:K[] = [];
        for (let name in this.table) {
            if (this.table.hasOwnProperty(name)) {
                let pair:IDictionaryPair<K, V> = this.table[name];
                array.push(pair.key);
            }
        }
        return array;
    }

    /**
     * Returns an array containing all of the values in this dictionary.
     * @return {Array} an array containing all of the values in this dictionary.
     */
    values = ():V[] => {
        let array:V[] = [];
        for (let name in this.table) {
            if (this.table.hasOwnProperty(name)) {
                let pair:IDictionaryPair<K, V> = this.table[name];
                array.push(pair.value);
            }
        }
        return array;
    }

    /**
     * Executes the provided function once for each key-value pair
     * present in this dictionary.
     * @param {function(Object,Object):*} callback function to execute, it is
     * invoked with two arguments: key and value. To break the iteration you can
     * optionally return false.
     */
    forEach = (callback:(key:K, value:V) => any):void => {
        for (let name in this.table) {
            if (this.table.hasOwnProperty(name)) {
                let pair:IDictionaryPair<K, V> = this.table[name];
                let ret = callback(pair.key, pair.value);
                if (ret === false) {
                    return;
                }
            }
        }
    }

    /**
     * Returns true if this dictionary contains a mapping for the specified key.
     * @param {Object} key key whose presence in this dictionary is to be
     * tested.
     * @return {boolean} true if this dictionary contains a mapping for the
     * specified key.
     */
    containsKey = (key:K):boolean => {
        return !this.isUndefined(this.getValue(key));
    }

    /**
     * Removes all mappings from this dictionary.
     * @this {Dictionary}
     */
    clear = () => {

        this.table = {};
        this.nElements = 0;
    }

    /**
     * Returns the number of keys in this dictionary.
     * @return {number} the number of key-value mappings in this dictionary.
     */
    size = ():number => {
        return this.nElements;
    }

    /**
     * Returns true if this dictionary contains no mappings.
     * @return {boolean} true if this dictionary contains no mappings.
     */
    isEmpty = ():boolean => {
        return this.nElements <= 0;
    }

    toString = ():string => {
        let toret = "{";
        this.forEach((k, v) => {
            toret = toret + "\n\t" + k.toString() + " : " + v.toString();
        });
        return toret + "\n}";
    }
} // End of dictionary

