/* Hello, World! Micro Service */

console.log('Hello World!, the Microservice is running!');

// A micro service will exit when it has nothing left to do.  So to
// avoid a premature exit, let's set an indefinite timer.  When we
// exit() later, the timer will get invalidated.
setInterval(function() {}, 1000);


class TestSuite {
    
    constructor(){

    }

    click(){
        LiquidCore.emit('sendClick', {
            x: 1,
            y: 1
        });
    }
}


LiquidCore.on( 'exit', function(name) {
    process.exit(0);
})

LiquidCore.emit( 'ready' );


var TestKit = new TestSuite();
TestKit.click();
