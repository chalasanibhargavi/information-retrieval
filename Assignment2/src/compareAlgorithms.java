import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;

public class compareAlgorithms {
	
	public static final String mainPath = "D:\\Grad Notes\\Search\\Assignments\\A2";
	public static final String filePath = mainPath+"\\topics.51-100";
	public static final String indexDir = mainPath+"\\index";
	
	public static void main(String[] args) throws Exception {
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
		
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer); 
	          
		
		//For Default Similarity
		IndexSearcher searcher1 = new IndexSearcher(reader);
		searcher1.setSimilarity(new ClassicSimilarity());
		
		//For BM25Similarity
		IndexSearcher searcher2 = new IndexSearcher(reader);
		searcher2.setSimilarity(new BM25Similarity()); 
		
		//For LM with Dirichlet Smoothing
		IndexSearcher searcher3 = new IndexSearcher(reader);
		searcher3.setSimilarity(new LMDirichletSimilarity()); 
		
		//For LM with Jelinek Mercer Smoothing
		IndexSearcher searcher4 = new IndexSearcher(reader);
		searcher4.setSimilarity(new LMJelinekMercerSimilarity((float) 0.7)); 
		
		HashMap<String, IndexSearcher> simMap = new HashMap<String, IndexSearcher>()
		{{
				put("VectorSpace", searcher1);
				put("BM25", searcher2);
				put("LMDirchlet", searcher3);
				put("LMJelinekMercer", searcher4);
		}};
		
		List<String> simList = Arrays.asList("VectorSpace","BM25","LMDirchlet","LMJelinekMercer");
		
		TrecTopicsReader qReader = new TrecTopicsReader();
		QualityQuery topicQueryList[] = qReader.readQueries(new BufferedReader(new FileReader(filePath)));
		
		//topicQueryList.length
			
		for(String simType: simList) {
			
			//Get the similarity type
			IndexSearcher searcherSim = simMap.get(simType);
			
			BufferedWriter titleFile = new BufferedWriter(new FileWriter(mainPath+"\\"+simType+"shortQuery.txt"));
			BufferedWriter descFile = new BufferedWriter(new FileWriter(mainPath+"\\"+simType+"longQuery.txt"));
			
			for(int i=0; i<topicQueryList.length; i++) {
				
				QualityQuery topicQuery = topicQueryList[i];
				String queryId =  topicQuery.getQueryID();
				
				//Consider short query
				String title = topicQuery.getValue("title");
				String titleString = title.replace("/", " ");
						
				Query query = parser.parse(titleString);
				TopScoreDocCollector collector = TopScoreDocCollector.create(1000);
				searcherSim.search(query, collector);
											
				ScoreDoc[] docs = collector.topDocs().scoreDocs;
				for (int jRank = 0; jRank < docs.length; jRank++) {
					
					Document doc = searcherSim.doc(docs[jRank].doc);
					writeRankToFile(queryId, doc.get("DOCNO"), jRank+1 , docs[jRank].score, titleFile, simType+"_short");
				}
				
				//Consider long query
				String desc = topicQuery.getValue("description");
				String descString = desc.replace("/", " ").split("<smry>")[0];
				
				Query query2 = parser.parse(titleString);
				TopScoreDocCollector collector2 = TopScoreDocCollector.create(1000);
				searcherSim.search(query2, collector2);
											
				ScoreDoc[] docs2 = collector2.topDocs().scoreDocs;
				for (int jRank2 = 0; jRank2 < docs2.length; jRank2++) {
					
					Document doc = searcherSim.doc(docs[jRank2].doc);
					writeRankToFile(queryId, doc.get("DOCNO"), jRank2+1, docs[jRank2].score, descFile, simType+"_long");
				}
			}
			titleFile.close();
			descFile.close();
		}
		
			
	}
	
	public static void writeRankToFile(String queryID, String docNum, int rankId, float scoreVal, BufferedWriter fileWriter, String runId) throws IOException {
		
		fileWriter.append(queryID+" 0 "+docNum+" "+rankId+" "+scoreVal+" "+runId);
		fileWriter.newLine();
		
	}
}