import java.io.BufferedWriter;
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
import utils.Common;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		MedicineSafetyProfile msp = new MedicineSafetyProfile();
		InputStream my23AndMeFileStream=null;
		String fileName="D:/workspace/Genomic-CDS/1097.23andme.564";
		try {
			my23AndMeFileStream = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("ERROR");
		}
		String report = msp.read23AndMeFileStream(my23AndMeFileStream, Common.DBSNP_ORIENTATION);
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
		msp.closeModel(null);
		/*
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
