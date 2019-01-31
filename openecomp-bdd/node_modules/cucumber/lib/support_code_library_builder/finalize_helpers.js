'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.wrapDefinitions = wrapDefinitions;

var _lodash = require('lodash');

var _lodash2 = _interopRequireDefault(_lodash);

var _utilArity = require('util-arity');

var _utilArity2 = _interopRequireDefault(_utilArity);

var _isGenerator = require('is-generator');

var _isGenerator2 = _interopRequireDefault(_isGenerator);

var _path = require('path');

var _path2 = _interopRequireDefault(_path);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function wrapDefinitions(_ref) {
  var cwd = _ref.cwd,
      definitionFunctionWrapper = _ref.definitionFunctionWrapper,
      definitions = _ref.definitions;

  if (definitionFunctionWrapper) {
    definitions.forEach(function (definition) {
      var codeLength = definition.code.length;
      var wrappedFn = definitionFunctionWrapper(definition.code, definition.options.wrapperOptions);
      if (wrappedFn !== definition.code) {
        definition.code = (0, _utilArity2.default)(codeLength, wrappedFn);
      }
    });
  } else {
    var generatorDefinitions = _lodash2.default.filter(definitions, function (definition) {
      return _isGenerator2.default.fn(definition.code);
    });
    if (generatorDefinitions.length > 0) {
      var references = generatorDefinitions.map(function (definition) {
        return _path2.default.relative(cwd, definition.uri) + ':' + definition.line;
      }).join('\n  ');
      var message = '\n        The following hook/step definitions use generator functions:\n\n          ' + references + '\n\n        Use \'this.setDefinitionFunctionWrapper(fn)\' to wrap then in a function that returns a promise.\n        ';
      throw new Error(message);
    }
  }
}
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9zdXBwb3J0X2NvZGVfbGlicmFyeV9idWlsZGVyL2ZpbmFsaXplX2hlbHBlcnMuanMiXSwibmFtZXMiOlsid3JhcERlZmluaXRpb25zIiwiY3dkIiwiZGVmaW5pdGlvbkZ1bmN0aW9uV3JhcHBlciIsImRlZmluaXRpb25zIiwiZm9yRWFjaCIsImNvZGVMZW5ndGgiLCJkZWZpbml0aW9uIiwiY29kZSIsImxlbmd0aCIsIndyYXBwZWRGbiIsIm9wdGlvbnMiLCJ3cmFwcGVyT3B0aW9ucyIsImdlbmVyYXRvckRlZmluaXRpb25zIiwiZmlsdGVyIiwiZm4iLCJyZWZlcmVuY2VzIiwibWFwIiwicmVsYXRpdmUiLCJ1cmkiLCJsaW5lIiwiam9pbiIsIm1lc3NhZ2UiLCJFcnJvciJdLCJtYXBwaW5ncyI6Ijs7Ozs7UUFLZ0JBLGUsR0FBQUEsZTs7QUFMaEI7Ozs7QUFDQTs7OztBQUNBOzs7O0FBQ0E7Ozs7OztBQUVPLFNBQVNBLGVBQVQsT0FJSjtBQUFBLE1BSERDLEdBR0MsUUFIREEsR0FHQztBQUFBLE1BRkRDLHlCQUVDLFFBRkRBLHlCQUVDO0FBQUEsTUFEREMsV0FDQyxRQUREQSxXQUNDOztBQUNELE1BQUlELHlCQUFKLEVBQStCO0FBQzdCQyxnQkFBWUMsT0FBWixDQUFvQixzQkFBYztBQUNoQyxVQUFNQyxhQUFhQyxXQUFXQyxJQUFYLENBQWdCQyxNQUFuQztBQUNBLFVBQU1DLFlBQVlQLDBCQUNoQkksV0FBV0MsSUFESyxFQUVoQkQsV0FBV0ksT0FBWCxDQUFtQkMsY0FGSCxDQUFsQjtBQUlBLFVBQUlGLGNBQWNILFdBQVdDLElBQTdCLEVBQW1DO0FBQ2pDRCxtQkFBV0MsSUFBWCxHQUFrQix5QkFBTUYsVUFBTixFQUFrQkksU0FBbEIsQ0FBbEI7QUFDRDtBQUNGLEtBVEQ7QUFVRCxHQVhELE1BV087QUFDTCxRQUFNRyx1QkFBdUIsaUJBQUVDLE1BQUYsQ0FBU1YsV0FBVCxFQUFzQixzQkFBYztBQUMvRCxhQUFPLHNCQUFZVyxFQUFaLENBQWVSLFdBQVdDLElBQTFCLENBQVA7QUFDRCxLQUY0QixDQUE3QjtBQUdBLFFBQUlLLHFCQUFxQkosTUFBckIsR0FBOEIsQ0FBbEMsRUFBcUM7QUFDbkMsVUFBTU8sYUFBYUgscUJBQ2hCSSxHQURnQixDQUNaLHNCQUFjO0FBQ2pCLGVBQU8sZUFBS0MsUUFBTCxDQUFjaEIsR0FBZCxFQUFtQkssV0FBV1ksR0FBOUIsSUFBcUMsR0FBckMsR0FBMkNaLFdBQVdhLElBQTdEO0FBQ0QsT0FIZ0IsRUFJaEJDLElBSmdCLENBSVgsTUFKVyxDQUFuQjtBQUtBLFVBQU1DLG1HQUdBTixVQUhBLDJIQUFOO0FBT0EsWUFBTSxJQUFJTyxLQUFKLENBQVVELE9BQVYsQ0FBTjtBQUNEO0FBQ0Y7QUFDRiIsImZpbGUiOiJmaW5hbGl6ZV9oZWxwZXJzLmpzIiwic291cmNlc0NvbnRlbnQiOlsiaW1wb3J0IF8gZnJvbSAnbG9kYXNoJ1xuaW1wb3J0IGFyaXR5IGZyb20gJ3V0aWwtYXJpdHknXG5pbXBvcnQgaXNHZW5lcmF0b3IgZnJvbSAnaXMtZ2VuZXJhdG9yJ1xuaW1wb3J0IHBhdGggZnJvbSAncGF0aCdcblxuZXhwb3J0IGZ1bmN0aW9uIHdyYXBEZWZpbml0aW9ucyh7XG4gIGN3ZCxcbiAgZGVmaW5pdGlvbkZ1bmN0aW9uV3JhcHBlcixcbiAgZGVmaW5pdGlvbnNcbn0pIHtcbiAgaWYgKGRlZmluaXRpb25GdW5jdGlvbldyYXBwZXIpIHtcbiAgICBkZWZpbml0aW9ucy5mb3JFYWNoKGRlZmluaXRpb24gPT4ge1xuICAgICAgY29uc3QgY29kZUxlbmd0aCA9IGRlZmluaXRpb24uY29kZS5sZW5ndGhcbiAgICAgIGNvbnN0IHdyYXBwZWRGbiA9IGRlZmluaXRpb25GdW5jdGlvbldyYXBwZXIoXG4gICAgICAgIGRlZmluaXRpb24uY29kZSxcbiAgICAgICAgZGVmaW5pdGlvbi5vcHRpb25zLndyYXBwZXJPcHRpb25zXG4gICAgICApXG4gICAgICBpZiAod3JhcHBlZEZuICE9PSBkZWZpbml0aW9uLmNvZGUpIHtcbiAgICAgICAgZGVmaW5pdGlvbi5jb2RlID0gYXJpdHkoY29kZUxlbmd0aCwgd3JhcHBlZEZuKVxuICAgICAgfVxuICAgIH0pXG4gIH0gZWxzZSB7XG4gICAgY29uc3QgZ2VuZXJhdG9yRGVmaW5pdGlvbnMgPSBfLmZpbHRlcihkZWZpbml0aW9ucywgZGVmaW5pdGlvbiA9PiB7XG4gICAgICByZXR1cm4gaXNHZW5lcmF0b3IuZm4oZGVmaW5pdGlvbi5jb2RlKVxuICAgIH0pXG4gICAgaWYgKGdlbmVyYXRvckRlZmluaXRpb25zLmxlbmd0aCA+IDApIHtcbiAgICAgIGNvbnN0IHJlZmVyZW5jZXMgPSBnZW5lcmF0b3JEZWZpbml0aW9uc1xuICAgICAgICAubWFwKGRlZmluaXRpb24gPT4ge1xuICAgICAgICAgIHJldHVybiBwYXRoLnJlbGF0aXZlKGN3ZCwgZGVmaW5pdGlvbi51cmkpICsgJzonICsgZGVmaW5pdGlvbi5saW5lXG4gICAgICAgIH0pXG4gICAgICAgIC5qb2luKCdcXG4gICcpXG4gICAgICBjb25zdCBtZXNzYWdlID0gYFxuICAgICAgICBUaGUgZm9sbG93aW5nIGhvb2svc3RlcCBkZWZpbml0aW9ucyB1c2UgZ2VuZXJhdG9yIGZ1bmN0aW9uczpcblxuICAgICAgICAgICR7cmVmZXJlbmNlc31cblxuICAgICAgICBVc2UgJ3RoaXMuc2V0RGVmaW5pdGlvbkZ1bmN0aW9uV3JhcHBlcihmbiknIHRvIHdyYXAgdGhlbiBpbiBhIGZ1bmN0aW9uIHRoYXQgcmV0dXJucyBhIHByb21pc2UuXG4gICAgICAgIGBcbiAgICAgIHRocm93IG5ldyBFcnJvcihtZXNzYWdlKVxuICAgIH1cbiAgfVxufVxuIl19