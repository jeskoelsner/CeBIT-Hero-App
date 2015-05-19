'use strict';

var mongoose = require('mongoose'),
		moment = require('moment'),
		hashids = new require('hashids')('livesapp-is_emurgency', 4),
		Schema = mongoose.Schema;

var HeroSchema = new Schema({
	id: Number,
	code: String,
	lat: Number,
	lng: Number,

	name: String,
	gender: String,
	company: String,

	phone: String,
	email: String,

	anon: { type: Boolean, default: false },

	score: { type: Number, default: 1 },
	scorehistory: Array,

	servertime: { type: Number, default: function(){return new Date().getTime()} }
});

HeroSchema.pre('init', function(next, data) {
	data.servertime = moment().utc().valueOf();
	next();
});

HeroSchema.pre('save', function (next) {
	this.code = hashids.encode(this.id);
	next();
});

module.exports = mongoose.model('Hero', HeroSchema);