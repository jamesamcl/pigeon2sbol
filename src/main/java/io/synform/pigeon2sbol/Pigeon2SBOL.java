
package io.synform.pigeon2sbol;

import org.sbolstandard.core2.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by james on 29/08/2017.
 */
public class Pigeon2SBOL
{
    static SBOLDocument pigeon2SBOL(InputStream is) throws IOException, SBOLValidationException, URISyntaxException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        SBOLDocument doc = new SBOLDocument();

        doc.setDefaultURIprefix("http://api.synform.io/temp/pigeon2sbol/");

        ComponentDefinition rootCD = doc.createComponentDefinition("rootCD", ComponentDefinition.DNA);


        ArrayList<String> colors = new ArrayList<String>();
        colors.add("#a6cee3");
        colors.add("#1f78b4");
        colors.add("#b2df8a");
        colors.add("#33a02c");
        colors.add("#fb9a99");
        colors.add("#e31a1c");
        colors.add("#fdbf6f");
        colors.add("#ff7f00");
        colors.add("#cab2d6");
        colors.add("#6a3d9a");
        colors.add("#ffff99");
        colors.add("#eced6f");
        colors.add("#888888");
        colors.add("#000000");


        ArrayList<Component> subComponentsInOrder = new ArrayList<Component>();


        HashSet<String> identifiersUsed = new HashSet<String>();
        HashSet<String> identifiersReferenced = new HashSet<String>();


        boolean parsingArcs = false;
        int readingColors = 0;


        String line;

        while((line = reader.readLine()) != null) {

            line = line.trim();

            if(line.length() == 0)
                continue;

            if(line.equalsIgnoreCase("rgbcolors")) {
                readingColors = colors.size();
                continue;
            }

            if(readingColors != 0) {
                colors.set(colors.size() - (readingColors --), line);
                continue;
            }

            if(line.equalsIgnoreCase("# Arcs")) {
                parsingArcs = true;
                continue;
            }

            String[] tokens = line.split("[ \t]+");

            if(parsingArcs) {

                if (tokens.length != 3) {
                    throw new Error("Expected exactly 3 tokens for arc line");
                }

                continue;
            }



            String type = tokens[0];

            OrientationType orientation = OrientationType.INLINE;

            if(type.charAt(0) == '<') {
                orientation = OrientationType.REVERSECOMPLEMENT;
                type = type.substring(1);
            } else if(type.charAt(0) == '>') {
                type = type.substring(1);
            }

            String id;
            String name;

            if(tokens.length >= 2) {
                name = tokens[1];
                id = getIdentifier(identifiersUsed, name);
            } else {
                id = getIdentifier(identifiersUsed, type);
                name = id;
            }

            ComponentDefinition subCD = doc.createComponentDefinition(id, ComponentDefinition.DNA);
            subCD.setName(name);

            Component subC = rootCD.createComponent(subCD.getDisplayId() + "_component", AccessType.PUBLIC, subCD.getDisplayId());
            subComponentsInOrder.add(subC);

            if(type.equals("c") || type.equals("g") || type.equals("g'")) {
                subCD.addRole(SequenceOntology.CDS);
            } else if(type.equals("p") || type.equals("P")) {
                subCD.addRole(SequenceOntology.PROMOTER);
            } else if(type.equals("t") || type.equals("T")) {
                subCD.addRole(SequenceOntology.TERMINATOR);
            } else if(type.equals("x")) {
                subCD.addRole(SequenceOntology.RESTRICTION_ENZYME_RECOGNITION_SITE);
            } else if(type.equals("z")) {
                subCD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
            } else if(type.equals("=")) {
                subCD.addRole(URI.create("http://identifiers.org/so/SO:0001953"));
            } else if(type.equals("i")) {
                subCD.addRole(URI.create("http://identifiers.org/so/SO:0001979"));
            } else if(type.equals("<") || type.equals(">")) {
                subCD.addRole(URI.create("http://identifiers.org/so/SO:0000342"));
            } else if(type.equals("o")) {
                subCD.addRole(URI.create("http://identifiers.org/so/SO:0001695"));
            } else if(type.equals("v")) {
                subCD.addRole(URI.create("http://identifiers.org/so/SO:0000988"));
            } else if(type.equals("r")) {
                subCD.addRole(SequenceOntology.RIBOSOME_ENTRY_SITE);
            }

            SequenceAnnotation sa = rootCD.createSequenceAnnotation(subC.getDisplayId() + "_sa", "location", orientation);
            sa.setComponent(subC.getIdentity());

            if(tokens.length >= 4) {

                if(!tokens[3].equalsIgnoreCase("nl")) {
                    throw new Error("Unknown token; only \"nl\" is valid here");
                }

                //glyph.label = false;
            }

            if(tokens.length >= 5) {
                throw new Error("Too many tokens in line");
            }

        }

        for(int i = subComponentsInOrder.size() - 2; i >= 0; -- i) {

            Component cur = subComponentsInOrder.get(i);
            Component next = subComponentsInOrder.get(i + 1);

            rootCD.createSequenceConstraint(getIdentifier(identifiersUsed, "p2s_order_constraint"), RestrictionType.PRECEDES, cur.getDisplayId(), next.getDisplayId());

        }

        return doc;

    }

    static String getIdentifier(Set<String> identifiersUsed, String name) {

        String newID = name;
        int n = 1;

        while(identifiersUsed.contains(newID))
            newID = name + (n ++);

        identifiersUsed.add(newID);

        return newID;
    }


}
