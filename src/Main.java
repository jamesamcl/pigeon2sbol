import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, SBOLConversionException, SBOLValidationException {

        FileInputStream is = new FileInputStream("./pigeon_examples/1.pigeon");

        Pigeon2SBOL.pigeon2SBOL(is).write(System.out);


    }
}
