import utils.Common;

/*import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import safetycode.MedicineSafetyProfile;
import utils.Common;*/

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println(Common.convertFrom64To2("cCB3RLNS2vCXUgK5Gl3c2z12jrLzVjqND-AG3bH7jWhDSVG392k2SR_NWK0nzPwTq51Y27ZaVmMFKHvuUpTUrYFZYXICleZWeBgQYW--8V1GAC80Ftvio9N9piVo0I2yH0-3Y0vmFC3KG0000"));
		System.out.println(Common.convertFrom2To64("100110001100001011000011011011010101010111011100000010111001001100100001011110101011110100000101010000101111000011100110000010111101000001000010101101110101010101111101011111101101110100010111001101111110001010010000000011100101010001000111101101100000101011001101011100011111010000000011001001000010101110000010011100011011111111010111100000010100000000110001111101011001111010011101110100000101000001100010000010000111100011100100011111110000010110001111010100010001111001111000011110110011011101011110110101100010001111100011100010100001010010001100101111101000100011100000101000001011101010011010100010100000111110111110001000011111000001010000001010001100001000000000001111110111111001101100110010001001010111001001110011101100011111110010000000010010000010111100010001000000111110000011100010000000111001110000001111001100000011010100010000000000000000000000000000"));
		
		/*TEST 2
		InputStream my23AndMeFileStream=null;
		String fileName="D:/workspace/Genomic-CDS/1097.23andme.564";
		String report="";
		MedicineSafetyProfile msp = null;
		
		msp = new MedicineSafetyProfile();
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		report = msp.read23AndMeFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/workspace/Genomic-CDS/report.txt"));
			bw.write(report+"\n");
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(msp.getBase64ProfileString());
		msp.closeModel(null);
		
		msp = new MedicineSafetyProfile();
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		report = msp.read23AndMeFileStream(my23AndMeFileStream, Common.FORWARD_ORIENTATION);
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter("D:/workspace/Genomic-CDS/report2.txt"));
			bw.write(report+"\n");
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		System.out.println(msp.getBase64ProfileString());
		msp.closeModel(null);*/
		
		
		/*TEST 1
		OntModel model = ModelFactory.createOntologyModel();
		InputStream in = FileManager.get().open("D:/workspace/Genomic-CDS/MSC_classes.ttl");
		if (in == null) {
		    throw new IllegalArgumentException(
		                                 "File: " + "D:/workspace/Genomic-CDS/MSC_classes.ttl" + " not found");
		}
		RDFReader reader = model.getReader("N3");
		reader.read(model, in,"http://www.genomic-cds.org/ont/genomic-cds.owl");
		
				
		String queryString = Common.SPARQL_NAME_SPACES
				+ " SELECT DISTINCT ?item ?rsid ?rank ?position ?bit_length " 
				+ " WHERE { " 
				+ "     ?item pgx:rsid ?rsid . "
				+ "		?item sc:rank ?rank . " 
				+ "		?item sc:position_in_base_2_string ?position . " 
				+ "		?item sc:bit_length ?bit_length . " 
				+ " } ORDER BY ?rank ";

		QueryExecution qexec = QueryExecutionFactory.create(QueryFactory.create(queryString), model);

		try {
			int tam=0;
			AnnotationProperty ap1 = model.getAnnotationProperty("http://www.genomic-cds.org/ont/MSC_classes.owl#criteria_syntax");
			AnnotationProperty ap2 = model.getAnnotationProperty("http://www.genomic-cds.org/ont/MSC_classes.owl#bit_code");
			
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				QuerySolution soln	= results.nextSolution();
				Resource res		= soln.getResource("item");
				Literal rsid		= soln.getLiteral("rsid");
				Literal rank 		= soln.getLiteral("rank");
				Literal position	= soln.getLiteral("position");
				Literal bit_length	= soln.getLiteral("bit_length");
				
				System.out.println(rsid.getString()+" -> ["+rank.getInt()+"] = "+bit_length.getInt()+" -> position = "+position.getInt());
				tam+=bit_length.getInt();
				if(res.canAs(OntClass.class)){
					OntClass ontClass = res.as(OntClass.class);
					ExtendedIterator<OntClass> eioc = ontClass.listSubClasses();
					while(eioc.hasNext()){
						OntClass oc = eioc.next();
						RDFNode criteria_syntax	= oc.getPropertyValue(ap1);
						if(criteria_syntax==null) continue;
						RDFNode bit_code 		= oc.getPropertyValue(ap2);
						if(bit_code==null) continue;
						
						//System.out.print("\t "+criteria_syntax.toString()+" -> ");
						//System.out.println(bit_code.toString());
					}
				}
			}
			System.out.println("Total length = "+tam);
		} finally {
			qexec.close();
		}*/
	}
}
