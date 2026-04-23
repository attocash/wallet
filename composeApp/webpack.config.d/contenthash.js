// contenthash.js
const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");

config.output = config.output || {};
config.output.filename = "attoWallet.[contenthash].js";
config.plugins.push(
  new HtmlWebpackPlugin({
    template: path.resolve(__dirname, "../../../../composeApp/src/wasmJsMain/htmlTemplate/index.html"),
    templateParameters: (compilation, assets, assetTags, options) => ({
      compilation,
      webpackConfig: compilation.options,
      htmlWebpackPlugin: {
        tags: assetTags,
        files: assets,
        options
      },
      serviceWorkerVersion: compilation.fullHash || compilation.hash || "dev"
    })
  })
);
