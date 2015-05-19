'use strict';

var mongoose = require('mongoose'),
		moment = require('moment'),
		Schema = mongoose.Schema;

var MissionSchema = new Schema({
	lat: Number,
	lng: Number,
	score: { type: Number, default: 10 },
	created: { type: Number, default: function(){return new Date().getTime()} },

	runtime: { type: Number, default: 1000*60*15 },
	pausetime: { type: Number, default: 1000*60*60 },

	servertime: { type: Number, default: function(){return new Date().getTime()}}
});

MissionSchema.pre('init', function(next, data) {
	data.servertime = moment().utc().valueOf();
	next();
});

module.exports = mongoose.model('Mission', MissionSchema);