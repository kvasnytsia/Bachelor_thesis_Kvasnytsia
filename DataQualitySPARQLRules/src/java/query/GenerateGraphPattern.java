/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package query;

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author Lea
 * the whole function was written by me.
 */
public class GenerateGraphPattern {

    public static String generateNodesEdges(String graph_model) {

        String[] triplesss = (graph_model).split("] \\[");
        String[][] alltriples = new String[triplesss.length][6];

        for (int i = 0; i < triplesss.length; i++) {
            String[] triple = (triplesss[i].split(", ", 3));
            alltriples[i][0] = triple[0];
            alltriples[i][1] = triple[1];
            alltriples[i][2] = triple[2];
            alltriples[i][3] = String.valueOf(i);
            alltriples[i][4] = "nodeID";
            alltriples[i][5] = "source";
            //content alltriples = subject, praedikat, object, id, nodeid, type(source,target)
        }
        //swapping subject and object to sort and compare nodes, to see double occurences
        String[][] alltriplesback = new String[alltriples.length][6];
        for (int i = 0; i < alltriples.length; i++) {
            alltriplesback[i][0] = alltriples[i][2];
            alltriplesback[i][2] = alltriples[i][0];
            alltriplesback[i][5] = "target";
            alltriplesback[i][1] = alltriples[i][1];
            alltriplesback[i][3] = alltriples[i][3];
            alltriplesback[i][4] = alltriples[i][4];
        }
        //merge both arrays to sort and count nodes
        int aLen = alltriples.length;
        int bLen = alltriplesback.length;
        String[][] triplecombo = new String[aLen + bLen][6];
        System.arraycopy(alltriples, 0, triplecombo, 0, aLen);
        System.arraycopy(alltriplesback, 0, triplecombo, aLen, bLen);
        triplecombo = sort(triplecombo, 0);
        int nodecount = 0;
        for (int i = 0; i < triplecombo.length; i++) {
            if (i + 1 < triplecombo.length) {
                while ((triplecombo[i][0].equals(triplecombo[i + 1][0]))) {
                    i = i + 1;
                }
            }
            nodecount = nodecount + 1;
        }
        //generate node array (nodeID, Name, Size)
        String[][] nodes = new String[nodecount][5];
        int nodeid = 0;
        for (int i = 0; i < triplecombo.length; i++) {
            triplecombo[i][4] = "n" + Integer.toString(nodeid);
            int j = 1;
            if (i + 1 < triplecombo.length) {
                while (triplecombo[i][0].equals(triplecombo[i + 1][0]) && i + 1 < triplecombo.length) {
                    triplecombo[i + 1][4] = "n" + Integer.toString(nodeid);
                    j = j + 1;
                    i = i + 1;
                }
            }
            nodes[nodeid][0] = "n" + Integer.toString(nodeid);
            nodes[nodeid][1] = triplecombo[i][0];
            nodes[nodeid][2] = Integer.toString(j);
            nodeid = nodeid + 1;
        }
        //change Subject and ID to sort for IDs and generate the Edges by scanning once only
        String[][] sortedtriplecombo = new String[triplecombo.length][6];
        for (int i = 0; i < triplecombo.length; i++) {
            sortedtriplecombo[i][0] = triplecombo[i][3];
            sortedtriplecombo[i][1] = triplecombo[i][1];
            sortedtriplecombo[i][2] = triplecombo[i][2];
            sortedtriplecombo[i][3] = triplecombo[i][0];
            sortedtriplecombo[i][4] = triplecombo[i][4];
            sortedtriplecombo[i][5] = triplecombo[i][5];
        }
        sortedtriplecombo = sort(sortedtriplecombo, 0);
        String[][] edges = new String[triplesss.length][4];
        int idedge = 0;
        //generating Edges (EdgeID, EdgeLabel, source, target)
        for (int i = 0; i < sortedtriplecombo.length; i = i + 2) {
            edges[idedge][0] = "e" + Integer.toString(idedge);
            edges[idedge][1] = sortedtriplecombo[i][1];

            if (sortedtriplecombo[i][5].equals("source")) {
                edges[idedge][2] = sortedtriplecombo[i][4];
                edges[idedge][3] = sortedtriplecombo[i + 1][4];
            } else {
                edges[idedge][2] = sortedtriplecombo[i + 1][4];
                edges[idedge][3] = sortedtriplecombo[i][4];
            }
            idedge = idedge + 1;
        }
        //nodes and edges are stored in 2 arrays, convertable to json to unse in sigmajs
        String [][][] resultarray = new String [2][Math.max(nodes.length, edges.length)][6];
        resultarray[0] = nodes;
        resultarray[1] = edges;
        //nodes are formed to match sigma format. Nodes are plottet on random places, a neighborhood solution would be more fitting.
        String resultstring = "s.graph";
        for (int i = 0; i < resultarray[0].length; i = i + 1) {
                    String id = resultarray[0][i][0];
                    String label = resultarray[0][i][1];
                    String size = resultarray[0][i][2];
                    Float x = (float)(Math.random() * (Math.sqrt(resultarray[0].length)*400));
                    Float y = (float)(Math.random() * (Math.sqrt(resultarray[0].length)*400));
                    Integer colour = (int)(Math.random() * 100000);
                    resultstring = resultstring +
                    ".addNode({ "+
                    "id: \'"+ id+ "\', "+
                    "label: \'"+label+"\', "+
                    "x: "+ x+", "+
                    "y: "+y+", "+
                    "size: "+size+", "+
                    "color:"+ "'#" +colour+"' "+
                    "})";
        }
        //edges are formed to fit the sigma format
        for (int i = 0; i < resultarray[1].length; i = i + 1){
            String id = resultarray[1][i][0];
            String label = resultarray[1][i][1];
            String source = resultarray[1][i][2];
            String target = resultarray[1][i][3];
            resultstring = resultstring +
            ".addEdge({ "+
            "id: \'" + id+"\', "+
            "label: \'"+label+"\', "+
            "source: \'"+source+"\', "+
            "target: \'"+target+"\' "+
            "})";
        }
        resultstring = resultstring +";";
        return (resultstring);

    }
    

    //taken from http://stackoverflow.com/questions/9288464/alphabetizing-a-2d-array-in-java answer from anzaan
    public static String[][] sort(String[][] array, final int sortIndex) {
        if (array.length < 2) {
            return array;
        }

        Arrays.sort(array, new Comparator<String[]>() {

            public int compare(String[] o1, String[] o2) {
                return o1[sortIndex].compareToIgnoreCase(o2[sortIndex]);
            }
        });
        return array;
    }

    public static void main(String[] args) {
        GenerateGraphPattern start = new GenerateGraphPattern();
    }

}


