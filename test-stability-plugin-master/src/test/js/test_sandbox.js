$('#filter-data').html('[filter, filter1, filter2]');
QUnit.test( "Sandbox Hide Div Test", function( assert ) {
    
  assert.ok($("#test-div-displayed").css('display') != 'none', "Div Displayed!" );
  assert.ok($("#test-div").css('display') == 'none', "Div Hidden, Passed!" );
    
    assert.ok($("#test-div1").css('display') == 'none', "Div 1 Hidden!");
    assert.ok($("#test-div2").css('display') == 'none', "Div 2 Hidden!, passed filter test!");
});
