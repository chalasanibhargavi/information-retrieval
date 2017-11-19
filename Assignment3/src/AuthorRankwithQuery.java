import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.MapTransformer;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;



public class AuthorRankwithQuery {

    public static String fileName = "D:\\Grad Notes\\Search\\Assignments\\A3\\author.net";
    public static String indexPath = "D:\\Grad Notes\\Search\\Assignments\\A3\\author_index";

    public static void main(String[] args) throws IOException, ParseException {

        List<String> queryList = Arrays.asList("Data Mining", "Information Retrieval");

        for( String qString : queryList){

            //Calculate the prior probabilities
            HashMap<String, Double> priorMap = calculatePrior(qString);

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

                //Assign prior 0.0 to non-seed nodes
                if(!priorMap.containsKey(label))
                {
                    priorMap.put(label, (double) 0.0);
                }
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

            Transformer<String, Double> priorMapTransformer = MapTransformer.getInstance(priorMap);
            PageRankWithPriors<String, String> ranker = new PageRankWithPriors<String, String>(graph, priorMapTransformer, alpha);
            ranker.evaluate();

            Map<String, Double> rankScores = new HashMap<String, Double>();
            for (String v : graph.getVertices()) {
                rankScores.put(v, ranker.getVertexScore(v));
            }

            //Sort the results by score
            List<Map.Entry> entryList = new ArrayList<Map.Entry>(sortMap(rankScores).entrySet());

            System.out.println("\nTop 10 Authors for query \""+qString+"\" : ");
            System.out.println("Author ID\tPage Rank Score");
            for (Map.Entry e : entryList.subList(0, 10)) {
                System.out.println(e.getKey() + "\t\t" + e.getValue());
            }
        }
    }

    public static HashMap<String, Double> calculatePrior(String queryString) throws IOException, ParseException {

        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        searcher.setSimilarity(new BM25Similarity());

        QueryParser parser = new QueryParser("content", analyzer);
        Query query = parser.parse(queryString);

        //Set top 300 docs as seed nodes
        TopDocs results = searcher.search(query, 300);
        ScoreDoc[] hits = results.scoreDocs;

        HashMap<String, Double> authorMap = new HashMap<String, Double>();

        double priorTotal = 0.0;

        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            priorTotal += hits[i].score;

            if (authorMap.containsKey(doc.get("authorid"))) {

                double preVal = authorMap.get(doc.get("authorid"));
                double sum = hits[i].score + preVal;
                authorMap.put(doc.get("authorid"), sum);
            } else {
                authorMap.put(doc.get("authorid"), (double) hits[i].score);
            }
        }

        HashMap<String, Double> normalizedPriors = new HashMap<String, Double>();
        List<Map.Entry> priorList = new ArrayList<Map.Entry>(authorMap.entrySet());

        for (Map.Entry e : priorList) {

            double priorVal = (double) e.getValue();
            priorVal /= priorTotal;
            normalizedPriors.put((String) e.getKey(), priorVal);
        }
        reader.close();

        return normalizedPriors;
    }

    public static <K, V extends Comparable<? super V>> Map<K,V> sortMap(Map<K, V> unsortedMap) {
        return unsortedMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
