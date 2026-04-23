if ("serviceWorker" in navigator) {
    window.addEventListener("load", function () {
        const currentScript = document.currentScript;
        const scriptUrl =
            currentScript == null
                ? null
                : new URL(currentScript.src, window.location.href);
        const cacheVersion =
            scriptUrl == null
                ? "dev"
                : scriptUrl.searchParams.get("hash") || "dev";

        navigator.serviceWorker
            .register("./service-worker.js?hash=" + encodeURIComponent(cacheVersion))
            .catch(function (error) {
                console.error("Service worker registration failed", error);
            });
    });
}
