console.log("Hello world; gecko test")

const manifest = document.querySelector("head > link[rel=manifest]");
browser.runtime.sendNativeMessage("browser", "Hello world 2");
