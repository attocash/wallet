import createZXing from "./vendor/zxing_reader.js";

let decoder;
let initialization;

export function initialize() {
  if (!initialization) {
    initialization = createZXing({
      locateFile: () => new URL("./vendor/zxing_reader.wasm", import.meta.url).href,
    }).then((instance) => {
      decoder = instance;
      return instance;
    }).catch((error) => {
      // Reopening the scanner can retry a failed same-origin asset load.
      initialization = undefined;
      throw error;
    });
  }
  return initialization;
}

export function decodeFromCanvas(canvas) {
  if (!decoder) throw new Error("QR decoder has not initialized");
  const width = canvas.width;
  const height = canvas.height;
  if (!width || !height) return null;
  const context = canvas.getContext("2d", { willReadFrequently: true });
  if (!context) return null;
  const pixels = context.getImageData(0, 0, width, height).data;
  const pointer = decoder._malloc(pixels.byteLength);
  if (!pointer) throw new Error("Could not allocate QR image memory");
  try {
    decoder.HEAPU8.set(pixels, pointer);
    const result = decoder.readBarcodeFromPixmap(pointer, width, height, true, "QRCode");
    if (result.error) throw new Error(result.error);
    return result.text || null;
  } finally {
    decoder._free(pointer);
  }
}
