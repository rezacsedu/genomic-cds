package test;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.HashSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import exception.BadFormedBase64NumberException;
import exception.VariantDoesNotMatchAnAllowedVariantException;

import safetycode.FileParser_VCF;
import safetycode.MedicineSafetyProfile;
import utils.Common;

public class findVcfSnpsInDbsnp {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		
		MedicineSafetyProfile msp = new MedicineSafetyProfile("d:/MSC_classes_1.owl");
		FileReader ist = new FileReader("d:/workspace/Genomic-CDS/input/user26_file237_yearofbirth_unknown_sex_unknown.23andme-exome-vcf.txt");
		FileParser_VCF fp = new FileParser_VCF(msp.getVCFReflistRsids(), msp.getMapCriteria2Bitcode());
		String report = fp.parse(ist, Common.FORWARD_ORIENTATION);
		String base64ProfileString = fp.getBase64ProfileString();
		System.out.println("report => \n"+report);
		System.out.println("base64 = "+base64ProfileString);

		try {
			msp.readBase64ProfileString(base64ProfileString);
		} catch (VariantDoesNotMatchAnAllowedVariantException e) {
			e.printStackTrace();
		} catch (BadFormedBase64NumberException e) {
			e.printStackTrace();
		}
		OutputStream onto = new FileOutputStream(new File("d:/workspace/Genomic-CDS/output_1/patient_26_model_3.owl"));
		msp.writeModel(onto);
		
		/*String filevcf = "d:/workspace/Genomic-CDS/input/user26_file237_yearofbirth_unknown_sex_unknown.23andme-exome-vcf.txt";
		String fileOntology = "d:/MSC_classes.owl";
		int matched = 0;
		int notMatched = 0;
		try {
			HashSet<String> listRsids = getListRsids(fileOntology);
			BufferedReader br = new BufferedReader(new FileReader(filevcf));
			String linea="";
			while((linea=br.readLine())!=null){
				linea=linea.trim();
				if(linea.startsWith("#") || linea.isEmpty())continue;
				
				String[] tokens = linea.split("\t");
				if(tokens.length > 3){
					String snpToCheck = tokens[2];
					if(snpToCheck.contains("rs")){
						if(listRsids.contains(snpToCheck)){
							System.out.println("Matched SNP = "+snpToCheck+"\t->"+tokens[3]);
							matched++;
						}else{
							notMatched++;
							//System.out.println("Not Matched SNP = "+snpToCheck);
						}
					}
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Matched SNPs = "+matched);
		System.out.println("Not matched SNPs = "+notMatched);
		*/
	}

	
	public static HashSet<String> getListRsids(String fileOntology) throws OWLOntologyCreationException {
		HashSet<String> listRsids = new HashSet<String>();
		
		OWLOntologyManager manager;
		OWLOntology ontology;
		OWLReasoner reasoner;
		// Each String array of the list contains:
		// [0] -> rsid
		manager = OWLManager.createOWLOntologyManager();
		File file = new File (fileOntology);
		ontology = manager.loadOntologyFromOntologyDocument(file);
		OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass human_with_genotype_marker = factory.getOWLClass(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#human_with_genotype_marker"));
		NodeSet<OWLClass> list_marker = reasoner.getSubClasses(human_with_genotype_marker, true);
        for(OWLClass clase: list_marker.getFlattened() ){
        	String rsid="";
        	OWLAnnotationProperty ann_rsid			= factory.getOWLAnnotationProperty(IRI.create("http://www.genomic-cds.org/ont/genomic-cds.owl#rsid"));
        	for (OWLAnnotation annotation : clase.getAnnotations(ontology, ann_rsid)) {
                if (annotation.getValue() instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral) annotation.getValue();
                    rsid=val.getLiteral();
                    break;
                }
            }
        	if(rsid==null || rsid.isEmpty()) continue;
        	listRsids.add(rsid);
        } 
		return listRsids;
	}
	
	
	
	
}
