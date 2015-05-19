'use strict';

describe('Controller: PresentationCtrl', function () {

  // load the controller's module
  beforeEach(module('cebitHeroRevisedApp'));

  var PresentationCtrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    PresentationCtrl = $controller('PresentationCtrl', {
      $scope: scope
    });
  }));

  it('should ...', function () {
    expect(1).toEqual(1);
  });
});
