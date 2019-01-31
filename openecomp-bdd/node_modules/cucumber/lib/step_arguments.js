'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.buildStepArgumentIterator = buildStepArgumentIterator;

var _util = require('util');

var _util2 = _interopRequireDefault(_util);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function buildStepArgumentIterator(mapping) {
  return function (arg) {
    if (arg.hasOwnProperty('rows')) {
      return mapping.dataTable(arg);
    } else if (arg.hasOwnProperty('content')) {
      return mapping.docString(arg);
    } else {
      throw new Error('Unknown argument type:' + _util2.default.inspect(arg));
    }
  };
}
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uL3NyYy9zdGVwX2FyZ3VtZW50cy5qcyJdLCJuYW1lcyI6WyJidWlsZFN0ZXBBcmd1bWVudEl0ZXJhdG9yIiwibWFwcGluZyIsImFyZyIsImhhc093blByb3BlcnR5IiwiZGF0YVRhYmxlIiwiZG9jU3RyaW5nIiwiRXJyb3IiLCJpbnNwZWN0Il0sIm1hcHBpbmdzIjoiOzs7OztRQUVnQkEseUIsR0FBQUEseUI7O0FBRmhCOzs7Ozs7QUFFTyxTQUFTQSx5QkFBVCxDQUFtQ0MsT0FBbkMsRUFBNEM7QUFDakQsU0FBTyxVQUFTQyxHQUFULEVBQWM7QUFDbkIsUUFBSUEsSUFBSUMsY0FBSixDQUFtQixNQUFuQixDQUFKLEVBQWdDO0FBQzlCLGFBQU9GLFFBQVFHLFNBQVIsQ0FBa0JGLEdBQWxCLENBQVA7QUFDRCxLQUZELE1BRU8sSUFBSUEsSUFBSUMsY0FBSixDQUFtQixTQUFuQixDQUFKLEVBQW1DO0FBQ3hDLGFBQU9GLFFBQVFJLFNBQVIsQ0FBa0JILEdBQWxCLENBQVA7QUFDRCxLQUZNLE1BRUE7QUFDTCxZQUFNLElBQUlJLEtBQUosQ0FBVSwyQkFBMkIsZUFBS0MsT0FBTCxDQUFhTCxHQUFiLENBQXJDLENBQU47QUFDRDtBQUNGLEdBUkQ7QUFTRCIsImZpbGUiOiJzdGVwX2FyZ3VtZW50cy5qcyIsInNvdXJjZXNDb250ZW50IjpbImltcG9ydCB1dGlsIGZyb20gJ3V0aWwnXG5cbmV4cG9ydCBmdW5jdGlvbiBidWlsZFN0ZXBBcmd1bWVudEl0ZXJhdG9yKG1hcHBpbmcpIHtcbiAgcmV0dXJuIGZ1bmN0aW9uKGFyZykge1xuICAgIGlmIChhcmcuaGFzT3duUHJvcGVydHkoJ3Jvd3MnKSkge1xuICAgICAgcmV0dXJuIG1hcHBpbmcuZGF0YVRhYmxlKGFyZylcbiAgICB9IGVsc2UgaWYgKGFyZy5oYXNPd25Qcm9wZXJ0eSgnY29udGVudCcpKSB7XG4gICAgICByZXR1cm4gbWFwcGluZy5kb2NTdHJpbmcoYXJnKVxuICAgIH0gZWxzZSB7XG4gICAgICB0aHJvdyBuZXcgRXJyb3IoJ1Vua25vd24gYXJndW1lbnQgdHlwZTonICsgdXRpbC5pbnNwZWN0KGFyZykpXG4gICAgfVxuICB9XG59XG4iXX0=