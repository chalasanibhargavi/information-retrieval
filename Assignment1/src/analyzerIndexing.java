import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

//Class used to compare the vocabulary of different analyzers
public class analyzerIndexing {
	
	
	public static final String dataDir = "D:\\EclipseProjects\\Z534\\corpus";
	public static final String indexKeyword = "D:\\EclipseProjects\\Z534\\IndexKeyword";
	public static final String indexStop = "D:\\EclipseProjects\\Z534\\IndexStop";
	public static final String indexSimple = "D:\\EclipseProjects\\Z534\\IndexSimple";
	public static final String indexStandard = "D:\\EclipseProjects\\Z534\\IndexStandard";
	
	public static void main(String[] args) throws Exception {
	

    final Pattern tagRegexDocNo = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
    final Pattern tagRegexText = Pattern.compile("<TEXT>(.+?)</TEXT>", Pattern.DOTALL);
    
    ArrayList<HashMap<String, String>> documents = new ArrayList<HashMap<String, String>>();
               
    int i = 1;
    File dir = new File(dataDir);
	File[] files = dir.listFiles(new FilenameFilter() {
	    public boolean accept(File dir, String name) {
	        return name.endsWith(".trectext");
	    }
	});
	for (File file : files) {
			
			if(i%100 == 0) {
				System.out.println("Parsing file.."+i);
			}
			i++;
			
			//Parse trectext file to get required fields
			String fileContent = readFile(file.getPath(), Charset.defaultCharset());
			
			List<String> splitContentList = splitDocContent(fileContent);
			
			for(String splitContent: splitContentList) {
				HashMap<String, String> document = new HashMap<String, String>();
				//String updated_string = splitContent.replace("\n", "").replace("\r", "");
				
				List<String> docNos = getTagValues(splitContent, tagRegexDocNo);
				document.put("DOCNO", docNos.get(0));
				
				
				List<String> bodyTags = getTagValues(splitContent, tagRegexText);
				if (bodyTags.size() == 1) {
					document.put("TEXT", bodyTags.get(0));
				}
				else {
					document.put("TEXT", String.join("", bodyTags));
				}
				
				documents.add(document);
			}		
	}
			
			try {
				
				
				//Build the Index
				Analyzer analyzer1 = new KeywordAnalyzer();
				Analyzer analyzer2 = new SimpleAnalyzer();
				Analyzer analyzer3 = new StopAnalyzer();
				Analyzer analyzer4 = new StandardAnalyzer();
				
				//Keyword Analyzer
				Directory idir1 = FSDirectory.open(Paths.get(indexKeyword));
				Directory idir2 = FSDirectory.open(Paths.get(indexStop));
				Directory idir3 = FSDirectory.open(Paths.get(indexSimple));
				Directory idir4 = FSDirectory.open(Paths.get(indexStandard));
				
				HashMap<Analyzer, Directory> analyzeDir = new HashMap<Analyzer, Directory>()
				{{
						put(analyzer1, idir1);
						put(analyzer2, idir2);
						put(analyzer3, idir3);
						put(analyzer4, idir4);
				}};
				
				List<Analyzer> analyzerList = Arrays.asList(analyzer1, analyzer2, analyzer3, analyzer4);
				
				int i1 = 1;
				for(Analyzer analyzer:analyzerList){
					
					System.out.println("Indexing for analyzer... "+i1);
					i1++;
					
				    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
				    iwc.setOpenMode(OpenMode.CREATE);
					
				    IndexWriter writer = new IndexWriter(analyzeDir.get(analyzer), iwc);  
				    
				    for (HashMap<String, String> document : documents) {
						indexDoc(writer, document);
					}
				    
				    writer.commit();
				    writer.forceMerge(1);
				    writer.close();
				}
			    
			}catch(IOException e) {
				System.out.println(" caught a " + e.getClass()
				+ "\n with message: " + e.getMessage());
			}
			
}	

	static void indexDoc(IndexWriter writer, HashMap<String, String> document) throws IOException{
	    
		Document luceneDoc = new Document();                                                                     
	
	    luceneDoc.add(new StringField("DOCNO", document.get("DOCNO"), Field.Store.YES)); 
	    luceneDoc.add(new TextField("TEXT", document.get("TEXT"), Field.Store.NO));
	
	    writer.addDocument(luceneDoc);
	
	}              
	
	
	public static String readFile(String path, Charset encoding) 
			  throws IOException 
	{
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);
	}
	
	public static List<String> splitDocContent(String input) {
		List<String> splitContentList = new ArrayList<String>();
		
		//\\s+(?=<DOC>)|(?<=</DOC>)\\s+
		final Pattern tagRegexDOC = Pattern.compile("<DOC>(.+?)</DOC>", Pattern.DOTALL);
		final Matcher matcherDOC = tagRegexDOC.matcher(input);
		
		//for(String s : input.split("\\s+(?=<DOC>)|(?<=</DOC>)\\s+")){
		while(matcherDOC.find()) {
			splitContentList.add(matcherDOC.group(1));
		}
		return splitContentList;
	}
	
	public static List<String> getTagValues(String str, Pattern tagRegex ) {
	    final List<String> tagValues = new ArrayList<String>();    
	    final Matcher matcher = tagRegex.matcher(str);
	    
	    while (matcher.find()) {
	    	
	        tagValues.add(matcher.group(1));
	    }
	    return tagValues;
	}

}
