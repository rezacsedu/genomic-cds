package test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import eu.trowl.owlapi3.rel.tms.reasoner.dl.RELReasoner;
import eu.trowl.owlapi3.rel.tms.reasoner.dl.RELReasonerFactory;

public class PruebaReclassify {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String ontofile1 = "d:/MSC_classes_1.owl";
		String outputfile1 = "d:/workspace/Genomic-CDS/output_1/output_patient22.owl";
		OWLOntology ontology = null;		
		OWLOntologyManager manager = null;
		try{
			System.out.println("1 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
			manager = OWLManager.createOWLOntologyManager();
			File file = new File(ontofile1);
			ontology = manager.loadOntologyFromOntologyDocument(file);
			RELReasonerFactory relfactory = new RELReasonerFactory();
			RELReasoner reasoner = relfactory.createReasoner(ontology);
			System.out.println("2 "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
			reasoner.precomputeInferences();
			
			
			System.out.println(" "+new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
