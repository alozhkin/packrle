
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import packrle.Archiver;
import java.io.*;


public class ArchiverTest {
    private File inputFile;
    private File outputFile;
    private File unpackFile;

    interface InnerFun {
        void test(String inputStr, String shouldBe) throws IOException;
    }

    @Before
    public void initialize() {
        try {
            inputFile = File.createTempFile("ArchiverInputTempFile", null);
            outputFile = File.createTempFile("ArchiverOutputTempFile", null);
            unpackFile = File.createTempFile("ArchiverUnpackTempFile", null);
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }

    @After
    public void delete() {
        if (!inputFile.delete()) {
            System.out.println("Failed to delete the file tempInputFile.txt");
        }
        if (!outputFile.delete()) {
            System.out.println("Failed to delete the file tempOutputFile.txt");
        }
        if (!unpackFile.delete()) {
            System.out.println("Failed to delete the file tempUnpackFile.txt");
        }
    }

    @Test
    public void ArchiverTest() throws IOException {
        InnerFun fun = new InnerFun() {
            @Override
            public void test(String inputStr, String shouldBe) throws IOException {
                ClassLoader cl = getClass().getClassLoader();
                try (FileReader rdOutput = new FileReader(outputFile)) {
                    try (FileWriter wrInput = new FileWriter(inputFile)) {
                        try (FileReader rdUnpack = new FileReader(unpackFile)) {
                            wrInput.write(inputStr);
                            wrInput.close();
                            Archiver arc = new Archiver();
                            arc.pack(inputFile.getCanonicalPath(), outputFile.getCanonicalPath());
                            StringBuilder outputStr = new StringBuilder();
                            int sym = rdOutput.read();
                            while (sym != -1) {
                                outputStr.append((char) sym);
                                sym = rdOutput.read();
                            }
                            Assert.assertEquals(shouldBe, outputStr.toString());
                            arc.unpack(outputFile.getCanonicalPath(), unpackFile.getCanonicalPath());
                            StringBuilder unpackStr = new StringBuilder();
                            int unSym = rdUnpack.read();
                            while (unSym != -1) {
                                unpackStr.append((char) unSym);
                                unSym = rdUnpack.read();
                            }
                            Assert.assertEquals(inputStr, unpackStr.toString());
                        }
                    }
                }
            }
        };

        String inputStr = "aaaabbbb\n";
        String shouldBe = "\u0004a\u0004b\u8000\n";
        fun.test(inputStr, shouldBe);

        inputStr = "aaaabbbb";
        shouldBe = "\u0004a\u0004b";
        fun.test(inputStr, shouldBe);

        inputStr = "aassddff";
        char a = (char) Archiver.LENGTH_OF_SEQUENCE + 8;
        shouldBe = a + "aassddff";
        fun.test(inputStr, shouldBe);

        inputStr = "aassddfffft";
        a = (char) Archiver.LENGTH_OF_SEQUENCE + 6;
        char b = (char) Archiver.LENGTH_OF_SEQUENCE + 1;
        shouldBe = a + "aassdd" + "\u0004" + "f" + b + "t";
        fun.test(inputStr, shouldBe);

        inputStr = "aassddffffty";
        a = (char) Archiver.LENGTH_OF_SEQUENCE + 6;
        b = (char) Archiver.LENGTH_OF_SEQUENCE + 2;
        shouldBe = a + "aassdd" + "\u0004" + "f" + b + "ty";
        fun.test(inputStr, shouldBe);

        inputStr = "";
        shouldBe = "";
        fun.test(inputStr, shouldBe);

        inputStr = "a";
        a = (char) Archiver.LENGTH_OF_SEQUENCE + 1;
        shouldBe = a + "a";
        fun.test(inputStr, shouldBe);

        inputStr = "af";
        a = (char) Archiver.LENGTH_OF_SEQUENCE + 2;
        shouldBe = a + "af";
        fun.test(inputStr, shouldBe);

        inputStr = "afh";
        a = (char) Archiver.LENGTH_OF_SEQUENCE + 3;
        shouldBe = a + "afh";
        fun.test(inputStr, shouldBe);

        inputStr = "aaa";
        shouldBe = "\u0003" + "a";
        fun.test(inputStr, shouldBe);

        inputStr = "\uD83D\uDE80\uD83D\uDE80\uD83D\uDE80";
        a = (char) Archiver.LENGTH_OF_SEQUENCE + 6;
        shouldBe = a + "\uD83D\uDE80\uD83D\uDE80\uD83D\uDE80";
        fun.test(inputStr, shouldBe);
    }
}

