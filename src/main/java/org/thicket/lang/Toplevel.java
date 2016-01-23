/*
 * Thicket
 * https://github.com/d-plaindoux/thicket
 *
 * Copyright (c) 2015-2016 Didier Plaindoux
 * Licensed under the LGPL2 license.
 */

package org.thicket.lang;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.Console;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Toplevel {

    public static void main(String[] args) throws IOException, ScriptException, NoSuchMethodException {
        final ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("JavaScript");
        final List<String> strings = Arrays.asList(args).stream().collect(Collectors.toList());

        loadResource(nashorn, "/console.js");
        loadResource(nashorn, "/reader.js");
        loadToplevel(nashorn, strings.remove(0));

        final Object toplevel = createTopLevel(nashorn, strings.remove(0));
        final Invocable engine = (Invocable) nashorn;

        final Console console = System.console();
        console.printf("Thicket v0.1\n");

        strings.forEach(p -> {
            try {
                engine.invokeMethod(toplevel, "loadPackage", p);
            } catch (ScriptException | NoSuchMethodException e) {
                System.out.println(e.getMessage());
            }
        });

        engine.invokeMethod(toplevel,"loadSpecifications","Boot.Core");

        //noinspection InfiniteLoopStatement
        while (true) {
            String line = readCodeToExecute(console);
            engine.invokeMethod(toplevel, "manageSourceCode", line);
        }
    }

    //
    // Private behaviors
    //

    private static Object loadToplevel(ScriptEngine nashorn, String arg) throws ScriptException, FileNotFoundException {
        return nashorn.eval(new FileReader(arg));
    }

    private static Object createTopLevel(ScriptEngine nashorn, String arg) throws ScriptException {
        final String toplevelCode = String.format("require('thicket')(new JavaFileDriver(['%s']),false)", arg);
        return nashorn.eval(toplevelCode);
    }

    private static void loadResource(ScriptEngine nashorn, String name) throws IOException, ScriptException {
        try (InputStream inputStream = Toplevel.class.getResourceAsStream(name)) {
            nashorn.eval(new InputStreamReader(inputStream));
        }
    }

    private static String readCodeToExecute(Console console) {
        String line = console.readLine("> ");

        while (!line.endsWith(";;")) {
            line = line + console.readLine();
        }

        return line.substring(0, line.length() - 2);
    }

}
