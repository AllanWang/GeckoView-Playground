declare namespace browser.runtime {
    interface Port {
        postMessage: (i: string) => void;
    }
}