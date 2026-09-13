// contenthash.js
const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");

config.output = config.output || {};
config.output.filename = "attoWallet.[contenthash].js";
config.plugins.push(
  new HtmlWebpackPlugin({
    template: path.resolve(__dirname, "../../../../composeApp/src/wasmJsMain/htmlTemplate/index.html"),
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
