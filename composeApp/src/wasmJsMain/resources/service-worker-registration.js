if ("serviceWorker" in navigator) {
    window.addEventListener("load", function () {
        fetch("./version.txt", { cache: "no-store" })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("Unable to load version");
                }
                return response.text();
            })
            .then(function (version) {
                return navigator.serviceWorker.register("./service-worker.js?v=" + encodeURIComponent(version.trim()));
            })
            .catch(function (error) {
                console.error("Service worker registration failed", error);
            });
    });
}
