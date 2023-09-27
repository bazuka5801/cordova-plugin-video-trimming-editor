'use strict';

var exec = require('cordova/exec');

var VideoTrimmingEditor = {

  open: function(param, onSuccess, onFail) {
    return exec(onSuccess, onFail, 'VideoTrimmingEditor', 'open', [param]);
  },
  check: function(param, onSuccess, onFail) {
    return exec(onSuccess, onFail, 'VideoTrimmingEditor', 'check', [param]);
  }
};
module.exports = VideoTrimmingEditor;
