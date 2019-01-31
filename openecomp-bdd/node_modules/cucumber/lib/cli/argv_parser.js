'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _commander = require('commander');

var _package = require('../../package.json');

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

var _gherkin = require('gherkin');

var _gherkin2 = _interopRequireDefault(_gherkin);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var ArgvParser = function () {
  function ArgvParser() {
    (0, _classCallCheck3.default)(this, ArgvParser);
  }

  (0, _createClass3.default)(ArgvParser, null, [{
    key: 'collect',
    value: function collect(val, memo) {
      memo.push(val);
      return memo;
    }
  }, {
    key: 'mergeJson',
    value: function mergeJson(option) {
      return function (str, memo) {
        var val = void 0;
        try {
          val = JSON.parse(str);
        } catch (error) {
          throw new Error(option + ' passed invalid JSON: ' + error.message + ': ' + str);
        }
        if (!_lodash2.default.isPlainObject(val)) {
          throw new Error(option + ' must be passed JSON of an object: ' + str);
        }
        return _lodash2.default.merge(memo, val);
      };
    }
  }, {
    key: 'mergeTags',
    value: function mergeTags(val, memo) {
      return memo === '' ? '(' + val + ')' : memo + ' and (' + val + ')';
    }
  }, {
    key: 'validateLanguage',
    value: function validateLanguage(val) {
      if (!_lodash2.default.includes(_lodash2.default.keys(_gherkin2.default.DIALECTS), val)) {
        throw new Error('Unsupported ISO 639-1: ' + val);
      }
      return val;
    }
  }, {
    key: 'parse',
    value: function parse(argv) {
      var program = new _commander.Command(_path2.default.basename(argv[1]));

      program.usage('[options] [<DIR|FILE[:LINE]>...]').version(_package.version, '-v, --version').option('-b, --backtrace', 'show full backtrace for errors').option('--compiler <EXTENSION:MODULE>', 'require files with the given EXTENSION after requiring MODULE (repeatable)', ArgvParser.collect, []).option('-d, --dry-run', 'invoke formatters without executing steps').option('--fail-fast', 'abort the run on first failure').option('-f, --format <TYPE[:PATH]>', 'specify the output format, optionally supply PATH to redirect formatter output (repeatable)', ArgvParser.collect, []).option('--format-options <JSON>', 'provide options for formatters (repeatable)', ArgvParser.mergeJson('--format-options'), {}).option('--i18n-keywords <ISO 639-1>', 'list language keywords', ArgvParser.validateLanguage, '').option('--i18n-languages', 'list languages').option('--language <ISO 639-1>', 'provide the default language for feature files', '').option('--name <REGEXP>', 'only execute the scenarios with name matching the expression (repeatable)', ArgvParser.collect, []).option('--no-strict', 'succeed even if there are pending steps').option('-p, --profile <NAME>', 'specify the profile to use (repeatable)', ArgvParser.collect, []).option('-r, --require <FILE|DIR>', 'require files before executing features (repeatable)', ArgvParser.collect, []).option('-t, --tags <EXPRESSION>', 'only execute the features or scenarios with tags matching the expression (repeatable)', ArgvParser.mergeTags, '').option('--world-parameters <JSON>', 'provide parameters that will be passed to the world constructor (repeatable)', ArgvParser.mergeJson('--world-parameters'), {});

      program.on('--help', function () {
        /* eslint-disable no-console */
        console.log('  For more details please visit https://github.com/cucumber/cucumber-js#cli\n');
        /* eslint-enable no-console */
      });

      program.parse(argv);

      return {
        options: program.opts(),
        args: program.args
      };
    }
  }]);
  return ArgvParser;
}();

exports.default = ArgvParser;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9jbGkvYXJndl9wYXJzZXIuanMiXSwibmFtZXMiOlsiQXJndlBhcnNlciIsInZhbCIsIm1lbW8iLCJwdXNoIiwib3B0aW9uIiwic3RyIiwiSlNPTiIsInBhcnNlIiwiZXJyb3IiLCJFcnJvciIsIm1lc3NhZ2UiLCJpc1BsYWluT2JqZWN0IiwibWVyZ2UiLCJpbmNsdWRlcyIsImtleXMiLCJESUFMRUNUUyIsImFyZ3YiLCJwcm9ncmFtIiwiYmFzZW5hbWUiLCJ1c2FnZSIsInZlcnNpb24iLCJjb2xsZWN0IiwibWVyZ2VKc29uIiwidmFsaWRhdGVMYW5ndWFnZSIsIm1lcmdlVGFncyIsIm9uIiwiY29uc29sZSIsImxvZyIsIm9wdGlvbnMiLCJvcHRzIiwiYXJncyJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7Ozs7QUFBQTs7OztBQUNBOztBQUNBOztBQUNBOzs7O0FBQ0E7Ozs7OztJQUVxQkEsVTs7Ozs7Ozs0QkFDSkMsRyxFQUFLQyxJLEVBQU07QUFDeEJBLFdBQUtDLElBQUwsQ0FBVUYsR0FBVjtBQUNBLGFBQU9DLElBQVA7QUFDRDs7OzhCQUVnQkUsTSxFQUFRO0FBQ3ZCLGFBQU8sVUFBU0MsR0FBVCxFQUFjSCxJQUFkLEVBQW9CO0FBQ3pCLFlBQUlELFlBQUo7QUFDQSxZQUFJO0FBQ0ZBLGdCQUFNSyxLQUFLQyxLQUFMLENBQVdGLEdBQVgsQ0FBTjtBQUNELFNBRkQsQ0FFRSxPQUFPRyxLQUFQLEVBQWM7QUFDZCxnQkFBTSxJQUFJQyxLQUFKLENBQ0pMLFNBQVMsd0JBQVQsR0FBb0NJLE1BQU1FLE9BQTFDLEdBQW9ELElBQXBELEdBQTJETCxHQUR2RCxDQUFOO0FBR0Q7QUFDRCxZQUFJLENBQUMsaUJBQUVNLGFBQUYsQ0FBZ0JWLEdBQWhCLENBQUwsRUFBMkI7QUFDekIsZ0JBQU0sSUFBSVEsS0FBSixDQUFVTCxTQUFTLHFDQUFULEdBQWlEQyxHQUEzRCxDQUFOO0FBQ0Q7QUFDRCxlQUFPLGlCQUFFTyxLQUFGLENBQVFWLElBQVIsRUFBY0QsR0FBZCxDQUFQO0FBQ0QsT0FiRDtBQWNEOzs7OEJBRWdCQSxHLEVBQUtDLEksRUFBTTtBQUMxQixhQUFPQSxTQUFTLEVBQVQsU0FBa0JELEdBQWxCLFNBQThCQyxJQUE5QixjQUEyQ0QsR0FBM0MsTUFBUDtBQUNEOzs7cUNBRXVCQSxHLEVBQUs7QUFDM0IsVUFBSSxDQUFDLGlCQUFFWSxRQUFGLENBQVcsaUJBQUVDLElBQUYsQ0FBTyxrQkFBUUMsUUFBZixDQUFYLEVBQXFDZCxHQUFyQyxDQUFMLEVBQWdEO0FBQzlDLGNBQU0sSUFBSVEsS0FBSixDQUFVLDRCQUE0QlIsR0FBdEMsQ0FBTjtBQUNEO0FBQ0QsYUFBT0EsR0FBUDtBQUNEOzs7MEJBRVllLEksRUFBTTtBQUNqQixVQUFNQyxVQUFVLHVCQUFZLGVBQUtDLFFBQUwsQ0FBY0YsS0FBSyxDQUFMLENBQWQsQ0FBWixDQUFoQjs7QUFFQUMsY0FDR0UsS0FESCxDQUNTLGtDQURULEVBRUdDLE9BRkgsbUJBRW9CLGVBRnBCLEVBR0doQixNQUhILENBR1UsaUJBSFYsRUFHNkIsZ0NBSDdCLEVBSUdBLE1BSkgsQ0FLSSwrQkFMSixFQU1JLDRFQU5KLEVBT0lKLFdBQVdxQixPQVBmLEVBUUksRUFSSixFQVVHakIsTUFWSCxDQVVVLGVBVlYsRUFVMkIsMkNBVjNCLEVBV0dBLE1BWEgsQ0FXVSxhQVhWLEVBV3lCLGdDQVh6QixFQVlHQSxNQVpILENBYUksNEJBYkosRUFjSSw2RkFkSixFQWVJSixXQUFXcUIsT0FmZixFQWdCSSxFQWhCSixFQWtCR2pCLE1BbEJILENBbUJJLHlCQW5CSixFQW9CSSw2Q0FwQkosRUFxQklKLFdBQVdzQixTQUFYLENBQXFCLGtCQUFyQixDQXJCSixFQXNCSSxFQXRCSixFQXdCR2xCLE1BeEJILENBeUJJLDZCQXpCSixFQTBCSSx3QkExQkosRUEyQklKLFdBQVd1QixnQkEzQmYsRUE0QkksRUE1QkosRUE4QkduQixNQTlCSCxDQThCVSxrQkE5QlYsRUE4QjhCLGdCQTlCOUIsRUErQkdBLE1BL0JILENBZ0NJLHdCQWhDSixFQWlDSSxnREFqQ0osRUFrQ0ksRUFsQ0osRUFvQ0dBLE1BcENILENBcUNJLGlCQXJDSixFQXNDSSwyRUF0Q0osRUF1Q0lKLFdBQVdxQixPQXZDZixFQXdDSSxFQXhDSixFQTBDR2pCLE1BMUNILENBMENVLGFBMUNWLEVBMEN5Qix5Q0ExQ3pCLEVBMkNHQSxNQTNDSCxDQTRDSSxzQkE1Q0osRUE2Q0kseUNBN0NKLEVBOENJSixXQUFXcUIsT0E5Q2YsRUErQ0ksRUEvQ0osRUFpREdqQixNQWpESCxDQWtESSwwQkFsREosRUFtREksc0RBbkRKLEVBb0RJSixXQUFXcUIsT0FwRGYsRUFxREksRUFyREosRUF1REdqQixNQXZESCxDQXdESSx5QkF4REosRUF5REksdUZBekRKLEVBMERJSixXQUFXd0IsU0ExRGYsRUEyREksRUEzREosRUE2REdwQixNQTdESCxDQThESSwyQkE5REosRUErREksOEVBL0RKLEVBZ0VJSixXQUFXc0IsU0FBWCxDQUFxQixvQkFBckIsQ0FoRUosRUFpRUksRUFqRUo7O0FBb0VBTCxjQUFRUSxFQUFSLENBQVcsUUFBWCxFQUFxQixZQUFNO0FBQ3pCO0FBQ0FDLGdCQUFRQyxHQUFSLENBQ0UsK0VBREY7QUFHQTtBQUNELE9BTkQ7O0FBUUFWLGNBQVFWLEtBQVIsQ0FBY1MsSUFBZDs7QUFFQSxhQUFPO0FBQ0xZLGlCQUFTWCxRQUFRWSxJQUFSLEVBREo7QUFFTEMsY0FBTWIsUUFBUWE7QUFGVCxPQUFQO0FBSUQ7Ozs7O2tCQXZIa0I5QixVIiwiZmlsZSI6ImFyZ3ZfcGFyc2VyLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IF8gZnJvbSAnbG9kYXNoJ1xuaW1wb3J0IHsgQ29tbWFuZCB9IGZyb20gJ2NvbW1hbmRlcidcbmltcG9ydCB7IHZlcnNpb24gfSBmcm9tICcuLi8uLi9wYWNrYWdlLmpzb24nXG5pbXBvcnQgcGF0aCBmcm9tICdwYXRoJ1xuaW1wb3J0IEdoZXJraW4gZnJvbSAnZ2hlcmtpbidcblxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQXJndlBhcnNlciB7XG4gIHN0YXRpYyBjb2xsZWN0KHZhbCwgbWVtbykge1xuICAgIG1lbW8ucHVzaCh2YWwpXG4gICAgcmV0dXJuIG1lbW9cbiAgfVxuXG4gIHN0YXRpYyBtZXJnZUpzb24ob3B0aW9uKSB7XG4gICAgcmV0dXJuIGZ1bmN0aW9uKHN0ciwgbWVtbykge1xuICAgICAgbGV0IHZhbFxuICAgICAgdHJ5IHtcbiAgICAgICAgdmFsID0gSlNPTi5wYXJzZShzdHIpXG4gICAgICB9IGNhdGNoIChlcnJvcikge1xuICAgICAgICB0aHJvdyBuZXcgRXJyb3IoXG4gICAgICAgICAgb3B0aW9uICsgJyBwYXNzZWQgaW52YWxpZCBKU09OOiAnICsgZXJyb3IubWVzc2FnZSArICc6ICcgKyBzdHJcbiAgICAgICAgKVxuICAgICAgfVxuICAgICAgaWYgKCFfLmlzUGxhaW5PYmplY3QodmFsKSkge1xuICAgICAgICB0aHJvdyBuZXcgRXJyb3Iob3B0aW9uICsgJyBtdXN0IGJlIHBhc3NlZCBKU09OIG9mIGFuIG9iamVjdDogJyArIHN0cilcbiAgICAgIH1cbiAgICAgIHJldHVybiBfLm1lcmdlKG1lbW8sIHZhbClcbiAgICB9XG4gIH1cblxuICBzdGF0aWMgbWVyZ2VUYWdzKHZhbCwgbWVtbykge1xuICAgIHJldHVybiBtZW1vID09PSAnJyA/IGAoJHt2YWx9KWAgOiBgJHttZW1vfSBhbmQgKCR7dmFsfSlgXG4gIH1cblxuICBzdGF0aWMgdmFsaWRhdGVMYW5ndWFnZSh2YWwpIHtcbiAgICBpZiAoIV8uaW5jbHVkZXMoXy5rZXlzKEdoZXJraW4uRElBTEVDVFMpLCB2YWwpKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1Vuc3VwcG9ydGVkIElTTyA2MzktMTogJyArIHZhbClcbiAgICB9XG4gICAgcmV0dXJuIHZhbFxuICB9XG5cbiAgc3RhdGljIHBhcnNlKGFyZ3YpIHtcbiAgICBjb25zdCBwcm9ncmFtID0gbmV3IENvbW1hbmQocGF0aC5iYXNlbmFtZShhcmd2WzFdKSlcblxuICAgIHByb2dyYW1cbiAgICAgIC51c2FnZSgnW29wdGlvbnNdIFs8RElSfEZJTEVbOkxJTkVdPi4uLl0nKVxuICAgICAgLnZlcnNpb24odmVyc2lvbiwgJy12LCAtLXZlcnNpb24nKVxuICAgICAgLm9wdGlvbignLWIsIC0tYmFja3RyYWNlJywgJ3Nob3cgZnVsbCBiYWNrdHJhY2UgZm9yIGVycm9ycycpXG4gICAgICAub3B0aW9uKFxuICAgICAgICAnLS1jb21waWxlciA8RVhURU5TSU9OOk1PRFVMRT4nLFxuICAgICAgICAncmVxdWlyZSBmaWxlcyB3aXRoIHRoZSBnaXZlbiBFWFRFTlNJT04gYWZ0ZXIgcmVxdWlyaW5nIE1PRFVMRSAocmVwZWF0YWJsZSknLFxuICAgICAgICBBcmd2UGFyc2VyLmNvbGxlY3QsXG4gICAgICAgIFtdXG4gICAgICApXG4gICAgICAub3B0aW9uKCctZCwgLS1kcnktcnVuJywgJ2ludm9rZSBmb3JtYXR0ZXJzIHdpdGhvdXQgZXhlY3V0aW5nIHN0ZXBzJylcbiAgICAgIC5vcHRpb24oJy0tZmFpbC1mYXN0JywgJ2Fib3J0IHRoZSBydW4gb24gZmlyc3QgZmFpbHVyZScpXG4gICAgICAub3B0aW9uKFxuICAgICAgICAnLWYsIC0tZm9ybWF0IDxUWVBFWzpQQVRIXT4nLFxuICAgICAgICAnc3BlY2lmeSB0aGUgb3V0cHV0IGZvcm1hdCwgb3B0aW9uYWxseSBzdXBwbHkgUEFUSCB0byByZWRpcmVjdCBmb3JtYXR0ZXIgb3V0cHV0IChyZXBlYXRhYmxlKScsXG4gICAgICAgIEFyZ3ZQYXJzZXIuY29sbGVjdCxcbiAgICAgICAgW11cbiAgICAgIClcbiAgICAgIC5vcHRpb24oXG4gICAgICAgICctLWZvcm1hdC1vcHRpb25zIDxKU09OPicsXG4gICAgICAgICdwcm92aWRlIG9wdGlvbnMgZm9yIGZvcm1hdHRlcnMgKHJlcGVhdGFibGUpJyxcbiAgICAgICAgQXJndlBhcnNlci5tZXJnZUpzb24oJy0tZm9ybWF0LW9wdGlvbnMnKSxcbiAgICAgICAge31cbiAgICAgIClcbiAgICAgIC5vcHRpb24oXG4gICAgICAgICctLWkxOG4ta2V5d29yZHMgPElTTyA2MzktMT4nLFxuICAgICAgICAnbGlzdCBsYW5ndWFnZSBrZXl3b3JkcycsXG4gICAgICAgIEFyZ3ZQYXJzZXIudmFsaWRhdGVMYW5ndWFnZSxcbiAgICAgICAgJydcbiAgICAgIClcbiAgICAgIC5vcHRpb24oJy0taTE4bi1sYW5ndWFnZXMnLCAnbGlzdCBsYW5ndWFnZXMnKVxuICAgICAgLm9wdGlvbihcbiAgICAgICAgJy0tbGFuZ3VhZ2UgPElTTyA2MzktMT4nLFxuICAgICAgICAncHJvdmlkZSB0aGUgZGVmYXVsdCBsYW5ndWFnZSBmb3IgZmVhdHVyZSBmaWxlcycsXG4gICAgICAgICcnXG4gICAgICApXG4gICAgICAub3B0aW9uKFxuICAgICAgICAnLS1uYW1lIDxSRUdFWFA+JyxcbiAgICAgICAgJ29ubHkgZXhlY3V0ZSB0aGUgc2NlbmFyaW9zIHdpdGggbmFtZSBtYXRjaGluZyB0aGUgZXhwcmVzc2lvbiAocmVwZWF0YWJsZSknLFxuICAgICAgICBBcmd2UGFyc2VyLmNvbGxlY3QsXG4gICAgICAgIFtdXG4gICAgICApXG4gICAgICAub3B0aW9uKCctLW5vLXN0cmljdCcsICdzdWNjZWVkIGV2ZW4gaWYgdGhlcmUgYXJlIHBlbmRpbmcgc3RlcHMnKVxuICAgICAgLm9wdGlvbihcbiAgICAgICAgJy1wLCAtLXByb2ZpbGUgPE5BTUU+JyxcbiAgICAgICAgJ3NwZWNpZnkgdGhlIHByb2ZpbGUgdG8gdXNlIChyZXBlYXRhYmxlKScsXG4gICAgICAgIEFyZ3ZQYXJzZXIuY29sbGVjdCxcbiAgICAgICAgW11cbiAgICAgIClcbiAgICAgIC5vcHRpb24oXG4gICAgICAgICctciwgLS1yZXF1aXJlIDxGSUxFfERJUj4nLFxuICAgICAgICAncmVxdWlyZSBmaWxlcyBiZWZvcmUgZXhlY3V0aW5nIGZlYXR1cmVzIChyZXBlYXRhYmxlKScsXG4gICAgICAgIEFyZ3ZQYXJzZXIuY29sbGVjdCxcbiAgICAgICAgW11cbiAgICAgIClcbiAgICAgIC5vcHRpb24oXG4gICAgICAgICctdCwgLS10YWdzIDxFWFBSRVNTSU9OPicsXG4gICAgICAgICdvbmx5IGV4ZWN1dGUgdGhlIGZlYXR1cmVzIG9yIHNjZW5hcmlvcyB3aXRoIHRhZ3MgbWF0Y2hpbmcgdGhlIGV4cHJlc3Npb24gKHJlcGVhdGFibGUpJyxcbiAgICAgICAgQXJndlBhcnNlci5tZXJnZVRhZ3MsXG4gICAgICAgICcnXG4gICAgICApXG4gICAgICAub3B0aW9uKFxuICAgICAgICAnLS13b3JsZC1wYXJhbWV0ZXJzIDxKU09OPicsXG4gICAgICAgICdwcm92aWRlIHBhcmFtZXRlcnMgdGhhdCB3aWxsIGJlIHBhc3NlZCB0byB0aGUgd29ybGQgY29uc3RydWN0b3IgKHJlcGVhdGFibGUpJyxcbiAgICAgICAgQXJndlBhcnNlci5tZXJnZUpzb24oJy0td29ybGQtcGFyYW1ldGVycycpLFxuICAgICAgICB7fVxuICAgICAgKVxuXG4gICAgcHJvZ3JhbS5vbignLS1oZWxwJywgKCkgPT4ge1xuICAgICAgLyogZXNsaW50LWRpc2FibGUgbm8tY29uc29sZSAqL1xuICAgICAgY29uc29sZS5sb2coXG4gICAgICAgICcgIEZvciBtb3JlIGRldGFpbHMgcGxlYXNlIHZpc2l0IGh0dHBzOi8vZ2l0aHViLmNvbS9jdWN1bWJlci9jdWN1bWJlci1qcyNjbGlcXG4nXG4gICAgICApXG4gICAgICAvKiBlc2xpbnQtZW5hYmxlIG5vLWNvbnNvbGUgKi9cbiAgICB9KVxuXG4gICAgcHJvZ3JhbS5wYXJzZShhcmd2KVxuXG4gICAgcmV0dXJuIHtcbiAgICAgIG9wdGlvbnM6IHByb2dyYW0ub3B0cygpLFxuICAgICAgYXJnczogcHJvZ3JhbS5hcmdzXG4gICAgfVxuICB9XG59XG4iXX0=