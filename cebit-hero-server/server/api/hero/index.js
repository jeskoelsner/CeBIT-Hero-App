'use strict';

var express = require('express');
var controller = require('./hero.controller');

var router = express.Router();

router.get('/', controller.index);
router.get('/csv/:v', controller.csv);
router.get('/:code', controller.show);
router.post('/', controller.create);
router.post('/:id', controller.register);
router.put('/:code', controller.update);
router.put('/:code/:id', controller.score);
router.delete('/:code', controller.destroy);

module.exports = router;