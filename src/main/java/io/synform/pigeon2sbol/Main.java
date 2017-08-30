
package io.synform.pigeon2sbol;

import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLValidationException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws IOException, SBOLConversionException, SBOLValidationException, URISyntaxException {

        FileInputStream is = new FileInputStream("./pigeon_examples/2.pigeon");

        Pigeon2SBOL.pigeon2SBOL(is).write(new FileOutputStream("out.xml"));


    }
}
