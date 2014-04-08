package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class GetPolymorphismAllele {

	/**
	 * Process the ontology to obtain the list of polymorphisms, their related alleles and the format that can be used to test them. 
	 */
	public static void main(String[] args) {
		OWLOntology ontology		= null;
		OWLOntologyManager manager	= null;
		OWLReasoner reasoner		= null;
		
		try{
			manager = OWLManager.createOWLOntologyManager();
			IRI physicalURI = IRI.create("file:d:/workspace/Genomic%20CDS/medicine%20safety%20code%20server/trunk/Genomic-CDS/WebContent/MSC_classes.owl");
			ontology = manager.loadOntologyFromOntologyDocument(physicalURI);
			OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
			reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
			OWLDataFactory factory = manager.getOWLDataFactory();
			
			OWLClass				polyClass			= manager.getOWLDataFactory().getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#polymorphism"));
			OWLAnnotationProperty	ann_tested_with		= manager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/MSC_classes.owl#can_be_tested_with"));
			OWLAnnotationProperty	ann_relevant_for	= manager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
			OWLAnnotationProperty	ann_label			= factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
			HashMap<String, HashSet<String>> list_poly_snps = new HashMap<String,HashSet<String>>();
			NodeSet<OWLClass> list_marker = reasoner.getSubClasses(polyClass, true);
			for(OWLClass clase: list_marker.getFlattened() ){
				String allele_name	= null;
				String poly_name	= null;
				for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_relevant_for)) {
        			IRI allele_IRI = IRI.create(annotation.getValue().toString());
        			OWLClass allele_class = factory.getOWLClass(allele_IRI);
        			if(allele_class!=null){
        				//allele_name = allele_class.getIRI().toString();
        				Set<OWLAnnotation> listLabels = allele_class.getAnnotations(ontology, ann_label);
        				for(OWLAnnotation labelAnn: listLabels){
        					if (labelAnn.getValue() instanceof OWLLiteral) {
        						OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
        						allele_name = literal.getLiteral().trim().toLowerCase();
        					}
        				}
     	                break;
        			}
    	        }
				
				boolean notSkip = false;
				Set<OWLAnnotation> list_chips = clase.getAnnotations(ontology, ann_tested_with);
				for(OWLAnnotation chipAnn: list_chips){
					if (chipAnn.getValue() instanceof OWLLiteral) {
						OWLLiteral literal = (OWLLiteral) chipAnn.getValue();
						String tested_with = literal.getLiteral().trim().toLowerCase();
						if(tested_with.contains("23andMe v3")){
							notSkip=true;;
						}
					}
				}
				if(notSkip) continue;
				
				Set<OWLAnnotation> listLabels = clase.getAnnotations(ontology, ann_label);
				for(OWLAnnotation labelAnn: listLabels){
					if (labelAnn.getValue() instanceof OWLLiteral) {
						OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
						poly_name = literal.getLiteral().trim().toLowerCase();
					}
					break;
				}
				
				if(allele_name!=null){
					if(list_poly_snps.containsKey(allele_name)){
						list_poly_snps.get(allele_name).add(poly_name);
					}else{
						HashSet<String> list_snps = new HashSet<String>();
						list_snps.add(poly_name);
						list_poly_snps.put(allele_name, list_snps);
					}
				}
			}
			
			Iterator<String> it_keys = list_poly_snps.keySet().iterator();
			while(it_keys.hasNext()){
				String allele = it_keys.next().toUpperCase();
				System.out.print(allele+"\t");
				Iterator<String> it_snps = list_poly_snps.get(allele.toLowerCase()).iterator();
				while(it_snps.hasNext()){
					String snp = it_snps.next();
					System.out.print(snp);
					if(it_snps.hasNext()) System.out.print("\t");
				}
				System.out.println();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
