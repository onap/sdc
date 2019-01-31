'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.getTestCases = exports.getTestCasesFromFilesystem = exports.getExpandedArgv = undefined;

var _bluebird = require('bluebird');

var _bluebird2 = _interopRequireDefault(_bluebird);

var getExpandedArgv = exports.getExpandedArgv = function () {
  var _ref2 = (0, _bluebird.coroutine)(function* (_ref) {
    var argv = _ref.argv,
        cwd = _ref.cwd;

    var _ArgvParser$parse = _argv_parser2.default.parse(argv),
        options = _ArgvParser$parse.options;

    var fullArgv = argv;
    var profileArgv = yield new _profile_loader2.default(cwd).getArgv(options.profile);
    if (profileArgv.length > 0) {
      fullArgv = _lodash2.default.concat(argv.slice(0, 2), profileArgv, argv.slice(2));
    }
    return fullArgv;
  });

  return function getExpandedArgv(_x) {
    return _ref2.apply(this, arguments);
  };
}();

var getTestCasesFromFilesystem = exports.getTestCasesFromFilesystem = function () {
  var _ref4 = (0, _bluebird.coroutine)(function* (_ref3) {
    var cwd = _ref3.cwd,
        eventBroadcaster = _ref3.eventBroadcaster,
        featureDefaultLanguage = _ref3.featureDefaultLanguage,
        featurePaths = _ref3.featurePaths,
        pickleFilter = _ref3.pickleFilter;

    var result = [];
    yield _bluebird2.default.each(featurePaths, function () {
      var _ref5 = (0, _bluebird.coroutine)(function* (featurePath) {
        var source = yield _fs2.default.readFile(featurePath, 'utf8');
        result = result.concat((yield getTestCases({
          eventBroadcaster: eventBroadcaster,
          language: featureDefaultLanguage,
          source: source,
          pickleFilter: pickleFilter,
          uri: _path2.default.relative(cwd, featurePath)
        })));
      });

      return function (_x3) {
        return _ref5.apply(this, arguments);
      };
    }());
    return result;
  });

  return function getTestCasesFromFilesystem(_x2) {
    return _ref4.apply(this, arguments);
  };
}();

var getTestCases = exports.getTestCases = function () {
  var _ref7 = (0, _bluebird.coroutine)(function* (_ref6) {
    var eventBroadcaster = _ref6.eventBroadcaster,
        language = _ref6.language,
        pickleFilter = _ref6.pickleFilter,
        source = _ref6.source,
        uri = _ref6.uri;

    var result = [];
    var events = _gherkin2.default.generateEvents(source, uri, {}, language);
    events.forEach(function (event) {
      eventBroadcaster.emit(event.type, _lodash2.default.omit(event, 'type'));
      if (event.type === 'pickle') {
        var pickle = event.pickle;

        if (pickleFilter.matches({ pickle: pickle, uri: uri })) {
          eventBroadcaster.emit('pickle-accepted', { pickle: pickle, uri: uri });
          result.push({ pickle: pickle, uri: uri });
        } else {
          eventBroadcaster.emit('pickle-rejected', { pickle: pickle, uri: uri });
        }
      }
      if (event.type === 'attachment') {
        throw new Error(event.data);
      }
    });
    return result;
  });

  return function getTestCases(_x4) {
    return _ref7.apply(this, arguments);
  };
}();

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _argv_parser = require('./argv_parser');

var _argv_parser2 = _interopRequireDefault(_argv_parser);

var _fs = require('mz/fs');

var _fs2 = _interopRequireDefault(_fs);

var _gherkin = require('gherkin');

var _gherkin2 = _interopRequireDefault(_gherkin);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

var _profile_loader = require('./profile_loader');

var _profile_loader2 = _interopRequireDefault(_profile_loader);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9jbGkvaGVscGVycy5qcyJdLCJuYW1lcyI6WyJhcmd2IiwiY3dkIiwicGFyc2UiLCJvcHRpb25zIiwiZnVsbEFyZ3YiLCJwcm9maWxlQXJndiIsImdldEFyZ3YiLCJwcm9maWxlIiwibGVuZ3RoIiwiY29uY2F0Iiwic2xpY2UiLCJnZXRFeHBhbmRlZEFyZ3YiLCJldmVudEJyb2FkY2FzdGVyIiwiZmVhdHVyZURlZmF1bHRMYW5ndWFnZSIsImZlYXR1cmVQYXRocyIsInBpY2tsZUZpbHRlciIsInJlc3VsdCIsImVhY2giLCJmZWF0dXJlUGF0aCIsInNvdXJjZSIsInJlYWRGaWxlIiwiZ2V0VGVzdENhc2VzIiwibGFuZ3VhZ2UiLCJ1cmkiLCJyZWxhdGl2ZSIsImdldFRlc3RDYXNlc0Zyb21GaWxlc3lzdGVtIiwiZXZlbnRzIiwiZ2VuZXJhdGVFdmVudHMiLCJmb3JFYWNoIiwiZW1pdCIsImV2ZW50IiwidHlwZSIsIm9taXQiLCJwaWNrbGUiLCJtYXRjaGVzIiwicHVzaCIsIkVycm9yIiwiZGF0YSJdLCJtYXBwaW5ncyI6Ijs7Ozs7Ozs7Ozs7O3VDQVFPLGlCQUE4QztBQUFBLFFBQWJBLElBQWEsUUFBYkEsSUFBYTtBQUFBLFFBQVBDLEdBQU8sUUFBUEEsR0FBTzs7QUFBQSw0QkFDakMsc0JBQVdDLEtBQVgsQ0FBaUJGLElBQWpCLENBRGlDO0FBQUEsUUFDN0NHLE9BRDZDLHFCQUM3Q0EsT0FENkM7O0FBRW5ELFFBQUlDLFdBQVdKLElBQWY7QUFDQSxRQUFNSyxjQUFjLE1BQU0sNkJBQWtCSixHQUFsQixFQUF1QkssT0FBdkIsQ0FBK0JILFFBQVFJLE9BQXZDLENBQTFCO0FBQ0EsUUFBSUYsWUFBWUcsTUFBWixHQUFxQixDQUF6QixFQUE0QjtBQUMxQkosaUJBQVcsaUJBQUVLLE1BQUYsQ0FBU1QsS0FBS1UsS0FBTCxDQUFXLENBQVgsRUFBYyxDQUFkLENBQVQsRUFBMkJMLFdBQTNCLEVBQXdDTCxLQUFLVSxLQUFMLENBQVcsQ0FBWCxDQUF4QyxDQUFYO0FBQ0Q7QUFDRCxXQUFPTixRQUFQO0FBQ0QsRzs7a0JBUnFCTyxlOzs7Ozs7dUNBVWYsa0JBTUo7QUFBQSxRQUxEVixHQUtDLFNBTERBLEdBS0M7QUFBQSxRQUpEVyxnQkFJQyxTQUpEQSxnQkFJQztBQUFBLFFBSERDLHNCQUdDLFNBSERBLHNCQUdDO0FBQUEsUUFGREMsWUFFQyxTQUZEQSxZQUVDO0FBQUEsUUFEREMsWUFDQyxTQUREQSxZQUNDOztBQUNELFFBQUlDLFNBQVMsRUFBYjtBQUNBLFVBQU0sbUJBQVFDLElBQVIsQ0FBYUgsWUFBYjtBQUFBLDJDQUEyQixXQUFNSSxXQUFOLEVBQXFCO0FBQ3BELFlBQU1DLFNBQVMsTUFBTSxhQUFHQyxRQUFILENBQVlGLFdBQVosRUFBeUIsTUFBekIsQ0FBckI7QUFDQUYsaUJBQVNBLE9BQU9QLE1BQVAsRUFDUCxNQUFNWSxhQUFhO0FBQ2pCVCw0Q0FEaUI7QUFFakJVLG9CQUFVVCxzQkFGTztBQUdqQk0sd0JBSGlCO0FBSWpCSixvQ0FKaUI7QUFLakJRLGVBQUssZUFBS0MsUUFBTCxDQUFjdkIsR0FBZCxFQUFtQmlCLFdBQW5CO0FBTFksU0FBYixDQURDLEVBQVQ7QUFTRCxPQVhLOztBQUFBO0FBQUE7QUFBQTtBQUFBLFFBQU47QUFZQSxXQUFPRixNQUFQO0FBQ0QsRzs7a0JBckJxQlMsMEI7Ozs7Ozt1Q0F1QmYsa0JBTUo7QUFBQSxRQUxEYixnQkFLQyxTQUxEQSxnQkFLQztBQUFBLFFBSkRVLFFBSUMsU0FKREEsUUFJQztBQUFBLFFBSERQLFlBR0MsU0FIREEsWUFHQztBQUFBLFFBRkRJLE1BRUMsU0FGREEsTUFFQztBQUFBLFFBRERJLEdBQ0MsU0FEREEsR0FDQzs7QUFDRCxRQUFNUCxTQUFTLEVBQWY7QUFDQSxRQUFNVSxTQUFTLGtCQUFRQyxjQUFSLENBQXVCUixNQUF2QixFQUErQkksR0FBL0IsRUFBb0MsRUFBcEMsRUFBd0NELFFBQXhDLENBQWY7QUFDQUksV0FBT0UsT0FBUCxDQUFlLGlCQUFTO0FBQ3RCaEIsdUJBQWlCaUIsSUFBakIsQ0FBc0JDLE1BQU1DLElBQTVCLEVBQWtDLGlCQUFFQyxJQUFGLENBQU9GLEtBQVAsRUFBYyxNQUFkLENBQWxDO0FBQ0EsVUFBSUEsTUFBTUMsSUFBTixLQUFlLFFBQW5CLEVBQTZCO0FBQUEsWUFDbkJFLE1BRG1CLEdBQ1JILEtBRFEsQ0FDbkJHLE1BRG1COztBQUUzQixZQUFJbEIsYUFBYW1CLE9BQWIsQ0FBcUIsRUFBRUQsY0FBRixFQUFVVixRQUFWLEVBQXJCLENBQUosRUFBMkM7QUFDekNYLDJCQUFpQmlCLElBQWpCLENBQXNCLGlCQUF0QixFQUF5QyxFQUFFSSxjQUFGLEVBQVVWLFFBQVYsRUFBekM7QUFDQVAsaUJBQU9tQixJQUFQLENBQVksRUFBRUYsY0FBRixFQUFVVixRQUFWLEVBQVo7QUFDRCxTQUhELE1BR087QUFDTFgsMkJBQWlCaUIsSUFBakIsQ0FBc0IsaUJBQXRCLEVBQXlDLEVBQUVJLGNBQUYsRUFBVVYsUUFBVixFQUF6QztBQUNEO0FBQ0Y7QUFDRCxVQUFJTyxNQUFNQyxJQUFOLEtBQWUsWUFBbkIsRUFBaUM7QUFDL0IsY0FBTSxJQUFJSyxLQUFKLENBQVVOLE1BQU1PLElBQWhCLENBQU47QUFDRDtBQUNGLEtBZEQ7QUFlQSxXQUFPckIsTUFBUDtBQUNELEc7O2tCQXpCcUJLLFk7Ozs7O0FBekN0Qjs7OztBQUNBOzs7O0FBQ0E7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0EiLCJmaWxlIjoiaGVscGVycy5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCBfIGZyb20gJ2xvZGFzaCdcbmltcG9ydCBBcmd2UGFyc2VyIGZyb20gJy4vYXJndl9wYXJzZXInXG5pbXBvcnQgZnMgZnJvbSAnbXovZnMnXG5pbXBvcnQgR2hlcmtpbiBmcm9tICdnaGVya2luJ1xuaW1wb3J0IHBhdGggZnJvbSAncGF0aCdcbmltcG9ydCBQcm9maWxlTG9hZGVyIGZyb20gJy4vcHJvZmlsZV9sb2FkZXInXG5pbXBvcnQgUHJvbWlzZSBmcm9tICdibHVlYmlyZCdcblxuZXhwb3J0IGFzeW5jIGZ1bmN0aW9uIGdldEV4cGFuZGVkQXJndih7IGFyZ3YsIGN3ZCB9KSB7XG4gIGxldCB7IG9wdGlvbnMgfSA9IEFyZ3ZQYXJzZXIucGFyc2UoYXJndilcbiAgbGV0IGZ1bGxBcmd2ID0gYXJndlxuICBjb25zdCBwcm9maWxlQXJndiA9IGF3YWl0IG5ldyBQcm9maWxlTG9hZGVyKGN3ZCkuZ2V0QXJndihvcHRpb25zLnByb2ZpbGUpXG4gIGlmIChwcm9maWxlQXJndi5sZW5ndGggPiAwKSB7XG4gICAgZnVsbEFyZ3YgPSBfLmNvbmNhdChhcmd2LnNsaWNlKDAsIDIpLCBwcm9maWxlQXJndiwgYXJndi5zbGljZSgyKSlcbiAgfVxuICByZXR1cm4gZnVsbEFyZ3Zcbn1cblxuZXhwb3J0IGFzeW5jIGZ1bmN0aW9uIGdldFRlc3RDYXNlc0Zyb21GaWxlc3lzdGVtKHtcbiAgY3dkLFxuICBldmVudEJyb2FkY2FzdGVyLFxuICBmZWF0dXJlRGVmYXVsdExhbmd1YWdlLFxuICBmZWF0dXJlUGF0aHMsXG4gIHBpY2tsZUZpbHRlclxufSkge1xuICBsZXQgcmVzdWx0ID0gW11cbiAgYXdhaXQgUHJvbWlzZS5lYWNoKGZlYXR1cmVQYXRocywgYXN5bmMgZmVhdHVyZVBhdGggPT4ge1xuICAgIGNvbnN0IHNvdXJjZSA9IGF3YWl0IGZzLnJlYWRGaWxlKGZlYXR1cmVQYXRoLCAndXRmOCcpXG4gICAgcmVzdWx0ID0gcmVzdWx0LmNvbmNhdChcbiAgICAgIGF3YWl0IGdldFRlc3RDYXNlcyh7XG4gICAgICAgIGV2ZW50QnJvYWRjYXN0ZXIsXG4gICAgICAgIGxhbmd1YWdlOiBmZWF0dXJlRGVmYXVsdExhbmd1YWdlLFxuICAgICAgICBzb3VyY2UsXG4gICAgICAgIHBpY2tsZUZpbHRlcixcbiAgICAgICAgdXJpOiBwYXRoLnJlbGF0aXZlKGN3ZCwgZmVhdHVyZVBhdGgpXG4gICAgICB9KVxuICAgIClcbiAgfSlcbiAgcmV0dXJuIHJlc3VsdFxufVxuXG5leHBvcnQgYXN5bmMgZnVuY3Rpb24gZ2V0VGVzdENhc2VzKHtcbiAgZXZlbnRCcm9hZGNhc3RlcixcbiAgbGFuZ3VhZ2UsXG4gIHBpY2tsZUZpbHRlcixcbiAgc291cmNlLFxuICB1cmlcbn0pIHtcbiAgY29uc3QgcmVzdWx0ID0gW11cbiAgY29uc3QgZXZlbnRzID0gR2hlcmtpbi5nZW5lcmF0ZUV2ZW50cyhzb3VyY2UsIHVyaSwge30sIGxhbmd1YWdlKVxuICBldmVudHMuZm9yRWFjaChldmVudCA9PiB7XG4gICAgZXZlbnRCcm9hZGNhc3Rlci5lbWl0KGV2ZW50LnR5cGUsIF8ub21pdChldmVudCwgJ3R5cGUnKSlcbiAgICBpZiAoZXZlbnQudHlwZSA9PT0gJ3BpY2tsZScpIHtcbiAgICAgIGNvbnN0IHsgcGlja2xlIH0gPSBldmVudFxuICAgICAgaWYgKHBpY2tsZUZpbHRlci5tYXRjaGVzKHsgcGlja2xlLCB1cmkgfSkpIHtcbiAgICAgICAgZXZlbnRCcm9hZGNhc3Rlci5lbWl0KCdwaWNrbGUtYWNjZXB0ZWQnLCB7IHBpY2tsZSwgdXJpIH0pXG4gICAgICAgIHJlc3VsdC5wdXNoKHsgcGlja2xlLCB1cmkgfSlcbiAgICAgIH0gZWxzZSB7XG4gICAgICAgIGV2ZW50QnJvYWRjYXN0ZXIuZW1pdCgncGlja2xlLXJlamVjdGVkJywgeyBwaWNrbGUsIHVyaSB9KVxuICAgICAgfVxuICAgIH1cbiAgICBpZiAoZXZlbnQudHlwZSA9PT0gJ2F0dGFjaG1lbnQnKSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoZXZlbnQuZGF0YSlcbiAgICB9XG4gIH0pXG4gIHJldHVybiByZXN1bHRcbn1cbiJdfQ==