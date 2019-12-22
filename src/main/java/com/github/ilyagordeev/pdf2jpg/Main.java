package com.github.ilyagordeev.pdf2jpg;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Timer;

@Command(
        name="pdf2jpg",
        description="Auto converter from PDF to JPG",
        version = "1.0")
public class Main {

    @Parameters(index = "0", arity = "1", description = "The path of PDF files")
    private String path;

    @Option(names = {"-r", "--resolution"}, defaultValue="150", description="JPG resolution, 150 dpi by default")
    private int resolution = 150;

    @Option(names = {"-t", "--timer"}, defaultValue="5", description="Timer in minutes, 0 for run once, 5 by default")
    private int timer = 5;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "java -jar pdf2jpg.jar <path> -t <minutes> -r <dpi>")
    private boolean helpRequested = false;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Main());
        commandLine.parseArgs(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }

        Logger logger = Logger.getLogger("org.apache");
        logger.setLevel(Level.FATAL);

        final Main main = CommandLine.populateCommand(new Main(), args);
        final Timer timer = new Timer();
        timer.schedule(new Converter(Paths.get(main.path), main.resolution), 0, main.timer*60000);
    }
}
