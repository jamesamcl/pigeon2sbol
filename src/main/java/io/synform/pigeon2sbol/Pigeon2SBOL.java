
package io.synform.pigeon2sbol;

import org.sbolstandard.core2.*;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by james on 29/08/2017.
 */
public class Pigeon2SBOL
{
    static SBOLDocument pigeon2SBOL(InputStream is) throws IOException, SBOLValidationException {
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

        HashMap<String, OrientationType> orientations = new HashMap<String, OrientationType>();

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

            Component subC = rootCD.createComponent(subCD.getDisplayId() + "_component", AccessType.PUBLIC, subCD.getDisplayId());

            subComponentsInOrder.add(subC);

            orientations.put(subC.getIdentity().toString(), orientation);

            if(type.equals("c")) {
                subCD.addRole(SequenceOntology.CDS);
            } else if(type.equals("p")) {
                subCD.addRole(SequenceOntology.PROMOTER);
            }

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
            OrientationType curOrientation = orientations.get(cur.getIdentity().toString());

            Component next = subComponentsInOrder.get(i + 1);
            OrientationType nextOrientation = orientations.get(next.getIdentity().toString());

            rootCD.createSequenceConstraint(getIdentifier(identifiersUsed, "p2s_order_constraint"), RestrictionType.PRECEDES, cur.getDisplayId(), next.getDisplayId());

            if(curOrientation != nextOrientation) {
                rootCD.createSequenceConstraint(getIdentifier(identifiersUsed, "p2s_orientation_constraint"), RestrictionType.OPPOSITE_ORIENTATION_AS, cur.getDisplayId(), next.getDisplayId());
            }

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
