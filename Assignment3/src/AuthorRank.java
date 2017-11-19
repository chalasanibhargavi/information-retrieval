import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class AuthorRank {

    public static void main(String[] args) throws IOException {

        String fileName = "D:\\Grad Notes\\Search\\Assignments\\A3\\author.net";

        DirectedSparseGraph<String, String> graph = new DirectedSparseGraph<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

        HashMap<String, String> vertexMap = new HashMap<String, String>();

        //Get Vertices
        String vertexInfo = br.readLine();
        int numVertices = Integer.parseInt(vertexInfo.split("\\s+")[1]);

        //Read input till the count of vertices
        for (int i = 0; i < numVertices; i++) {
            String s = br.readLine();
            String[] splitV = s.split("\\s+");
            String id = splitV[0];
            String label = splitV[1].substring(1, splitV[1].length() - 1);
            vertexMap.put(id, label);
            graph.addVertex(label);
        }

        //Get Edges
        String edgeInfo = br.readLine();
        int numEdges = Integer.parseInt(edgeInfo.split("\\s+")[1]);

        //Read input till the count of edges
        for (int i = 0; i < numEdges; i++) {
            String s = br.readLine();
            String[] splitE = s.split("\\s+");
            //Get edge connections
            String con1 = splitE[0];
            String con2 = splitE[1];

            Pair<String> p = new Pair<String>(vertexMap.get(con1), vertexMap.get(con2));
            graph.addEdge(Integer.toString(i), p, EdgeType.DIRECTED);

        }

        double dampingFactor = 0.85;
        double alpha = 1 - dampingFactor;
        PageRank<String, String> ranker = new PageRank<String, String>(graph, alpha);
        ranker.evaluate();

        HashMap<String, Double> rankScores = new HashMap<String, Double>();
        for (String v : graph.getVertices()) {
            rankScores.put(v, ranker.getVertexScore(v));
        }

        //Sort the results by score
        List<Map.Entry> entryList = new ArrayList<Map.Entry>(sortMap(rankScores).entrySet());

        System.out.println("Top 10 Authors: ");
        System.out.println("Author ID\tPage Rank Score");

        for (Map.Entry e : entryList.subList(0, 10)) {
            System.out.println(e.getKey() + "\t\t" + e.getValue());
        }
    }

    public static <K, V extends Comparable<? super V>> Map<K,V> sortMap(Map<K, V> unsortedMap) {
        return unsortedMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}



