import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

//Class used to get the stats from the generated index
public class DataStats {
	public static void main(String[] args) throws Exception {
		
		final String indexDir = "D:\\EclipseProjects\\Z534\\Index";
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get((indexDir))));
		 
		//Print the total number of documents in the corpus
		System.out.println("Total number of documents in the corpus: "+reader.maxDoc());                            
	                   
	    //Print the number of documents containing the term "new" in <field>TEXT</field>.
	    System.out.println("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
	
	    //Print the total number of occurrences of the term "new" across all documents for <field>TEXT</field>.
	    System.out.println("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));                                                       
	      
	    //Vocabulary
	    Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
	                    
	    //Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.
	    System.out.println("Size of the vocabulary for TEXT field: "+vocabulary.size());
	
	    //Print the total number of documents that have at least one term for <field>TEXT</field>
	    System.out.println("Number of documents that have at least one term for TEXT field: "+vocabulary.getDocCount());
	    
	    //Print the total number of tokens for <field>TEXT</field>
	    System.out.println("Number of tokens for TEXT field: "+vocabulary.getSumTotalTermFreq());	
	    
	    //Print the total number of postings for <field>TEXT</field>
	    System.out.println("Number of postings for TEXT field: "+vocabulary.getSumDocFreq());      	                   
	
	    //Print the vocabulary for <field>TEXT</field>
	    TermsEnum iterator = vocabulary.iterator();
        BytesRef byteRef = null;
		
        final String vocabDir = "D:/EclipseProjects/Z534/vocab.txt";
        
        //System.out.println("\n*******Vocabulary-Start**********");
        StringBuffer sb = new StringBuffer();
        
        while((byteRef = iterator.next()) != null) {

           String term = byteRef.utf8ToString();
           //System.out.print(term+"\t");
           sb.append(term+"\t");

       }
        System.out.println("\nWriting vocabulary to the directory "+vocabDir);
	    try (BufferedWriter bw = new BufferedWriter(new FileWriter(vocabDir))) {
	    	bw.append(sb);
	    	bw.flush();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
       

       //System.out.println("\n*******Vocabulary-End**********");        

       reader.close();
	}
}
