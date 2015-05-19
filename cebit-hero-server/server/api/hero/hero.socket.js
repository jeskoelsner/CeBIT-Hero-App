/**
 * Broadcast updates to client when the model changes
 */

'use strict';

var Hero = require('./hero.model');

exports.register = function(socket) {
  Hero.schema.post('save', function (doc) {
    onSave(socket, doc);
  });
  Hero.schema.post('remove', function (doc) {
    onRemove(socket, doc);
  });
}

function onSave(socket, doc, cb) {
  socket.emit('hero:save', doc);
}

function onRemove(socket, doc, cb) {
  socket.emit('hero:remove', doc);
}