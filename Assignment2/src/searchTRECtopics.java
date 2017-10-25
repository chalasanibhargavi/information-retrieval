import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class searchTRECtopics {
	
		public static final String mainPath = "D:\\Grad Notes\\Search\\Assignments\\A2";
		public static final String filePath = mainPath+"\\topics.51-100";
		public static final String indexDir = mainPath+"\\index";
	
		public static void main(String[] args) throws Exception {
		
			TrecTopicsReader qReader = new TrecTopicsReader();
			QualityQuery topicQueryList[] = qReader.readQueries(new BufferedReader(new FileReader(filePath)));
			
			BufferedWriter titleFile = new BufferedWriter(new FileWriter(mainPath+"\\EasySearchshortQuery.txt"));
			BufferedWriter descFile = new BufferedWriter(new FileWriter(mainPath+"\\EasySearchlongQuery.txt"));
			
			long startTime = System.currentTimeMillis();
			
			for(int i=0; i<topicQueryList.length; i++) {
				QualityQuery topicQuery = topicQueryList[i];
				String queryId =  topicQuery.getQueryID();
				
				System.out.println(queryId);
				
				//Consider short query
				String title = topicQuery.getValue("title");
				String titleString = title.replace("/", " ");
				
				List<Map.Entry> relvScTitle = EasySearch.getRelevantDocs(titleString);
				
				//Write output to file
				writeToFile(queryId, relvScTitle, titleFile, "Easy_short");
				
				
				//Consider long query
				String desc = topicQuery.getValue("description");
				String descString = desc.replace("/", " ").split("<smry>")[0];
				
				List<Map.Entry> relvScDesc = EasySearch.getRelevantDocs(descString);
				
				//Write output to file
				writeToFile(queryId, relvScDesc, descFile, "Easy_long");
			}
			
			System.out.println("RunTime: "+(System.currentTimeMillis() - startTime)/1000 +" secs");
			
			titleFile.close();
			descFile.close();
	}
	
	public static void writeToFile(String queryID, List<Map.Entry> entryList, BufferedWriter fileWriter, String runId) throws IOException {
		int rankCounter = 1;
		for(Map.Entry e : entryList) {
			
			fileWriter.append(queryID+" 0 "+e.getKey()+" "+rankCounter+" "+e.getValue()+" "+runId);
			fileWriter.newLine();
			++rankCounter;
		}
	}
	
}