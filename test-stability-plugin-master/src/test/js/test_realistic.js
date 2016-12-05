QUnit.test( "Real Hide Div Test", function( assert ) {
    
  assert.ok($("#test-div-displayed").css('display') != 'none', "Div Displayed!" );
    assert.ok($("#test-div").css('display') == 'none', "Div 1 Hidden!, Test Passed!");
});
