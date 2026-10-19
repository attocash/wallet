const CACHE_VERSION = new URL(self.location.href).searchParams.get("hash");
if (!CACHE_VERSION) {
  throw new Error("Missing service-worker build hash");
}
const CACHE_NAME = "atto-wallet-" + CACHE_VERSION;
const APP_SHELL = [
  "./",
  "./index.html",
  "./manifest.webmanifest",
  "./logo-touch-180.png",
  "./logo-192.png",
  "./logo-512.png",
  "./logo-maskable-192.png",
  "./logo-maskable-512.png",
  "./logo-monochrome-192.png",
  "./logo-monochrome-512.png",
  "./og-image.png",
  "./logo.svg",
  "./logo-maskable.svg",
  "./logo-monochrome.svg",
  "./logo.ico",
  "./composeResources/attowallet.composeapp.generated.resources/drawable/logo.svg",
  "./composeResources/attowallet.composeapp.generated.resources/font/noto_sans_variable.ttf"
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) =>
      cache.addAll(APP_SHELL.map((path) => new Request(path, { cache: "reload" })))
    ).then(() => self.skipWaiting())
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((key) => key !== CACHE_NAME).map((key) => caches.delete(key)))
    ).then(() => self.clients.claim())
  );
});

self.addEventListener("fetch", (event) => {
  const { request } = event;
  const url = new URL(request.url);

  if (request.method !== "GET" || url.origin !== self.location.origin) {
    return;
  }

  if (request.mode === "navigate") {
    event.respondWith(
      fetch(request).catch(() => caches.match("./index.html"))
    );
    return;
  }

  event.respondWith(
    caches.match(request).then((cachedResponse) => {
      if (cachedResponse) {
        return cachedResponse;
      }

      return fetch(request).then((networkResponse) => {
        if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== "basic") {
          return networkResponse;
        }

        const responseToCache = networkResponse.clone();
        caches.open(CACHE_NAME).then((cache) => cache.put(request, responseToCache));
        return networkResponse;
      });
    })
  );
});
