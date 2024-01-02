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
 * Copyright 2023 Wren Security.
 */
const {
    useEslint,
    useLocalResources
} = require("@wrensecurity/commons-ui-build");
const gulp = require("gulp");

const TARGET_PATH = "target/www";

gulp.task("eslint", useEslint({ src: "src/main/js/**/*.js" }));

gulp.task("build:assets", useLocalResources({ "src/main/resources/**": "" }, { dest: TARGET_PATH }));

gulp.task("build:scripts", useLocalResources({ "src/main/js/**/*.js": "" }, { dest: TARGET_PATH }));

gulp.task("build", gulp.series(
    gulp.parallel(
        "build:assets",
        "build:scripts"
    )
));

gulp.task("watch", () => {
    gulp.watch("src/main/js/**", gulp.parallel("build:scripts"));
});

gulp.task("default", gulp.series("eslint", "build"));
