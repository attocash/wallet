// contenthash.js
const path = require("path");
const fs = require("fs");
const crypto = require("crypto");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const htmlTemplate = path.resolve(__dirname, "../../../../composeApp/src/wasmJsMain/htmlTemplate/index.html");
const inputDirectory = path.resolve(__dirname, "kotlin");

if (config.devServer) {
  config.devServer.static = [...(config.devServer.static || []), inputDirectory];
}

class WebLogoHashPlugin {
  apply(compiler) {
    const pluginName = "WebLogoHashPlugin";
    compiler.hooks.thisCompilation.tap(pluginName, (compilation) => {
      const inputs = new Map();
      const readInput = (filename, filepath) => {
        compilation.fileDependencies.add(filepath);
        const bytes = fs.readFileSync(filepath);
        inputs.set(filename, bytes);
        return bytes.toString("utf8");
      };
      const manifest = readInput("manifest.webmanifest", path.resolve(inputDirectory, "manifest.webmanifest"));
      const worker = readInput("service-worker.js", path.resolve(inputDirectory, "service-worker.js"));
      const template = readInput("index.html", htmlTemplate);
      // Only root web icons are versioned; the shared Compose SVG keeps its path.
      const logoReference = /(["'])(\.\/)?(logo(?:-[a-z0-9]+)*\.(?:svg|png|ico))\1/g;
      const iconNames = new Set(
        [manifest, worker, template].flatMap((source) =>
          [...source.matchAll(logoReference)].map((match) => match[3])
        )
      );
      const iconHashes = new Map();
      for (const filename of [...iconNames].sort()) {
        const filepath = path.resolve(inputDirectory, filename);
        compilation.fileDependencies.add(filepath);
        const bytes = fs.readFileSync(filepath);
        inputs.set(filename, bytes);
        iconHashes.set(filename, crypto.createHash("sha256").update(bytes).digest("hex").slice(0, 16));
      }
      const versionIcons = (source) => source.replace(
        logoReference,
        (_, quote, prefix = "", filename) => `${quote}${prefix}${filename}?hash=${iconHashes.get(filename)}${quote}`
      );

      // Static-only changes must also invalidate the worker's build cache name.
      compilation.hooks.fullHash.tap(pluginName, (hash) => {
        for (const filename of [...inputs.keys()].sort()) {
          hash.update(filename);
          hash.update("\0");
          hash.update(inputs.get(filename));
          hash.update("\0");
        }
      });
      compilation.hooks.processAssets.tap(
        { name: pluginName, stage: compiler.webpack.Compilation.PROCESS_ASSETS_STAGE_ADDITIONAL },
        () => {
          const { RawSource } = compiler.webpack.sources;
          compilation.emitAsset("manifest.webmanifest", new RawSource(versionIcons(manifest)));
          compilation.emitAsset("service-worker.js", new RawSource(versionIcons(worker)));
        }
      );
      HtmlWebpackPlugin.getHooks(compilation).afterTemplateExecution.tap(pluginName, (data) => {
        data.html = versionIcons(data.html);
        return data;
      });
    });
  }
}

config.output = config.output || {};
config.output.filename = "attoWallet.[contenthash].js";
config.plugins.push(
  new WebLogoHashPlugin(),
  new HtmlWebpackPlugin({
    cache: false,
    template: htmlTemplate,
    templateParameters: (compilation, assets, assetTags, options) => {
      const buildHash = compilation.fullHash;
      if (!buildHash) {
        throw new Error("Webpack build hash is unavailable");
      }
      return {
        compilation,
        webpackConfig: compilation.options,
        htmlWebpackPlugin: {
          tags: assetTags,
          files: assets,
          options
        },
        serviceWorkerVersion: buildHash
      };
    }
  })
);
