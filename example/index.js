/* Hello, World! Micro Service */
console.log('Hello World!, the Microservice is running!');

// A micro service will exit when it has nothing left to do.  So to
// avoid a premature exit, let's set an indefinite timer.  When we
// exit() later, the timer will get invalidated.
setInterval(function () { }, 1000);

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
    if (watchers[actionName]) {
        if (watchers[actionName]) {
            try {
                watchers[actionName][eventId](reponse.result);
                delete watchers[actionName][eventId];
            } catch (e) { }
        }
    }
})


// Wrapper
function sendAction(actionName, data) {
    return new Promise((resolve, reject) => {
        // var respName = actionName + 'Response';
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
            try {
                delete watchers[actionName][eventId];
            } catch (e) {
            }
            reject('timeout');
        }, 3000);
    });
}



class Driver {

    static findByText(text) {
        return sendAction('findElement', {
            strategy: 'text',
            selector: text
        });
    }

    static getSource() {
        return sendAction('getSource', {});
    }

    static clickElement(elementId) {
        return sendAction('doActionToElement', {
            elementId,
            action: 'click'
        });
    }


    static triggerEventToElement(elementId, type) {
        return sendAction('doActionToElement', {
            elementId,
            action: type
        });
    }
    // static 
}





LiquidCore.on('exit', function (name) {
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
LiquidCore.emit('ready');


const cheerio = require('cheerio');


var ATTR_ID = 'element-id';


async function getDoc() {

    var viewTree = await Driver.getSource();
    var doc = cheerio.load(viewTree, { ignoreWhitespace: true, xmlMode: true });


    doc.prototype.click = function () {
        for (let index = 0; index < this.length; index++) {
            const element = this.eq(index);
            Driver.triggerEventToElement(element.attr(ATTR_ID), 'click');
        }
    }

    doc.prototype.scroll = function (type) {
        type = type || 'forward';
        for (let index = 0; index < this.length; index++) {
            const element = this.eq(index);
            Driver.triggerEventToElement(element.attr(ATTR_ID), 'scroll-' + type);
        }
    }

    return doc;
}



//var TestKit = new TestSuite();



(async function loop() {
    var $ = await getDoc();

    var chrome = $("[text*='惠拍']");

    var clickElements = $("[clickable='true']");
    var scrollElements = $("[scrollable='true']");
    // chrome.eq(0).click();
    // var els = await Driver.findByText("Chrome");
    // els = JSON.parse(els);
    // var source = await Driver.getSource();
    // console.log('source', chrome.length);
    // console.log('findByTextNNN', els);
    // if (els.length > 0) {
    //     Driver.clickElement(els[0].elementId)
    // };

    if (chrome.length) {
        console.log('chrome', chrome.length, chrome.attr());
        var icon = chrome.eq(0);
        console.log('chrome icon id', icon.attr(ATTR_ID))
        Driver.clickElement(icon.attr(ATTR_ID));
        // return;
    }

    for (let index = 0; index < clickElements.length; index++) {
        const element = clickElements.eq(index);
        console.log('clickElements', element.attr());
        // element.click();
    }

    for (let index = 0; index < scrollElements.length; index++) {
        const element = scrollElements.eq(index);
        console.log('scrollElements', element.attr())
        element.scroll();
    }

    setTimeout(loop, 5000);
})();