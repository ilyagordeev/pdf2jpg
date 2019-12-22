package com.github.ilyagordeev.pdf2jpg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class FileFinder extends TimerTask {
    private final int resolution;
    private final Path pathPDF;
    private static boolean busy = false;

    public boolean refresh() throws IOException {
        if (busy) return false;
        checkPath(pathPDF);
        return true;
    }

    public FileFinder(Path pathPDF, int resolution) {
        this.resolution = resolution;
        this.pathPDF = pathPDF;
    }

    private void checkPath(Path pathPDF) throws IOException {
        busy = true;
        // Создаем список всех PDF-файлов в выбранной директории
        List<String> filesPDF = Files.walk(pathPDF)
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .filter(f -> f.endsWith(".pdf"))
                .collect(Collectors.toList());

        // Создаем список непустых папок в выбранной директории
        List<String> dirJPG = Files.walk(pathPDF)
                .filter(Files::isDirectory)
                .filter(f -> {
                    try {
                        return Files.list(f).count() > 0;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                })
                .filter(d -> !d.equals(pathPDF))
                .map(x -> x.toString() + ".pdf")
                .collect(Collectors.toList());

        // Удаляем из списка файлов те файлы, имена которых совпадают с именами непустых директорий
        filesPDF.removeAll(dirJPG);

        if (filesPDF.isEmpty()) {
            System.out.println(new Date() + " --- No new PDF files.");
            busy = false;
            return;
        }

        // Создаём директорию и запускаем переконвертацию
        filesPDF.forEach(f -> {
            try {
                String first = f.substring(0, f.length() - 4);
                if (!Files.exists(Paths.get(first)))
                    Files.createDirectory(Paths.get(first));
                Runnable generator = new Converter(new File(f), resolution);
                Thread thread = new Thread(generator);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        busy = false;
    }

    @Override
    public void run() {
        try {
            refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
