const port = browser.runtime.connectNative("chordataTestChannel");
port.onMessage.addListener(response => {
    const model = <ExtensionModel>response;
    // Let's just echo the message back
    port.postMessage(`Hello world - Received: ${JSON.stringify(response)}`);
});
port.postMessage("Hello world 4 (port)");

function cookieResponse(cookies: any) {
    console.log("Cookie response", cookies)
}

browser.runtime.onMessage.addListener(cookieResponse);