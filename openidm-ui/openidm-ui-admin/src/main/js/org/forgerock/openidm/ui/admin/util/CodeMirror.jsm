/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2025 Wren Security.
 */

import { EditorView, basicSetup } from "codemirror";
import { javascript } from "@codemirror/lang-javascript";
import { StreamLanguage } from "@codemirror/language";
import { groovy } from "@codemirror/legacy-modes/mode/groovy";
import { xml } from "@codemirror/legacy-modes/mode/xml";
import { EditorState, StateEffect, Compartment } from "@codemirror/state";
import { placeholder } from "@codemirror/view";

/**
 * @typedef {"groovy" | "javascript" | "xml"} SupportedLanguage
 */

/**
 * @typedef {Object} CodeMirrorOptions
 * @property {SupportedLanguage} mode - script language
 * @property {string} value - initial script content
 * @property {Function[]} updateCallbacks - array of callbacks to be called on editor update
 * @property {boolean} [readonly] - whether the editor should be readonly
 * @property {boolean} [lineWrapping] - whether line wrapping should be enabled
 * @property {string} [height] - height of the editor (e.g., "240px", "10rem")
 * @property {string} [placeholder] - placeholder text to show when editor is empty
 */

const resolveLanguage = (lang) => {
    if (lang === "groovy") {
        return StreamLanguage.define(groovy);
    }
    if (lang === "xml") {
        return StreamLanguage.define(xml);
    }
    return javascript();
};

/**
 * CodeMirror script editor utility class.
 *
 * @param {Element} parent Element to attach the editor to.
 * @param {CodeMirrorOptions} [options]
 */
export default function (parent, options) {
    const themeConfig = {
        "&": {
            border: "1px solid #dbdbdb",
            backgroundColor: '#ffffff'
        }
    };

    if (options.readonly) {
        themeConfig[".cm-cursor"] = { display: "none" };
        themeConfig[".cm-dropCursor"] = { display: "none" };
        themeConfig[".cm-activeLine"] = { backgroundColor: "unset" };
        themeConfig[".cm-activeLineGutter"] = { backgroundColor: "unset" };
        themeConfig["&"].backgroundColor = "#f7f7f7";
    }

    const height = options.height ?? '100%';
    themeConfig["&.cm-editor"] = { height: height };
    themeConfig[".cm-scroller"] = { "max-height": height, overflow: 'auto' };

    const theme = EditorView.theme(themeConfig);

    const languageConf = new Compartment();
    const readonlyConf = new Compartment();
    const editableConf = new Compartment();
    const lineWrappingConf = new Compartment();

    const extensions = [
        basicSetup,
        theme,
        languageConf.of(resolveLanguage(options.mode)),
        readonlyConf.of(options.readonly ? EditorState.readOnly.of(true) : []),
        editableConf.of(options.readonly ? EditorView.editable.of(false) : []),
        lineWrappingConf.of(options.lineWrapping ? EditorView.lineWrapping : [])
    ];

    if (options.updateCallbacks?.length > 0) {
        extensions.push(...options.updateCallbacks.map((cb) => EditorView.updateListener.of(cb)));
    }

    if (options.placeholder) {
        extensions.push(placeholder(options.placeholder));
    }

    // Create editor view
    const editor = new EditorView({
        parent,
        doc: options.value ?? "",
        extensions
    });

    return {
        /**
         * Editor instance.
         */
        editor,
        /**
         * @returns {Element} parent element containing the editor
         */
        getParent: () => parent,
        /**
         * @returns {string} script content string
         */
        getValue: () => editor.state.doc.toString(),
        /**
         * Set script content to provided string value.
         * @param {string} content script content
         */
        setValue: (content) => {
            editor.dispatch({
                changes: { from: 0, to: editor.state.doc.toString().length, insert: content }
            });
        },
        /**
         * Set script language.
         * @param {SupportedLanguage} lang target script language
         */
        setLanguage: (lang) => {
            editor.dispatch({
                effects: languageConf.reconfigure(resolveLanguage(lang))
            });
        },
        /**
         * Set readonly mode.
         * @param {boolean} readonly whether the editor should be readonly
         */
        setReadonly: (readonly) => {
            editor.dispatch({
                effects: [
                    readonlyConf.reconfigure(readonly ? EditorState.readOnly.of(true) : []),
                    editableConf.reconfigure(readonly ? EditorView.editable.of(false) : [])
                ]
            });
        },
        /**
         * Focus the editor.
         */
        focus: () => {
            editor.focus();
        },
        /**
         * Add an update listener to the editor.
         * @param {Function} callback update listener callback function
         */
        addUpdateListener: (callback) => {
            editor.dispatch({
                effects: StateEffect.appendConfig.of([EditorView.updateListener.of(callback)])
            });
        },
        /**
         * Destroy the editor instance.
         */
        destroy: () => {
            editor.destroy();
        }
    };
}
