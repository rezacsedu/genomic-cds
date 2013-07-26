package safetycode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;


import eu.trowl.owlapi3.rel.reasoner.dl.RELReasoner;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;

public class PatientProfileReasoning extends Thread {
	
	private HashMap<String,HashSet<String>> list_recommendations;
	private IRI documentIRI;
	
	public PatientProfileReasoning(HashMap<String,HashSet<String>> list_recommendations, IRI documentIRI){
		this.list_recommendations=list_recommendations;
		this.documentIRI=documentIRI;
	}
	
	
	public void run() {
		try {
			OWLOntologyManager	manager		= OWLManager.createOWLOntologyManager();
			OWLOntology			ontology	= manager.loadOntologyFromOntologyDocument(documentIRI);
			RELReasoner			reasoner	= new RELReasonerFactory().createReasoner(ontology);
			reasoner.precomputeInferences();
			//boolean isConsistent = reasoner.isConsistent();
			//if(isConsistent) System.out.println("La ontología es consistente");
			
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLNamedIndividual patient				= factory.getOWLNamedIndividual(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#this_patient"));//We obtain the patient instance
			OWLAnnotationProperty ann_relevant_for	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#relevant_for"));
			OWLAnnotationProperty ann_cds_message	= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#CDS_message"));
	        if(patient!=null){
	        	NodeSet<OWLClass> list_types = reasoner.getTypes(patient, false);
	        	for(OWLClass type: list_types.getFlattened() ){
	        		String drug_name="";
	        		for (OWLAnnotation annotation : type.getAnnotations(ontology, ann_relevant_for)) {
	        			IRI drug_IRI = IRI.create(annotation.getValue().toString());
	        			OWLClass drug_class = factory.getOWLClass(drug_IRI);
	        			if(drug_class!=null){
	        				Set<OWLAnnotation> listLabels = drug_class.getAnnotations(ontology, factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()));
	        				for(OWLAnnotation labelAnn: listLabels){
	        					if (labelAnn.getValue() instanceof OWLLiteral) {
	        						OWLLiteral literal = (OWLLiteral) labelAnn.getValue();
	        						drug_name = literal.getLiteral().trim().toLowerCase();
	        					}
	        				}
	     	                break;
	        			}
	    	        }
	        		
	        		if(!drug_name.isEmpty()){
	        			String cds_message = "";
	        			for (OWLAnnotation annotation : type.getAnnotations(ontology, ann_cds_message)) {
	        	            if (annotation.getValue() instanceof OWLLiteral) {
	        	                OWLLiteral rule_message = (OWLLiteral) annotation.getValue();
	        	                cds_message = rule_message.getLiteral();
	        	                if(list_recommendations.containsKey(drug_name)){
	        	                	HashSet<String> list_messages = list_recommendations.get(drug_name);
	        	                	list_messages.add(cds_message);
	        	                }else{
	        	                	HashSet<String> list_messages = new HashSet<String>();
	        	                	list_messages.add(cds_message);
	        	                	list_recommendations.put(drug_name, list_messages);
	        	                }
	        	                break;
	        	            }
	        	        }
	        		}	
	        	}
	        }
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
}
