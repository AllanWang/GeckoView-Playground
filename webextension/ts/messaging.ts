const port = browser.runtime.connectNative("chordataTestChannel");
port.onMessage.addListener(response => {
    const model = <ExtensionModel>response;
    // Let's just echo the message back
    port.postMessage(`Hello world - Received: ${JSON.stringify(response)}`);
});
port.postMessage("Hello world 3 (port)");
