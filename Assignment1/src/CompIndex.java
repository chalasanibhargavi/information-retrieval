import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class CompIndex {
	public static void main(String[] args) throws ParseException, IOException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths
				.get("D:\\EclipseProjects\\Z534\\Index3")));
		IndexSearcher searcher = new IndexSearcher(reader);

		/**
		 * Get query terms from the query string
		 */
		String queryString = "New York";

		// Get the preprocessed query terms
		
		Analyzer analyzer1 = new KeywordAnalyzer();
		Analyzer analyzer2 = new SimpleAnalyzer();
		Analyzer analyzer3 = new StopAnalyzer();
		Analyzer analyzer4 = new StandardAnalyzer();
		
		List<Analyzer> analyzerList = Arrays.asList(analyzer1, analyzer2, analyzer3, analyzer4);
		
		for(Analyzer analyzer:analyzerList){
			
			QueryParser parser = new QueryParser("TEXT", analyzer);
			Query query = parser.parse(queryString);
			Set<Term> queryTerms = new LinkedHashSet<Term>();
			searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
			System.out.println("Terms in the query: ");
			for (Term t : queryTerms) {
				System.out.println(t.text());
			}
			System.out.println();
		}
		
		
		
		/**
		 * Get document frequency
		 */
		int df=reader.docFreq(new Term("TEXT", "police"));
		System.out.println("Number of documents containing the term \"police\" for field \"TEXT\": "+df);
		System.out.println();

		/**
		 * Get document length and term frequency
		 */
		// Use DefaultSimilarity.decodeNormValue(…) to decode normalized
		// document length
		ClassicSimilarity dSimi = new ClassicSimilarity();
		// Get the segments of the index
		List<LeafReaderContext> leafContexts = reader.getContext().reader()
				.leaves();
		// Processing each segment
		for (int i = 0; i < leafContexts.size(); i++) {
			// Get document length
			LeafReaderContext leafContext = leafContexts.get(i);
			int startDocNo = leafContext.docBase;
			int numberOfDoc = leafContext.reader().maxDoc();
			for (int docId = 0; docId < numberOfDoc; docId++) {
				// Get normalized length (1/sqrt(numOfTokens)) of the document
				float normDocLeng = dSimi.decodeNormValue(leafContext.reader()
						.getNormValues("TEXT").get(docId));
				// Get length of the document
				float docLeng = 1 / (normDocLeng * normDocLeng);
				System.out.println("Length of doc(" + (docId + startDocNo)
						+ ", " + searcher.doc(docId + startDocNo).get("DOCNO")
						+ ") is " + docLeng);
			}
			System.out.println();

			// Get frequency of the term "police" from its postings
			PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
					"TEXT", new BytesRef("police"));
			
			int doc;
			if (de != null) {
				while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
					System.out.println("\"police\" occurs " + de.freq()
							+ " time(s) in doc(" + (de.docID() + startDocNo)
							+ ")");
				}
			}
		}
	}
}
