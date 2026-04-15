// contenthash.js
const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");
config.output.filename = "attoWallet.[contenthash].js";
config.plugins.push(
  new HtmlWebpackPlugin({
    template: path.resolve(__dirname, "../../../../composeApp/src/wasmJsMain/htmlTemplate/index.html")
  })
);
