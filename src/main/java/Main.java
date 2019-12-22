import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Paths;

@CommandLine.Command(
        name="pdf2jpg",
        description="Auto converter from PDF to JPG",
        mixinStandardHelpOptions = true )
public class Main {

    @CommandLine.Parameters(index = "0", description = "The path of PDF files")
    private String path;

    @CommandLine.Option(names = {"-r", "--resolution"}, description="JPG resolution, 150 dpi by default")
    private int resolution = 150;

    @CommandLine.Option(names = {"-t", "--timer"}, description="Timer in minutes, 0 for run once, 5 by default")
    private int timer = 5;

    public static void main(String[] args) {
        final Main main = CommandLine.populateCommand(new Main(), args);
        try {
            new Converter(Paths.get(main.path), main.resolution);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
