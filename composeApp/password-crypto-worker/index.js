export function createArgon2Worker() {
  return new Worker(new URL("./argon2-worker.js", import.meta.url), {
    type: "module",
    name: "wallet-argon2",
  });
}
