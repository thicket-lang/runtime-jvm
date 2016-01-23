/*
 * Thicket
 * https://github.com/d-plaindoux/thicket
 *
 * Copyright (c) 2015-2016 Didier Plaindoux
 * Licensed under the LGPL2 license.
 */

function JavaFileDriver(directories) {
    if (Array.isArray(directories)) {
        this.directories = directories;
    } else {
        this.directories = [ directories ];
    }
}

JavaFileDriver.prototype.asyncReadContent = function (filename, success, error) {
    try {
        success(this.readContent(filename));
    } catch (e) {
        error(e);
    }
};

JavaFileDriver.prototype.readContent = function (filename) {
    for(var i = 0; i < this.directories.length; i++) {
        var file = new java.io.File(this.directories[i] + "/" + filename);
        if (file.isFile()) {
            var result = "";
            var reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(file)));

            try {
                var line = reader.readLine();
                while (line != null) {
                    result += line + "\n";
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }

            return result;
        }
    }

    throw new Error("File " + filename + " not found");
};

