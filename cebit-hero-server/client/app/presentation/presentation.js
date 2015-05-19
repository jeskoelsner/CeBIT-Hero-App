'use strict';

angular.module('cebitHeroRevisedApp')
  .config(function ($stateProvider) {
    $stateProvider
      .state('presentation', {
        url: '/presentation',
        templateUrl: 'app/presentation/presentation.html',
        controller: 'PresentationCtrl'
      });
  });