function readCookies() {
    const application = "chordataTestChannelBackground"

    const gettingAllCookies = browser.cookies.getAll({url: "https://touch.facebook.com"});

    browser.runtime.sendNativeMessage(application, "fetch cookie")

    gettingAllCookies.then((cookies) => {
        for (let cookie of cookies) {
            browser.runtime.sendNativeMessage(application, `cookie ${cookie.domain} ${cookie.value}`)
        }
    })

}

browser.runtime.sendNativeMessage("chordataTestChannelBackground", "start bg")


browser.tabs.onUpdated.addListener(readCookies);
