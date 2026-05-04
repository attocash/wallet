#!/usr/bin/env node

import { existsSync, mkdtempSync, rmSync, statSync, unlinkSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { spawn, spawnSync } from "node:child_process";
import { tmpdir } from "node:os";
import { setTimeout as delay } from "node:timers/promises";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const defaultOutput = resolve(root, "composeApp/src/wasmJsMain/resources/og-image.png");
const defaultUrl = "http://127.0.0.1:8080/?screen=og-image";

const args = new Map(
  process.argv.slice(2).map((arg) => {
    const [key, ...value] = arg.split("=");
    return [key, value.join("=")];
  }),
);

const scriptTimeoutMs = parseTimeoutMs(args.get("--timeout-ms") || "60000");
const scriptTimeout = setTimeout(() => {
  console.error(`Timed out after ${scriptTimeoutMs}ms while generating the OG image.`);
  process.exit(124);
}, scriptTimeoutMs);
const url = args.get("--url") || defaultUrl;
const output = resolve(root, args.get("--out") || defaultOutput);
const browser = args.get("--browser")
  ? browserCommand(args.get("--browser"))
  : findBrowser();

if (!browser) {
  console.error(
    [
      "No supported browser command found.",
      "Install Chromium, Chrome, Edge, Brave, or Firefox, then run:",
      "  ./gradlew wasmJsBrowserDevelopmentRun",
      `  node scripts/generate-og-image.mjs --url=${defaultUrl}`,
    ].join("\n"),
  );
  process.exit(1);
}

await assertReachable(url);

if (existsSync(output)) {
  unlinkSync(output);
}

if (browser.firefox) {
  captureWithFirefox(browser, url, output);
} else {
  await captureWithChrome(browser, url, output);
}

if (!existsSync(output) || statSync(output).size < 10_000) {
  console.error(`Screenshot did not produce ${output}`);
  process.exit(1);
}

console.log(`Generated ${output}`);
clearTimeout(scriptTimeout);
process.exit(0);

function findBrowser() {
  const candidates = [
    process.env.CHROME_BIN && browserCommand(process.env.CHROME_BIN),
    process.env.CHROMIUM_BIN && browserCommand(process.env.CHROMIUM_BIN),
    browserCommand("google-chrome"),
    browserCommand("chromium"),
    browserCommand("chromium-browser"),
    browserCommand("microsoft-edge"),
    browserCommand("brave-browser"),
    browserCommand("firefox"),
    browserCommand("flatpak", [
      "run",
      `--filesystem=${root}`,
      "--command=chrome",
      "com.google.Chrome",
    ]),
    browserCommand("flatpak", [
      "run",
      `--filesystem=${root}`,
      "--command=firefox",
      "org.mozilla.firefox",
    ], true),
  ].filter(Boolean);

  for (const candidate of candidates) {
    const probe = spawnSync(candidate.command[0], [...candidate.command.slice(1), "--version"], {
      cwd: root,
      stdio: "ignore",
    });

    if (probe.status === 0) {
      return candidate;
    }
  }

  return null;
}

function browserCommand(command, args = [], firefox = command.includes("firefox")) {
  return {
    command: [command, ...args],
    firefox,
  };
}

function captureWithFirefox(browser, targetUrl, outputPath) {
  const command = [
    ...browser.command,
    "--headless",
    "--window-size",
    "1200,630",
    "--screenshot",
    outputPath,
    targetUrl,
  ];

  const result = spawnSync(command[0], command.slice(1), {
    cwd: root,
    stdio: "inherit",
  });

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

async function captureWithChrome(browser, targetUrl, outputPath) {
  const profileDir = mkdtempSync(resolve(tmpdir(), "atto-og-chrome-"));
  const command = [
    ...browser.command,
    "--headless=new",
    "--use-angle=swiftshader",
    "--ignore-gpu-blocklist",
    "--enable-unsafe-swiftshader",
    "--hide-scrollbars",
    "--no-first-run",
    "--disable-dev-shm-usage",
    "--force-device-scale-factor=1",
    "--remote-debugging-port=0",
    "--remote-allow-origins=*",
    "--window-size=1200,630",
    `--user-data-dir=${profileDir}`,
    "about:blank",
  ];

  const child = spawn(command[0], command.slice(1), {
    cwd: root,
    stdio: ["ignore", "ignore", "pipe"],
  });

  try {
    const browserWsUrl = await readDevToolsUrl(child);
    const pageWsUrl = await findPageWebSocket(browserWsUrl);
    const cdp = await connectCdp(pageWsUrl);

    try {
      await cdp.send("Page.enable");
      await cdp.send("Runtime.enable");
      await cdp.send("Log.enable");
      await cdp.send("Page.addScriptToEvaluateOnNewDocument", {
        source: `
          window.addEventListener('error', (event) => {
            console.error('window error', event.error?.stack || event.message || String(event.error));
          });
          window.addEventListener('unhandledrejection', (event) => {
            console.error('unhandled rejection', event.reason?.stack || event.reason || String(event.reason));
          });
        `,
      });
      await cdp.send("Emulation.setDeviceMetricsOverride", {
        width: 1200,
        height: 630,
        deviceScaleFactor: 1,
        mobile: false,
      });
      const loaded = cdp.waitForEvent("Page.loadEventFired", 20_000);
      await cdp.send("Page.navigate", { url: targetUrl });
      await loaded;
      await waitForCanvas(cdp, targetUrl);
      await delay(4_000);

      const screenshot = await cdp.send("Page.captureScreenshot", {
        format: "png",
        fromSurface: true,
        clip: {
          x: 0,
          y: 0,
          width: 1200,
          height: 630,
          scale: 1,
        },
      });
      writeFileSync(outputPath, Buffer.from(screenshot.data, "base64"));
    } finally {
      await cdp.close();
    }
  } finally {
    await stopChrome(child);
    rmSync(profileDir, { recursive: true, force: true });
  }
}

function readDevToolsUrl(child) {
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => reject(new Error("Timed out waiting for Chrome DevTools URL")), 15_000);

    child.stderr.on("data", (chunk) => {
      const match = chunk.toString().match(/DevTools listening on (ws:\/\/\S+)/);
      if (match) {
        clearTimeout(timeout);
        resolve(match[1]);
      }
    });

    child.on("exit", (code) => {
      clearTimeout(timeout);
      reject(new Error(`Chrome exited before DevTools was ready (${code})`));
    });
  });
}

async function stopChrome(child) {
  if (child.exitCode != null) return;

  child.kill("SIGTERM");

  const exited = await waitForExit(child, 3000);
  if (!exited && child.exitCode == null) {
    child.kill("SIGKILL");
    await waitForExit(child, 1000);
  }

  child.stderr?.destroy();
}

function waitForExit(child, timeoutMs) {
  if (child.exitCode != null) {
    return Promise.resolve(true);
  }

  return new Promise((resolve) => {
    const timeout = setTimeout(() => {
      child.off("exit", onExit);
      resolve(false);
    }, timeoutMs);
    const onExit = () => {
      clearTimeout(timeout);
      resolve(true);
    };

    child.once("exit", onExit);
  });
}

async function findPageWebSocket(browserWsUrl) {
  const browserUrl = new URL(browserWsUrl);
  const response = await fetch(`http://${browserUrl.host}/json/list`);
  const targets = await response.json();
  const page = targets.find((target) => target.type === "page" && target.webSocketDebuggerUrl);

  if (!page) {
    throw new Error("Chrome did not expose a page target");
  }

  return page.webSocketDebuggerUrl;
}

function connectCdp(wsUrl) {
  const socket = new WebSocket(wsUrl);
  let nextId = 1;
  const pending = new Map();
  const eventWaiters = new Map();
  const diagnostics = [];

  socket.addEventListener("message", (event) => {
    const message = JSON.parse(event.data);

    if (message.id != null) {
      const request = pending.get(message.id);
      if (!request) return;

      pending.delete(message.id);
      if (message.error) {
        request.reject(new Error(message.error.message));
      } else {
        request.resolve(message.result || {});
      }
      return;
    }

    if (
      message.method === "Runtime.exceptionThrown" ||
      message.method === "Runtime.consoleAPICalled" ||
      message.method === "Log.entryAdded"
    ) {
      diagnostics.push(JSON.stringify(message).slice(0, 1000));
    }

    const waiters = eventWaiters.get(message.method);
    if (!waiters?.length) return;

    for (const waiter of waiters.splice(0)) {
      clearTimeout(waiter.timeout);
      waiter.resolve(message.params || {});
    }
  });

  return new Promise((resolve, reject) => {
    socket.addEventListener("open", () => {
      resolve({
        diagnostics,
        send(method, params = {}) {
          const id = nextId++;
          socket.send(JSON.stringify({ id, method, params }));

          return new Promise((requestResolve, requestReject) => {
            pending.set(id, {
              resolve: requestResolve,
              reject: requestReject,
            });
          });
        },
        waitForEvent(method, timeoutMs) {
          return new Promise((eventResolve, eventReject) => {
            const timeout = setTimeout(() => {
              eventReject(new Error(`Timed out waiting for ${method}`));
            }, timeoutMs);
            const waiters = eventWaiters.get(method) || [];

            waiters.push({
              timeout,
              resolve: eventResolve,
            });
            eventWaiters.set(method, waiters);
          });
        },
        close() {
          return new Promise((closeResolve) => {
            if (socket.readyState === 3) {
              closeResolve();
              return;
            }

            const closeTimeout = setTimeout(closeResolve, 1000);

            socket.addEventListener("close", () => {
              clearTimeout(closeTimeout);
              closeResolve();
            }, { once: true });
            socket.close();
          });
        },
      });
    });
    socket.addEventListener("error", reject);
  });
}

function parseTimeoutMs(value) {
  const timeoutMs = Number.parseInt(value, 10);

  if (Number.isFinite(timeoutMs) && timeoutMs > 0) {
    return timeoutMs;
  }

  return 60000;
}

async function waitForCanvas(cdp, targetUrl) {
  const deadline = Date.now() + 20_000;

  while (Date.now() < deadline) {
    const result = await cdp.send("Runtime.evaluate", {
      expression: `
        Boolean(
          document.querySelector('canvas') ||
          document.querySelector('#AttoWallet')?.shadowRoot?.querySelector('canvas') ||
          document.querySelector('flt-glass-pane')?.shadowRoot?.querySelector('canvas')
        )
      `,
      returnByValue: true,
    });

    if (result.result?.value === true) {
      return;
    }

    await delay(250);
  }

  const debug = await cdp.send("Runtime.evaluate", {
    expression: `
      JSON.stringify({
        href: location.href,
        title: document.title,
        scripts: Array.from(document.scripts).map((script) => script.src || script.textContent?.slice(0, 80)),
        resources: performance.getEntriesByType('resource').map((entry) => entry.name).slice(0, 20),
        bodyText: document.body?.innerText?.slice(0, 500) || "",
        bodyHtml: document.body?.innerHTML?.slice(0, 500) || "",
        attoShadowHtml: document.querySelector('#AttoWallet')?.shadowRoot?.innerHTML?.slice(0, 500) || "",
        glassShadowHtml: document.querySelector('flt-glass-pane')?.shadowRoot?.innerHTML?.slice(0, 500) || "",
        diagnostics: ${JSON.stringify(cdp.diagnostics)}
      })
    `,
    returnByValue: true,
  });

  throw new Error(
    `Timed out waiting for Compose canvas at ${targetUrl}: ${debug.result?.value || "no page details"}`,
  );
}

async function assertReachable(targetUrl) {
  try {
    const response = await fetch(targetUrl, {
      signal: AbortSignal.timeout(5000),
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
  } catch (error) {
    console.error(
      [
        `Could not load ${targetUrl}.`,
        "Start the web app first:",
        "  ./gradlew wasmJsBrowserDevelopmentRun",
        `Then run: node scripts/generate-og-image.mjs --url=${targetUrl}`,
        `Reason: ${error.message}`,
      ].join("\n"),
    );
    process.exit(1);
  }
}
