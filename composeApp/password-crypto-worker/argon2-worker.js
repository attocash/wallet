import { argon2id } from "hash-wasm";

self.onmessage = async ({ data }) => {
  // One derivation per worker. Its owner terminates it after success, failure, or cancellation.
  self.onmessage = null;
  try {
    const key = await argon2id({
      password: data.password,
      salt: data.salt,
      iterations: data.iterations,
      memorySize: data.memoryKiB,
      parallelism: data.parallelism,
      hashLength: 32,
      outputType: "binary",
    });
    self.postMessage({ key }, [key.buffer]);
  } catch {
    self.postMessage({ error: true });
  } finally {
    data.password.fill(0);
    self.close();
  }
};
