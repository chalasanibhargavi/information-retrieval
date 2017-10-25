import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Set;


public class EasySearch {
	
	//Class to create a custom search function
    public static List<Map.Entry> getRelevantDocs(String queryStr) throws Exception{
    	
    	//Read the index
        final String indexDir = "D:\\Grad Notes\\Search\\Assignments\\A2\\index";
        
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
        IndexSearcher searcher = new IndexSearcher(reader);
        
        int numTotalDocs = reader.maxDoc();
        //System.out.println("Total number of docs: "+numTotalDocs);

        // Get the preprocessed query terms
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser("TEXT", analyzer);
    
        Query query = parser.parse(queryStr);
        Set<Term> queryTerms = new LinkedHashSet<Term>();
        searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
        
        HashMap<String, Integer> termDocMap = new HashMap<String, Integer>();
        
        for (Term t : queryTerms) {
            
            //Count of all documents containing the term
            int termDocFreq=reader.docFreq(new Term("TEXT", t.text()));
            termDocMap.put(t.text(), termDocFreq);

        }
        

        //Use DefaultSimilarity.decodeNormValue(…) to decode normalized document length
        ClassicSimilarity dSimi = new ClassicSimilarity();
        HashMap<String, Float> docRelvMap = new HashMap<String, Float>();
        HashMap<String, Float> normLenMap = new HashMap<String, Float>();
        
        //Get the segments of the index
        List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
        
        //System.out.println("Searching..");
        for (int i = 0; i < leafContexts.size(); i++) {
        	
            LeafReaderContext leafContext=leafContexts.get(i);
            int startDocNo=leafContext.docBase;
            int numberOfDoc=leafContext.reader().maxDoc();
            
            
            for (int docId = startDocNo; docId < startDocNo+numberOfDoc; docId++) {
                
            	//Get normalized length for each document
                float normDocLen=dSimi.decodeNormValue(leafContext.reader()
                        .getNormValues("TEXT").get(docId-startDocNo)); //For index
                float docLen = 1 / (normDocLen * normDocLen);
                
                String docNum = searcher.doc(docId).get("DOCNO");
                normLenMap.put(docNum, docLen);
                         
            }
            
            
            HashMap<String, Float> termScoreMap = new HashMap<String, Float>();
            for (Term t : queryTerms) {

	            //Get the term frequency within each document containing it 
            	
	            PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(), 
	            		"TEXT", new BytesRef(t.text()));
	            
	            int doc;
	            if(de != null) {
	            	
	            	while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
		            	
		            	int termFreq = de.freq();
		            	String docNum = searcher.doc(de.docID()+startDocNo).get("DOCNO");
		            	float tfVal = (float) (termFreq / normLenMap.get(docNum));
		            	float idfVal = (float) (Math.log(1 + ((float) numTotalDocs / termDocMap.get(t.text()))) / Math.log(2));
		            	float relvScoreTerm = tfVal + idfVal ;
		            	
		            	termScoreMap.put(t.text()+"|"+docNum , relvScoreTerm);
		                
		            }
	            }      
            }
            
            //Get score based on all query terms
            for (int docId = startDocNo; docId < startDocNo+numberOfDoc; docId++) {
               
                String docNum = searcher.doc(docId).get("DOCNO");
                
                float relvScoreDoc = 0;
                for (Term t : queryTerms) {
                	
                	float tScore = 0;
                	
                	if (termScoreMap.containsKey(t.text()+"|"+docNum)) {
                		tScore = termScoreMap.get(t.text()+"|"+docNum);
                	}
              	
                	relvScoreDoc = relvScoreDoc + tScore;
                }
                
                docRelvMap.put(docNum, relvScoreDoc);
            }
        }
        
        class FloatEntryComparator implements Comparator<Map.Entry> {
        	  public int compare(Map.Entry e1, Map.Entry e2) {
        	    return ((Float)e2.getValue()).intValue() - ((Float)e1.getValue()).intValue();
        	  }
        }

        List<Map.Entry> entries = new ArrayList<Map.Entry>(docRelvMap.entrySet());
        Collections.sort(entries, new FloatEntryComparator());
        
        //Return top 1000 docs
        return entries.subList(0,1000);
    }
}
        
