package packrle;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArchiverLauncher {

    @Option(name = "-z", forbids = "-u", usage = "are we packing?")
    private boolean packing;

    @Option(name = "-u", forbids = "-z", usage = "are we unpacking?")
    private boolean unpacking;

    @Option(name = "-out")
    private boolean isOut;

    @Argument
    private List<String> arguments = new ArrayList<>();


    public static void main(String[] args) {
        new ArchiverLauncher().launch(args);
    }

    private void launch(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            if (arguments.isEmpty() || !unpacking && !packing) throw new CmdLineException(parser, new Exception());
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar packrle.jar [-z|-u] [-out outputname.txt] inputname.txt");
            parser.printUsage(System.err);
            return;
        }

        Archiver archiver = new Archiver();
        try {
            String output;
            String input;
            if (isOut) {
                output = arguments.get(0);
                input = arguments.get(1);
            } else {
                input = arguments.get(0);
                output = input + ".rle";
            }
            if (unpacking) {
                archiver.unpack(input, output);
            } else {
                archiver.pack(input, output);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
