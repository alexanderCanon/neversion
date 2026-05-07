// @ts-check
const baseConfig = require("../../eslint.config.js");

module.exports = [
  ...baseConfig,
  {
    files: ["**/*.ts"],
    rules: {
      // Reglas específicas para el Panel (Angular 17+)
      "@angular-eslint/prefer-standalone": "error"
    },
  },
];
