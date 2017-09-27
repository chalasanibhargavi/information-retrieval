
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;



//Class used to generate the index for the given set of documents using Standard Analyzer
public class generateIndex {
	
		public static final String dataDir = "D:\\EclipseProjects\\Z534\\corpus";
		public static final String indexDir = "D:\\EclipseProjects\\Z534\\Index";
	
		public static void main(String[] args) throws Exception {
		

	    final Pattern tagRegexDocNo = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
	    final Pattern tagRegexHead = Pattern.compile("<HEAD>(.+?)</HEAD>");
	    final Pattern tagRegexByLine = Pattern.compile("<BYLINE>(.+?)</BYLINE>");
	    final Pattern tagRegexDateLine = Pattern.compile("<DATELINE>(.+?)</DATELINE>");
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
					
					List<String> headTags = getTagValues(splitContent, tagRegexHead);
					if (headTags.size() == 1) {
						document.put("HEAD", headTags.get(0));
					}
					else {
						document.put("HEAD", String.join(" ", headTags));
					}
					
					List<String> bylineTags = getTagValues(splitContent, tagRegexByLine);
					if (bylineTags.size() == 1) {
						document.put("BYLINE", bylineTags.get(0));
					}
					else {
						document.put("BYLINE", String.join(" ", bylineTags));
					}
					
					List<String> datelineTags = getTagValues(splitContent, tagRegexDateLine);
					if (datelineTags.size() == 1) {
						document.put("DATELINE", datelineTags.get(0));
					}
					else {
						document.put("DATELINE", String.join(" ", datelineTags));
					}
					
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
				  
					System.out.println("Indexing to directory " + indexDir + " ...");
					
					 //Get index path
					Directory idir = FSDirectory.open(Paths.get(indexDir));
					
					//Build the Index
				    Analyzer analyzer = new StandardAnalyzer();
				    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
				    iwc.setOpenMode(OpenMode.CREATE);
					
				    IndexWriter writer = new IndexWriter(idir, iwc);  
				    
				    for (HashMap<String, String> document : documents) {
						indexDoc(writer, document);
					}
				    				   
				    writer.commit();
				    writer.forceMerge(1);
				    writer.close();
			
				}catch(IOException e) {
					System.out.println(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
				}
				
	
}	
	
	static void indexDoc(IndexWriter writer, HashMap<String, String> document) throws IOException{
        
		Document luceneDoc = new Document();                                                                     

        luceneDoc.add(new StringField("DOCNO", document.get("DOCNO"), Field.Store.YES)); 
        luceneDoc.add(new StringField("HEAD", document.get("HEAD"), Field.Store.NO));
        luceneDoc.add(new StringField("BYLINE", document.get("BYLINE"), Field.Store.NO));	
        luceneDoc.add(new StringField("DATELINE", document.get("DATELINE"), Field.Store.NO));
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
