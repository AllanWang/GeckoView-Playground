async function updateCookies(changeInfo: browser.cookies._OnChangedChangeInfo) {

    const application = "frostBackgroundChannel"

    if (changeInfo.cookie.storeId == 'firefox-default') return

    browser.runtime.sendNativeMessage(application, changeInfo)
}

async function readCookies() {
    const application = "frostBackgroundChannel"

    browser.runtime.sendNativeMessage(application, 'start cookie fetch')

    // Testing with domains or urls didn't work
    const cookies = await browser.cookies.getAll({
    });

    const cookies2 = await browser.cookies.getAll({
        domain: ".facebook.com",
        storeId: "firefox-default"
    })

    const cookieStores = await browser.cookies.getAllCookieStores();

    const all: string[] = []

    for (let store of cookieStores) {
        const c = await browser.cookies.getAll({
            storeId: store.id
        })
        all.push(store.id)
        all.push(c.length.toString())
    }

    const ids = await browser.contextualIdentities.query({})

    const tabs = await browser.tabs.query({})

    browser.runtime.sendNativeMessage(application, { name: "cookies", data: all,
    contexts: ids.map(t => t.name),
    tabs: tabs.map(t => `${t.id} ${t.cookieStoreId}`),
    data3: cookies.filter(s => s.storeId != 'firefox-default').length })
}

async function handleMessage(request: any, sender: browser.runtime.MessageSender, sendResponse: (response?: any) => void) {
    browser.runtime.sendNativeMessage("frostBackgroundChannel", 'pre send')

    await new Promise(resolve => setTimeout(resolve, 1000));

    browser.runtime.sendNativeMessage("frostBackgroundChannel", 'post send')

    sendResponse({ received: request, asdf: "asdf" })
}

// Reading cookies with storeId might not be fully supported on Android
// https://stackoverflow.com/q/76505000/4407321
// Using manifest 3 stopped getAll from working
// Reading now always shows storeId as firefox-default
// Setting a cookie with a custom container does not seem to work

browser.cookies.onChanged.addListener(updateCookies);
browser.tabs.onActivated.addListener(readCookies);
// browser.runtime.onStartup.addListener(readCookies);

// browser.runtime.onMessage.addListener(handleMessage);

function onHeader(details: browser.webRequest._OnSendHeadersDetails) {
    browser.runtime.sendNativeMessage("frostBackgroundChannel",
        { "header": "cookie", "data": [...(details.requestHeaders?.entries() ?? [])] })
}

// browser.webRequest.onSendHeaders.addListener(onHeader, { urls: ["*://*.facebook.com/*"] })