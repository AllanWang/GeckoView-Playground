console.log("Hello world; gecko test")

const manifest = document.querySelector("head > link[rel=manifest]");
browser.runtime.sendNativeMessage("browser", "Hello world 2 (message)");

const port = browser.runtime.connectNative("chordataTestChannel");
port.onMessage.addListener(response => {
  // Let's just echo the message back
  port.postMessage(`Hello world - Received: ${JSON.stringify(response)}`);
});
port.postMessage("Hello world 3 (port)");