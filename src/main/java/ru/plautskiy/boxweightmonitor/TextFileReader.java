/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.plautskiy.boxweightmonitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plautskii
 */
public class TextFileReader {
    public static List<String> read(File file) {
        List<String> lines = new ArrayList<>();
        String line;
        try {
            try (BufferedReader br = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TextFileReader.class.getName()).log(Level.SEVERE, null, ex);
            return Collections.emptyList();
        }

        return lines;
    }

    public static List<String> readFromCurrentCAS() {
        return read(findLastCASFile());
    }

    public static File findLastCASFile() {
        File casfile = new File("");
        Path dir = Paths.get(BoxWeightMonitor.appProperties.getProperty("raw-path"));

        Optional<Path> lastFilePath = Optional.empty();
        try {
            lastFilePath = Files.list(dir)
                    .filter(f -> !Files.isDirectory(f) && f.toFile().toString().endsWith(".txt"))
                    .max(Comparator.comparingLong(f -> f.toFile().lastModified()));
        } catch (IOException ex) {
            System.out.println("Can't find last modified cas file. Exit");
            Logger.getLogger(TextFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (lastFilePath.isPresent()) {
            casfile = lastFilePath.get().toFile();
        } else {
            System.out.println("Can't find last modified cas file. Exit");
            System.exit(0);
        }
        return casfile;
    }

    public static void write(List<String> list, String file) {
        OpenOption[] options = new OpenOption[]{StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
        Path path = Paths.get(file);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException ex) {
                Logger.getLogger(TextFileReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try (final BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"), options)) {
            for (String ch : list) {
                writer.write(ch);
                writer.write(System.lineSeparator());
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(TextFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void update(List<String> entries, String file, boolean isAppend) {
        OpenOption[] options;
        if (isAppend) {
            options = new OpenOption[]{StandardOpenOption.APPEND, StandardOpenOption.WRITE};
        } else {
            options = new OpenOption[]{StandardOpenOption.WRITE};
        }

        Path path = Paths.get(file);
        try (final BufferedWriter writer = Files.newBufferedWriter(path, Charset.forName("UTF-8"), options)) {
            for (String str : entries) {
                writer.write(str);
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(TextFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
