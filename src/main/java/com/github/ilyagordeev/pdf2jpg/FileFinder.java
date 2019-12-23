package com.github.ilyagordeev.pdf2jpg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FileFinder extends TimerTask {
    private final int resolution;
    private final Path pathPDF;
    private static boolean busy = false;

    public boolean refresh() throws IOException, InterruptedException {
        if (busy) return false;
        checkPath(pathPDF);
        return true;
    }

    public FileFinder(Path pathPDF, int resolution) {
        this.resolution = resolution;
        this.pathPDF = pathPDF;
    }

    private void checkPath(Path pathPDF) throws IOException, InterruptedException {
        busy = true;
        // Создаем список всех PDF-файлов в выбранной директории
        List<String> filesPDF = Files.walk(pathPDF)
                .filter(Files::isRegularFile)
                .map(Path::toString)
                .filter(f -> f.endsWith(".pdf") || f.endsWith(".PDF"))
                .map(s -> s.substring(0, s.length() - 4))
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
                .map(Path::toString)
                .collect(Collectors.toList());

        // Удаляем из списка файлов те файлы, имена которых совпадают с именами непустых директорий
        filesPDF.removeAll(dirJPG);

        if (filesPDF.isEmpty()) {
            System.out.println(new Date() + " --- No new PDF files.");
            busy = false;
            return;
        }

        // Создаём директорию и запускаем переконвертацию
        ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        filesPDF.forEach(f -> {
            try {
                if (!Files.exists(Paths.get(f)))
                    Files.createDirectory(Paths.get(f));
            } catch (IOException e) {
                e.printStackTrace();
            }
            service.submit(new Converter(new File(f + ".pdf"), resolution));
        });
        service.shutdown();
        service.awaitTermination(5, TimeUnit.MINUTES);
        busy = false;
    }

    @Override
    public void run() {
        try {
            refresh();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
