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
import IntlObj from 'intl';
import IntlMessageFormatObj from 'intl-messageformat';
import IntlRelativeFormatObj from 'intl-relativeformat';
import createFormatCacheObj from 'intl-format-cache';
import i18nJson from 'i18nJson';

/*
	Intl libs are using out dated transpailer from ecmascript6.
*  TODO: As soon as they fix it, remove this assignments!!!
* */
var Intl               = window.Intl || IntlObj.default,
	IntlMessageFormat  = IntlMessageFormatObj.default,
	IntlRelativeFormat = IntlRelativeFormatObj.default,
	createFormatCache  = createFormatCacheObj.default;

var i18nData;

if(i18nJson) {
	i18nData = i18nJson.dataWrapperArr[i18nJson.i18nDataIdx];
}


/*extract locale*/
var _locale = window.localStorage && localStorage.getItem('user_locale');
if(!_locale) {
	if(window.navigator) {
		_locale = navigator.language || navigator.userLanguage;

		//For now removing the dashes from the language.
		let indexOfDash = _locale.indexOf('-');
		if(-1 !== indexOfDash) {
			_locale = _locale.substr(0, indexOfDash);
		}
	}
	if(!_locale) {
		_locale = 'en';
	}
}

var _localeUpper = _locale.toUpperCase();

var i18n = {

	_locale: _locale,
	_localeUpper: _localeUpper,
	_i18nData: i18nData || {},

	number(num) {
		return createFormatCache(Intl.NumberFormat)(this._locale).format(num);
	},

	date(date, options, relativeDates) {
		if (undefined === relativeDates || relativeDates) {
			return this.dateRelative(date, options);
		} else {
			return this.dateNormal(date, options);
		}
	},

	dateNormal(date, options) {
		return createFormatCache(Intl.DateTimeFormat)(this._locale, options).format(date);
	},

	dateRelative(date, options) {
		return createFormatCache(IntlRelativeFormat)(this._locale, options).format(date);
	},

	message(messageId, options) {
		return createFormatCache(IntlMessageFormat)(this._i18nData[messageId] || String(messageId), this._locale).format(options);
	},

	getLocale() {
		return this._locale;
	},

	getLocaleUpper() {
		return this._localeUpper;
	},

	setLocale(locale) {
		localStorage.setItem('user_locale', locale);
		window.location.reload();
	}

};

function i18nWrapper() {
	return i18nWrapper.message.apply(i18nWrapper, arguments);
}

/*replace with some kind of extend method*/
var prop, propKey;
for (propKey in i18n) {
	prop = i18n[propKey];
	if (typeof prop === 'function') {
		prop = prop.bind(i18nWrapper);
	}
	i18nWrapper[propKey] = prop;
}


export default i18nWrapper;
