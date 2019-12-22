package com.github.ilyagordeev.pdf2jpg;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Converter implements Runnable{
    public Converter(File file, int resolution) {
        this.file = file;
        this.resolution = resolution;
    }

    private File file;
    private int resolution;

    @Override
    public void run() {
        try {
            generateImageFromPDF(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateImageFromPDF(File file) throws IOException {

        PDDocument document = PDDocument.load(file);
        if (document != null) {
            System.out.printf("\nFile \"%s\" loaded with %d pages\n", file.getName(), document.getNumberOfPages());
        }
        else {
            return;
        }

        PDFRenderer pdfRenderer = new PDFRenderer(document);

        ImageIO.scanForPlugins();

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            System.out.print("#");

            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(
                    page, 150, ImageType.RGB);
            ImageIOUtil.writeImage(
                    bufferedImage, String.format(file.getCanonicalPath()
                            .substring(0, file.getCanonicalPath().length() - 4) + "\\" +
                            file.getName().substring(0, file.getName().length() - 4) +
                            "_%d%s", page + 1, ".jpg"), resolution);
        }
        System.out.printf(" -> %s converted", file.getName());
        document.close();
    }
}
