/* Hello, World! Micro Service */
console.log('Hello World!, the Microservice is running!');

// A micro service will exit when it has nothing left to do.  So to
// avoid a premature exit, let's set an indefinite timer.  When we
// exit() later, the timer will get invalidated.
setInterval(function() {}, 1000);


function getGuid() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = Math.random() * 16 | 0,
            v = c == 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}


var watchers = {};


LiquidCore.on('actionResponse', (reponse) => {
     var originalEvent = reponse.event;
     var eventId = originalEvent.eventId;
     var actionName = originalEvent.actionName;
     if(watchers[actionName]){
        if(watchers[actionName]){
            try{
                watchers[actionName][eventId](reponse.result);
                delete watchers[actionName][eventId];
            }catch(e){}
        }
     }
})


// Wrapper
function sendAction(actionName, data){
    return new Promise((resolve, reject) => {
        var respName = actionName+'Response';
        var eventId = getGuid();
        data.eventId = eventId;
        data.actionName = actionName;

        LiquidCore.emit(actionName, data);

        watchers[actionName] = {};
        watchers[actionName][eventId] = (re) => {
            resolve(re);
        }

//        LiquidCore.on(respName, (reponse) => {
//            var originalEvent = reponse.event;
//            if(originalEvent.eventId == eventId){
//                resolve(reponse.result);
//            }
//        });

        setTimeout(() => {
            try{
                delete watchers[actionName][eventId];
            }catch(e){
            }
            reject('timeout');
        }, 3000);
    });
}

function findByText(text){
    return sendAction('findElement', {
        strategy: 'text',
        selector: text
    });
}

function getSource(){
    return sendAction('getSource', {});
}


LiquidCore.on( 'exit', function(name) {
    process.exit(0);
})


//LiquidCore.on('findByTextResponse', function(data) {
//    var element = JSON.parse(data.result);
//    console.log(element, element.length);
//    var appButon = element[0];
//    if(appButon){
//        console.log("send click");
////        LiquidCore.emit('clickByElementId', {
////            id: appButon.elementId
////        })
//    }
//})


LiquidCore.emit( 'ready' );

//var TestKit = new TestSuite();
setInterval(() => {
    (async () => {
        console.log('source new');
        var els = await findByText("Chrome");
        var source = await getSource();
        console.log('source', source);
        console.log('findByText', els);
    })();

}, 2 * 1000);
