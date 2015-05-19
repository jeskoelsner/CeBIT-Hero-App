/**
 * Broadcast updates to client when the model changes
 */

'use strict';

var Mission = require('./mission.model');

exports.register = function(socket) {
  Mission.schema.post('save', function (doc) {
    onSave(socket, doc);
  });
  Mission.schema.post('remove', function (doc) {
    onRemove(socket, doc);
  });
}

function onSave(socket, doc, cb) {
  socket.emit('mission:save', doc);
}

function onRemove(socket, doc, cb) {
  socket.emit('mission:remove', doc);
}