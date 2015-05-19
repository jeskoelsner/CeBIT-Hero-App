'use strict';

var express = require('express');
var controller = require('./mission.controller');

var router = express.Router();

router.get('/', controller.index);
router.get('/:id', controller.show);  //with id=0 latest mission
router.post('/', controller.create);
router.post('/shoot/:id', controller.shoot);
router.put('/:id', controller.update);
router.patch('/:id', controller.update);
router.delete('/:id', controller.destroy);
router.delete('/', controller.drop);

module.exports = router;
