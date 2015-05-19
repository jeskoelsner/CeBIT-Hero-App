'use strict';

angular.module('cebitHeroRevisedApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('heroid', {
        url: '/heroid',
        templateUrl: 'app/heroid/heroid.html',
        controller: 'HeroidCtrl'
      });
  });