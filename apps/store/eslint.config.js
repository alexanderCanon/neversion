// @ts-check
const baseConfig = require("../../eslint.config.js");

module.exports = [
  ...baseConfig,
  {
    files: ["**/*.ts"],
    rules: {
      // En Angular 16 todavía usamos módulos, así que desactivamos standalone obligatorio
      "@angular-eslint/prefer-standalone": "off"
    },
  },
];
