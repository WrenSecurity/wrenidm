import globals from "globals";
import wrensecurityConfig from "@wrensecurity/eslint-config";

export default [
    wrensecurityConfig,
    {
        languageOptions: {
            globals: {
                ...globals.amd,
                ...globals.browser,
                ...globals.qunit,
                ...globals.es2015
            }
        },
        rules: {
            "arrow-parens": ["error", "always"],
            "arrow-spacing": "error",
            "indent": ["error", 4, {
                "FunctionDeclaration": {
                    "parameters": 2
                },
                "FunctionExpression": {
                    "parameters": 2
                },
                "SwitchCase": 1,
                "VariableDeclarator": 1
            }],
            "no-alert": "error",
            "no-empty-character-class": "error",
            "no-extend-native": "error",
            "no-mixed-spaces-and-tabs": "error",
            "no-multiple-empty-lines": "error",
            "no-multi-str": "error",
            "no-unused-vars": "warn", // Temporarily changed to warning due to many inconsistencies
            "no-void": "error"
        }
    },
    {
        files: ["gulpfile.js"],
        languageOptions: {
            globals: {
                ...globals.node
            },
            ecmaVersion: 2021
        }
    }
];
