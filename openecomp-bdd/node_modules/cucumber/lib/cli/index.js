'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2 = require('babel-runtime/helpers/extends');

var _extends3 = _interopRequireDefault(_extends2);

var _bluebird = require('bluebird');

var _bluebird2 = _interopRequireDefault(_bluebird);

var _classCallCheck2 = require('babel-runtime/helpers/classCallCheck');

var _classCallCheck3 = _interopRequireDefault(_classCallCheck2);

var _createClass2 = require('babel-runtime/helpers/createClass');

var _createClass3 = _interopRequireDefault(_createClass2);

var _helpers = require('../formatter/helpers');

var _helpers2 = require('./helpers');

var _install_validator = require('./install_validator');

var _i18n = require('./i18n');

var I18n = _interopRequireWildcard(_i18n);

var _configuration_builder = require('./configuration_builder');

var _configuration_builder2 = _interopRequireDefault(_configuration_builder);

var _events = require('events');

var _events2 = _interopRequireDefault(_events);

var _builder = require('../formatter/builder');

var _builder2 = _interopRequireDefault(_builder);

var _fs = require('mz/fs');

var _fs2 = _interopRequireDefault(_fs);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

var _pickle_filter = require('../pickle_filter');

var _pickle_filter2 = _interopRequireDefault(_pickle_filter);

var _runtime = require('../runtime');

var _runtime2 = _interopRequireDefault(_runtime);

var _support_code_library_builder = require('../support_code_library_builder');

var _support_code_library_builder2 = _interopRequireDefault(_support_code_library_builder);

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Cli = function () {
  function Cli(_ref) {
    var argv = _ref.argv,
        cwd = _ref.cwd,
        stdout = _ref.stdout;
    (0, _classCallCheck3.default)(this, Cli);

    this.argv = argv;
    this.cwd = cwd;
    this.stdout = stdout;
  }

  (0, _createClass3.default)(Cli, [{
    key: 'getConfiguration',
    value: function () {
      var _ref2 = (0, _bluebird.coroutine)(function* () {
        var fullArgv = yield (0, _helpers2.getExpandedArgv)({ argv: this.argv, cwd: this.cwd });
        return yield _configuration_builder2.default.build({ argv: fullArgv, cwd: this.cwd });
      });

      function getConfiguration() {
        return _ref2.apply(this, arguments);
      }

      return getConfiguration;
    }()
  }, {
    key: 'initializeFormatters',
    value: function () {
      var _ref4 = (0, _bluebird.coroutine)(function* (_ref3) {
        var _this = this;

        var eventBroadcaster = _ref3.eventBroadcaster,
            formatOptions = _ref3.formatOptions,
            formats = _ref3.formats,
            supportCodeLibrary = _ref3.supportCodeLibrary;

        var streamsToClose = [];
        var eventDataCollector = new _helpers.EventDataCollector(eventBroadcaster);
        yield _bluebird2.default.map(formats, function () {
          var _ref6 = (0, _bluebird.coroutine)(function* (_ref5) {
            var _context;

            var type = _ref5.type,
                outputTo = _ref5.outputTo;

            var stream = _this.stdout;
            if (outputTo) {
              var fd = yield _fs2.default.open(_path2.default.resolve(_this.cwd, outputTo), 'w');
              stream = _fs2.default.createWriteStream(null, { fd: fd });
              streamsToClose.push(stream);
            }
            var typeOptions = (0, _extends3.default)({
              eventBroadcaster: eventBroadcaster,
              eventDataCollector: eventDataCollector,
              log: (_context = stream).write.bind(_context),
              stream: stream,
              supportCodeLibrary: supportCodeLibrary
            }, formatOptions);
            return _builder2.default.build(type, typeOptions);
          });

          return function (_x2) {
            return _ref6.apply(this, arguments);
          };
        }());
        return function () {
          return _bluebird2.default.each(streamsToClose, function (stream) {
            return _bluebird2.default.promisify(stream.end.bind(stream))();
          });
        };
      });

      function initializeFormatters(_x) {
        return _ref4.apply(this, arguments);
      }

      return initializeFormatters;
    }()
  }, {
    key: 'getSupportCodeLibrary',
    value: function getSupportCodeLibrary(supportCodePaths) {
      _support_code_library_builder2.default.reset(this.cwd);
      supportCodePaths.forEach(function (codePath) {
        return require(codePath);
      });
      return _support_code_library_builder2.default.finalize();
    }
  }, {
    key: 'run',
    value: function () {
      var _ref7 = (0, _bluebird.coroutine)(function* () {
        yield (0, _install_validator.validateInstall)(this.cwd);
        var configuration = yield this.getConfiguration();
        if (configuration.listI18nLanguages) {
          this.stdout.write(I18n.getLanguages());
          return true;
        }
        if (configuration.listI18nKeywordsFor) {
          this.stdout.write(I18n.getKeywords(configuration.listI18nKeywordsFor));
          return true;
        }
        var supportCodeLibrary = this.getSupportCodeLibrary(configuration.supportCodePaths);
        var eventBroadcaster = new _events2.default();
        var cleanup = yield this.initializeFormatters({
          eventBroadcaster: eventBroadcaster,
          formatOptions: configuration.formatOptions,
          formats: configuration.formats,
          supportCodeLibrary: supportCodeLibrary
        });
        var testCases = yield (0, _helpers2.getTestCasesFromFilesystem)({
          cwd: this.cwd,
          eventBroadcaster: eventBroadcaster,
          featureDefaultLanguage: configuration.featureDefaultLanguage,
          featurePaths: configuration.featurePaths,
          pickleFilter: new _pickle_filter2.default(configuration.pickleFilterOptions)
        });
        var runtime = new _runtime2.default({
          eventBroadcaster: eventBroadcaster,
          options: configuration.runtimeOptions,
          supportCodeLibrary: supportCodeLibrary,
          testCases: testCases
        });
        var result = yield runtime.start();
        yield cleanup();
        return result;
      });

      function run() {
        return _ref7.apply(this, arguments);
      }

      return run;
    }()
  }]);
  return Cli;
}();

exports.default = Cli;
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9jbGkvaW5kZXguanMiXSwibmFtZXMiOlsiSTE4biIsIkNsaSIsImFyZ3YiLCJjd2QiLCJzdGRvdXQiLCJmdWxsQXJndiIsImJ1aWxkIiwiZXZlbnRCcm9hZGNhc3RlciIsImZvcm1hdE9wdGlvbnMiLCJmb3JtYXRzIiwic3VwcG9ydENvZGVMaWJyYXJ5Iiwic3RyZWFtc1RvQ2xvc2UiLCJldmVudERhdGFDb2xsZWN0b3IiLCJtYXAiLCJ0eXBlIiwib3V0cHV0VG8iLCJzdHJlYW0iLCJmZCIsIm9wZW4iLCJyZXNvbHZlIiwiY3JlYXRlV3JpdGVTdHJlYW0iLCJwdXNoIiwidHlwZU9wdGlvbnMiLCJsb2ciLCJ3cml0ZSIsImVhY2giLCJwcm9taXNpZnkiLCJlbmQiLCJzdXBwb3J0Q29kZVBhdGhzIiwicmVzZXQiLCJmb3JFYWNoIiwicmVxdWlyZSIsImNvZGVQYXRoIiwiZmluYWxpemUiLCJjb25maWd1cmF0aW9uIiwiZ2V0Q29uZmlndXJhdGlvbiIsImxpc3RJMThuTGFuZ3VhZ2VzIiwiZ2V0TGFuZ3VhZ2VzIiwibGlzdEkxOG5LZXl3b3Jkc0ZvciIsImdldEtleXdvcmRzIiwiZ2V0U3VwcG9ydENvZGVMaWJyYXJ5IiwiY2xlYW51cCIsImluaXRpYWxpemVGb3JtYXR0ZXJzIiwidGVzdENhc2VzIiwiZmVhdHVyZURlZmF1bHRMYW5ndWFnZSIsImZlYXR1cmVQYXRocyIsInBpY2tsZUZpbHRlciIsInBpY2tsZUZpbHRlck9wdGlvbnMiLCJydW50aW1lIiwib3B0aW9ucyIsInJ1bnRpbWVPcHRpb25zIiwicmVzdWx0Iiwic3RhcnQiXSwibWFwcGluZ3MiOiI7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7Ozs7QUFBQTs7QUFDQTs7QUFDQTs7QUFDQTs7SUFBWUEsSTs7QUFDWjs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFFQTs7OztBQUNBOzs7Ozs7OztJQUVxQkMsRztBQUNuQixxQkFBbUM7QUFBQSxRQUFyQkMsSUFB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QWNOLGdCQURXLENBQTNCO0FBR0EsWUFBTXJCLG1CQUFtQixzQkFBekI7QUFDQSxZQUFNa0MsVUFBVSxNQUFNLEtBQUtDLG9CQUFMLENBQTBCO0FBQzlDbkMsNENBRDhDO0FBRTlDQyx5QkFBZTBCLGNBQWMxQixhQUZpQjtBQUc5Q0MsbUJBQVN5QixjQUFjekIsT0FIdUI7QUFJOUNDO0FBSjhDLFNBQTFCLENBQXRCO0FBTUEsWUFBTWlDLFlBQVksTUFBTSwwQ0FBMkI7QUFDakR4QyxlQUFLLEtBQUtBLEdBRHVDO0FBRWpESSw0Q0FGaUQ7QUFHakRxQyxrQ0FBd0JWLGNBQWNVLHNCQUhXO0FBSWpEQyx3QkFBY1gsY0FBY1csWUFKcUI7QUFLakRDLHdCQUFjLDRCQUFpQlosY0FBY2EsbUJBQS9CO0FBTG1DLFNBQTNCLENBQXhCO0FBT0EsWUFBTUMsVUFBVSxzQkFBWTtBQUMxQnpDLDRDQUQwQjtBQUUxQjBDLG1CQUFTZixjQUFjZ0IsY0FGRztBQUcxQnhDLGdEQUgwQjtBQUkxQmlDO0FBSjBCLFNBQVosQ0FBaEI7QUFNQSxZQUFNUSxTQUFTLE1BQU1ILFFBQVFJLEtBQVIsRUFBckI7QUFDQSxjQUFNWCxTQUFOO0FBQ0EsZUFBT1UsTUFBUDtBQUNELE87Ozs7Ozs7Ozs7OztrQkF2RmtCbEQsRyIsImZpbGUiOiJpbmRleC5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCB7IEV2ZW50RGF0YUNvbGxlY3RvciB9IGZyb20gJy4uL2Zvcm1hdHRlci9oZWxwZXJzJ1xuaW1wb3J0IHsgZ2V0RXhwYW5kZWRBcmd2LCBnZXRUZXN0Q2FzZXNGcm9tRmlsZXN5c3RlbSB9IGZyb20gJy4vaGVscGVycydcbmltcG9ydCB7IHZhbGlkYXRlSW5zdGFsbCB9IGZyb20gJy4vaW5zdGFsbF92YWxpZGF0b3InXG5pbXBvcnQgKiBhcyBJMThuIGZyb20gJy4vaTE4bidcbmltcG9ydCBDb25maWd1cmF0aW9uQnVpbGRlciBmcm9tICcuL2NvbmZpZ3VyYXRpb25fYnVpbGRlcidcbmltcG9ydCBFdmVudEVtaXR0ZXIgZnJvbSAnZXZlbnRzJ1xuaW1wb3J0IEZvcm1hdHRlckJ1aWxkZXIgZnJvbSAnLi4vZm9ybWF0dGVyL2J1aWxkZXInXG5pbXBvcnQgZnMgZnJvbSAnbXovZnMnXG5pbXBvcnQgcGF0aCBmcm9tICdwYXRoJ1xuaW1wb3J0IFBpY2tsZUZpbHRlciBmcm9tICcuLi9waWNrbGVfZmlsdGVyJ1xuaW1wb3J0IFByb21pc2UgZnJvbSAnYmx1ZWJpcmQnXG5pbXBvcnQgUnVudGltZSBmcm9tICcuLi9ydW50aW1lJ1xuaW1wb3J0IHN1cHBvcnRDb2RlTGlicmFyeUJ1aWxkZXIgZnJvbSAnLi4vc3VwcG9ydF9jb2RlX2xpYnJhcnlfYnVpbGRlcidcblxuZXhwb3J0IGRlZmF1bHQgY2xhc3MgQ2xpIHtcbiAgY29uc3RydWN0b3IoeyBhcmd2LCBjd2QsIHN0ZG91dCB9KSB7XG4gICAgdGhpcy5hcmd2ID0gYXJndlxuICAgIHRoaXMuY3dkID0gY3dkXG4gICAgdGhpcy5zdGRvdXQgPSBzdGRvdXRcbiAgfVxuXG4gIGFzeW5jIGdldENvbmZpZ3VyYXRpb24oKSB7XG4gICAgY29uc3QgZnVsbEFyZ3YgPSBhd2FpdCBnZXRFeHBhbmRlZEFyZ3YoeyBhcmd2OiB0aGlzLmFyZ3YsIGN3ZDogdGhpcy5jd2QgfSlcbiAgICByZXR1cm4gYXdhaXQgQ29uZmlndXJhdGlvbkJ1aWxkZXIuYnVpbGQoeyBhcmd2OiBmdWxsQXJndiwgY3dkOiB0aGlzLmN3ZCB9KVxuICB9XG5cbiAgYXN5bmMgaW5pdGlhbGl6ZUZvcm1hdHRlcnMoe1xuICAgIGV2ZW50QnJvYWRjYXN0ZXIsXG4gICAgZm9ybWF0T3B0aW9ucyxcbiAgICBmb3JtYXRzLFxuICAgIHN1cHBvcnRDb2RlTGlicmFyeVxuICB9KSB7XG4gICAgY29uc3Qgc3RyZWFtc1RvQ2xvc2UgPSBbXVxuICAgIGNvbnN0IGV2ZW50RGF0YUNvbGxlY3RvciA9IG5ldyBFdmVudERhdGFDb2xsZWN0b3IoZXZlbnRCcm9hZGNhc3RlcilcbiAgICBhd2FpdCBQcm9taXNlLm1hcChmb3JtYXRzLCBhc3luYyAoeyB0eXBlLCBvdXRwdXRUbyB9KSA9PiB7XG4gICAgICBsZXQgc3RyZWFtID0gdGhpcy5zdGRvdXRcbiAgICAgIGlmIChvdXRwdXRUbykge1xuICAgICAgICBsZXQgZmQgPSBhd2FpdCBmcy5vcGVuKHBhdGgucmVzb2x2ZSh0aGlzLmN3ZCwgb3V0cHV0VG8pLCAndycpXG4gICAgICAgIHN0cmVhbSA9IGZzLmNyZWF0ZVdyaXRlU3RyZWFtKG51bGwsIHsgZmQgfSlcbiAgICAgICAgc3RyZWFtc1RvQ2xvc2UucHVzaChzdHJlYW0pXG4gICAgICB9XG4gICAgICBjb25zdCB0eXBlT3B0aW9ucyA9IHtcbiAgICAgICAgZXZlbnRCcm9hZGNhc3RlcixcbiAgICAgICAgZXZlbnREYXRhQ29sbGVjdG9yLFxuICAgICAgICBsb2c6IDo6c3RyZWFtLndyaXRlLFxuICAgICAgICBzdHJlYW0sXG4gICAgICAgIHN1cHBvcnRDb2RlTGlicmFyeSxcbiAgICAgICAgLi4uZm9ybWF0T3B0aW9uc1xuICAgICAgfVxuICAgICAgcmV0dXJuIEZvcm1hdHRlckJ1aWxkZXIuYnVpbGQodHlwZSwgdHlwZU9wdGlvbnMpXG4gICAgfSlcbiAgICByZXR1cm4gZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gUHJvbWlzZS5lYWNoKHN0cmVhbXNUb0Nsb3NlLCBzdHJlYW0gPT5cbiAgICAgICAgUHJvbWlzZS5wcm9taXNpZnkoOjpzdHJlYW0uZW5kKSgpXG4gICAgICApXG4gICAgfVxuICB9XG5cbiAgZ2V0U3VwcG9ydENvZGVMaWJyYXJ5KHN1cHBvcnRDb2RlUGF0aHMpIHtcbiAgICBzdXBwb3J0Q29kZUxpYnJhcnlCdWlsZGVyLnJlc2V0KHRoaXMuY3dkKVxuICAgIHN1cHBvcnRDb2RlUGF0aHMuZm9yRWFjaChjb2RlUGF0aCA9PiByZXF1aXJlKGNvZGVQYXRoKSlcbiAgICByZXR1cm4gc3VwcG9ydENvZGVMaWJyYXJ5QnVpbGRlci5maW5hbGl6ZSgpXG4gIH1cblxuICBhc3luYyBydW4oKSB7XG4gICAgYXdhaXQgdmFsaWRhdGVJbnN0YWxsKHRoaXMuY3dkKVxuICAgIGNvbnN0IGNvbmZpZ3VyYXRpb24gPSBhd2FpdCB0aGlzLmdldENvbmZpZ3VyYXRpb24oKVxuICAgIGlmIChjb25maWd1cmF0aW9uLmxpc3RJMThuTGFuZ3VhZ2VzKSB7XG4gICAgICB0aGlzLnN0ZG91dC53cml0ZShJMThuLmdldExhbmd1YWdlcygpKVxuICAgICAgcmV0dXJuIHRydWVcbiAgICB9XG4gICAgaWYgKGNvbmZpZ3VyYXRpb24ubGlzdEkxOG5LZXl3b3Jkc0Zvcikge1xuICAgICAgdGhpcy5zdGRvdXQud3JpdGUoSTE4bi5nZXRLZXl3b3Jkcyhjb25maWd1cmF0aW9uLmxpc3RJMThuS2V5d29yZHNGb3IpKVxuICAgICAgcmV0dXJuIHRydWVcbiAgICB9XG4gICAgY29uc3Qgc3VwcG9ydENvZGVMaWJyYXJ5ID0gdGhpcy5nZXRTdXBwb3J0Q29kZUxpYnJhcnkoXG4gICAgICBjb25maWd1cmF0aW9uLnN1cHBvcnRDb2RlUGF0aHNcbiAgICApXG4gICAgY29uc3QgZXZlbnRCcm9hZGNhc3RlciA9IG5ldyBFdmVudEVtaXR0ZXIoKVxuICAgIGNvbnN0IGNsZWFudXAgPSBhd2FpdCB0aGlzLmluaXRpYWxpemVGb3JtYXR0ZXJzKHtcbiAgICAgIGV2ZW50QnJvYWRjYXN0ZXIsXG4gICAgICBmb3JtYXRPcHRpb25zOiBjb25maWd1cmF0aW9uLmZvcm1hdE9wdGlvbnMsXG4gICAgICBmb3JtYXRzOiBjb25maWd1cmF0aW9uLmZvcm1hdHMsXG4gICAgICBzdXBwb3J0Q29kZUxpYnJhcnlcbiAgICB9KVxuICAgIGNvbnN0IHRlc3RDYXNlcyA9IGF3YWl0IGdldFRlc3RDYXNlc0Zyb21GaWxlc3lzdGVtKHtcbiAgICAgIGN3ZDogdGhpcy5jd2QsXG4gICAgICBldmVudEJyb2FkY2FzdGVyLFxuICAgICAgZmVhdHVyZURlZmF1bHRMYW5ndWFnZTogY29uZmlndXJhdGlvbi5mZWF0dXJlRGVmYXVsdExhbmd1YWdlLFxuICAgICAgZmVhdHVyZVBhdGhzOiBjb25maWd1cmF0aW9uLmZlYXR1cmVQYXRocyxcbiAgICAgIHBpY2tsZUZpbHRlcjogbmV3IFBpY2tsZUZpbHRlcihjb25maWd1cmF0aW9uLnBpY2tsZUZpbHRlck9wdGlvbnMpXG4gICAgfSlcbiAgICBjb25zdCBydW50aW1lID0gbmV3IFJ1bnRpbWUoe1xuICAgICAgZXZlbnRCcm9hZGNhc3RlcixcbiAgICAgIG9wdGlvbnM6IGNvbmZpZ3VyYXRpb24ucnVudGltZU9wdGlvbnMsXG4gICAgICBzdXBwb3J0Q29kZUxpYnJhcnksXG4gICAgICB0ZXN0Q2FzZXNcbiAgICB9KVxuICAgIGNvbnN0IHJlc3VsdCA9IGF3YWl0IHJ1bnRpbWUuc3RhcnQoKVxuICAgIGF3YWl0IGNsZWFudXAoKVxuICAgIHJldHVybiByZXN1bHRcbiAgfVxufVxuIl19