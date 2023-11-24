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
    useLocalResources,
} = require("@wrensecurity/commons-ui-build");
const gulp = require("gulp");
const { join, dirname } = require("path");

const TARGET_PATH = "target/www";

gulp.task("build:assets", useLocalResources({ "src/main/resources/**": "" }, { dest: TARGET_PATH }));

gulp.task("build:swagger", () => {
    const baseDir = dirname(require.resolve("swagger-ui/dist/swagger-ui.js"));
    return gulp.src([
        `${baseDir}/swagger-ui.js`,
        `${baseDir}/swagger-ui.min.js`,
        `${baseDir}/css/*`,
        `${baseDir}/fonts/*`,
        `${baseDir}/images/*`,
        `${baseDir}/lang/*`,
        `${baseDir}/lib/*`,
    ], { base: baseDir }).pipe(gulp.dest(TARGET_PATH));
});

gulp.task("build:swagger-themes", () => gulp.src(require.resolve("swagger-ui-themes/themes/theme-flattop.css"))
    .pipe(gulp.dest(join(TARGET_PATH, "css"))));

gulp.task("build", gulp.parallel(
    "build:assets",
    "build:swagger",
    "build:swagger-themes"
));

gulp.task("default", gulp.series("build"));
