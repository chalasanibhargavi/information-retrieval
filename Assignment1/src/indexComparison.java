import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

//Class used to generate the indices using four different analyzers
public class indexComparison {
	public static void main(String[] args) throws ParseException, IOException {
		
		IndexReader readerKey = DirectoryReader.open(FSDirectory.open(Paths.get("D:\\EclipseProjects\\Z534\\IndexKeyword")));
		IndexReader readerStop = DirectoryReader.open(FSDirectory.open(Paths.get("D:\\EclipseProjects\\Z534\\IndexStop")));
		IndexReader readerSimple = DirectoryReader.open(FSDirectory.open(Paths.get("D:\\EclipseProjects\\Z534\\IndexSimple")));
		IndexReader readerStandard = DirectoryReader.open(FSDirectory.open(Paths.get("D:\\EclipseProjects\\Z534\\IndexStandard")));
		
		HashMap<String, IndexReader> analyzeDir = new HashMap<String, IndexReader>()
		{{
				put("Keyword Analyzer", readerKey);
				put("Stop Analyzer", readerStop);
				put("Simple Analyzer", readerSimple);
				put("Standard Analyzer", readerStandard);
		}};
		
		List<String> typeList = Arrays.asList("Keyword Analyzer","Stop Analyzer","Simple Analyzer","Standard Analyzer");
		int i = 0;
		for(String aType :typeList){
			
			System.out.println("\nResults for "+aType+" :\n");
			//Index reader
			IndexReader reader = analyzeDir.get(aType);
					
			//Print the total number of documents in the corpus
			System.out.println("Total number of documents in the corpus: "+reader.maxDoc());                                                                                 
		      
		    //Vocabulary
		    Terms vocabulary1 = MultiFields.getTerms(reader, "TEXT");
		    
		    //Print the total number of tokens for <field>TEXT</field>
		    System.out.println("Number of tokens for TEXT field: "+vocabulary1.getSumTotalTermFreq());	
		                    
		    //Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.
		    System.out.println("Size of the vocabulary for TEXT field: "+vocabulary1.size());
			
		    TermsEnum iterator = vocabulary1.iterator();
	        BytesRef byteRef = null;
			
	        final String vocabDir = "D:/EclipseProjects/Z534/vocab_comp"+i+".txt";
	        
	        //System.out.println("\n*******Vocabulary-Start**********");
	        StringBuffer sb = new StringBuffer();
	        
	        while((byteRef = iterator.next()) != null) {

	           String term = byteRef.utf8ToString();
	           //System.out.print(term+"\t");
	           sb.append(term+"\t");

	       }
	        System.out.println("Writing vocabulary to the directory "+vocabDir);
		    try (BufferedWriter bw = new BufferedWriter(new FileWriter(vocabDir))) {
		    	bw.append(sb);
		    	bw.flush();
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
	       
		  i++;  
		    
		}
		
		
	    
	    
	}
}
