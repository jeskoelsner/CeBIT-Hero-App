'use strict';

describe('Controller: HeromapCtrl', function () {

  // load the controller's module
  beforeEach(module('cebitHeroRevisedApp'));

  var HeromapCtrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    HeromapCtrl = $controller('HeromapCtrl', {
      $scope: scope
    });
  }));

  it('should ...', function () {
    expect(1).toEqual(1);
  });
});
