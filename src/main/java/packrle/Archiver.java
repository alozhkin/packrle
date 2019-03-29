package packrle;

import java.io.*;
import java.util.*;


public class Archiver {

    //one half of the char = 2^16 - 1 / 2. Number of identical characters in 1..32767, different in 32768..65534
    public static final int LENGTH_OF_SEQUENCE = 32767;

    //uses RLE, packs sequences of identical and different characters by writing it's size
    //aaaabfghyt -> 4a6bfghyt
    public void pack(String inputFileName, String outputFileName) throws IOException {
        try (FileReader rd = new FileReader(inputFileName)) {
            File file = new File(outputFileName);
            try (FileWriter wr = new FileWriter(file)) {
                //ignore sequences of two identical characters in porpoise of packing bad input
                int sym1 = rd.read();
                int sym2 = rd.read();
                int sym3 = rd.read();
                //collecting our sequence
                ArrayList<Integer> sequence = new ArrayList<>();
                sequence.add(sym1);
                //don't do anything with blank file
                if (sym1 != -1) {
                    if (sym2 == -1) {
                        wr.write(Archiver.LENGTH_OF_SEQUENCE + 1);
                        wr.write(sym1);
                    } else if (sym3 == -1) {
                        wr.write(Archiver.LENGTH_OF_SEQUENCE + 2);
                        wr.write(sym1);
                        wr.write(sym2);
                    } else {
                        boolean isEqual = sym1 == sym2 && sym1 == sym3;
                        while (sym3 != -1) {
                            sym1 = sym2;
                            sym2 = sym3;
                            sym3 = rd.read();
                            //if sequence of identical or different characters ends or sequence size is too big,
                            //write it's size and itself
                            //redefine syms
                            if (isEqual && (sym2 != sym3 || sequence.size() == LENGTH_OF_SEQUENCE - 2)) {
                                //identical in 1..32767
                                wr.write(sequence.size() + 2);
                                wr.write(sequence.get(0));
                                sequence.clear();
                                sym1 = sym3;
                                sym2 = rd.read();
                                sym3 = rd.read();
                                isEqual = sym1 == sym2 && sym1 == sym3;
                            } else if (!isEqual && (sym1 == sym2 && sym1 == sym3
                                    || sequence.size() == LENGTH_OF_SEQUENCE)) {
                                //different in 32768..65534
                                wr.write(LENGTH_OF_SEQUENCE + sequence.size());
                                for (int el : sequence) {
                                    wr.write(el);
                                }
                                sequence.clear();
                                isEqual = sym1 == sym2 && sym1 == sym3;
                            }
                            sequence.add(sym1);
                        }
                        if (sym2 != -1) {
                            wr.write(sequence.size() + LENGTH_OF_SEQUENCE + 1);
                            for (int el : sequence) {
                                wr.write(el);
                            }
                            wr.write(sym2);
                        } else if (sym1 != -1) {
                            wr.write(sequence.size() + LENGTH_OF_SEQUENCE);
                            for (int el : sequence) {
                                wr.write(el);
                            }
                        }
                    }
                }
            }
        }
    }

    public void unpack(String inputFileName, String outputFileName) throws IOException {
        try (FileReader rd = new FileReader(inputFileName)) {
            File file = new File(outputFileName);
            try (FileWriter wr = new FileWriter(file)) {
                int num = rd.read();
                while (num != -1) {
                    if (num > LENGTH_OF_SEQUENCE) {
                        num = num - LENGTH_OF_SEQUENCE;
                        for (int i = 0; i < num; i++) {
                            int temp = rd.read();
                            wr.write(temp);
                        }
                    } else {
                        int temp = rd.read();
                        for (int i = 0; i < num; i++) {
                            wr.write(temp);
                        }
                    }
                    num = rd.read();
                }
            }
        }
    }
}
