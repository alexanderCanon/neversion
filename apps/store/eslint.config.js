// @ts-check
const baseConfig = require("../../eslint.config.js");

module.exports = [
  ...baseConfig,
  {
    files: ["**/*.ts"],
    rules: {
      // The Store keeps NgModules while migrating its build and SSR infrastructure.
      "@angular-eslint/prefer-standalone": "off"
    },
  },
];
