const path = require('path');

// Point Puppeteer cache to project-local directory
// When inlined into karma.conf.js, __dirname is build/wasm/packages/attoWallet-test/
// so we need four levels up to reach the project root
process.env.PUPPETEER_CACHE_DIR = path.join(__dirname, '..', '..', '..', '..', '.cache', 'puppeteer');

process.env.CHROME_BIN = require('puppeteer').executablePath();
