if ("serviceWorker" in navigator) {
    // currentScript is only available while this script is being evaluated.
    const cacheVersion = new URL(document.currentScript.src).searchParams.get("hash");
    if (!cacheVersion) {
        throw new Error("Missing service-worker build hash");
    }

    const hadController = navigator.serviceWorker.controller != null;
    let reloading = false;
    navigator.serviceWorker.addEventListener("controllerchange", function () {
        const controller = navigator.serviceWorker.controller;
        if (!hadController || controller == null || reloading) {
            return;
        }

        const workerVersion = new URL(controller.scriptURL).searchParams.get("hash");
        if (workerVersion === cacheVersion) {
            // This page may have loaded assets through the previous release's worker.
            reloading = true;
            window.location.reload();
        }
    });

    window.addEventListener("load", function () {
        navigator.serviceWorker
            .register("./service-worker.js?hash=" + encodeURIComponent(cacheVersion))
            .catch(function (error) {
                console.error("Service worker registration failed", error);
            });
    });
}
