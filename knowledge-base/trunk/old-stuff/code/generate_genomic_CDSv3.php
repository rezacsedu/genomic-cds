<?php
error_reporting(E_ALL);
require_once 'Classes/PHPExcel/IOFactory.php';
date_default_timezone_set('Europe/London');
ini_set("memory_limit","3072M");



/***************************************
 *******  Input file locations   *******
 ***************************************/
//$db_snp_xml_input_file_location = "..\\data\\dbSNP\\core_rsid_data_from_dbsnp_25_3_2014.xml";//Source file of SNP descriptions gathered from dbSNP.
//$db_snp_xml_input_file_location = "..\\data\\dbSNP\\140602093523_XMF.xml";
$db_snp_xml_input_file_location = "..\\data\\dbSNP\\140611100354_XMF.xml";
$snps_not_in_dbSNP_file_location = "..\\data\\dbSNP\\list_snps_not_in_dbSNP.txt";

$haplotype_spreadsheet_file_location = "..\\data\\PharmGKB\\haplotype_spreadsheet_disabled_overlappings.xlsx";//Source file of haplotype descriptions, such as CYP2C19*1 or DPYD*2A.

$pharmacogenomics_decision_support_spreadsheet_file_location = "..\\data\\decision-support-rules\\Pharmacogenomics decision support spreadsheet_v2.xlsx";//Source file of pharmacogenomics rule descriptions.

$pharmacogenomic_CDS_base_file_location = "..\\ontology\\genomic-cds_base.owl";//Base file of the ontology that conceptualizes the pharmacogenomics domain.
$MSC_classes_base_file_location = "..\\ontology\\MSC_classes_base.owl";//Base file of the ontology that will be used in MSC server and contains all descriptions of the human subclasses.
$pharmacogenomic_CDS_demo_additions_file_location = "..\\ontology\\genomic-cds_demo_additions.owl";//Base file of the ontology with patient's genotype used for prototyping. 
$CDS_rule_demo_additions_file_location = "..\\ontology\\genomic-cds_rule_demo_additions.owl";
$snpCoveredBy23andMe_v2_file_location = "..\\data\\assay-information\\SNPs covered by 23andMe v2.txt";//List of SNPs that are recognized by 23andMe v2 format.
$snpCoveredBy23andMe_v3_file_location = "..\\data\\assay-information\\SNPs covered by 23andMe v3.txt";//List of SNPs that are recognized by 23andMe v3 format.
$snpCoveredByAffimetrix_file_location = "..\\data\\assay-information\\SNPs covered by Affymetrix DMET chip - PMID 20217574.txt";//List of SNPs that are recognized by Affymetrix DMET chip - 20217574.
$snpCoveredByUniOfFloridaAndStandford_file_location = "..\\data\\assay-information\\SNPs covered by University of Florida and Stanford University chip - PMID 22910441.txt";//List of SNPs that are recognized by the University of Florida and Stanford chip - PMID 22910441.txt.




/******************************************
 *******   Output file locations   ********
 ******************************************/
$light_rule_file_location = "..\\ontology\\genomic-cds.owl";//Light ontology version with the conceptualization of haplotypes and the CDS and phenotype rules.
$light_rule_demo_file_location = "..\\ontology\\genomic-cds_demo.owl";//Light ontology version with demo patient's genotype based on direct association of user with SNPs variant classes and the genomic-cds_rules ontology. 

$full_rule_file_location = "..\\ontology\\MSC_classes.owl";//Full ontology version with the conceptualization of human SNP variant classes, human haplotype classes and the CDS and phenotype rules.
$full_rule_demo_file_location = "..\\ontology\\MSC_classes_demo.owl";//Full ontology version with a demo patient's genotype based on human SNP variants and the MSC_classes ontology.

$CDS_server_file_location = "..\\ontology\\MSC_textual_rules.owl";// Full ontology version but with only the textual description of CDS and phenotype rules. It is the one used in MSC server.
$CDS_server_demo_file_location = "..\\ontology\\MSC_textual_rules_demo.owl";// Full ontology version with a demo patient's genotype based on human SNP variants and the MSC_textual_rules ontology.

$report_file_location = "..\\ontology\\generate_genomic_CDS_report.txt";//Output report about problems that may happen during the execution of this script.


/**************************************************
 *******  Initializing important variables  *******
 **************************************************/
$owl = file_get_contents($pharmacogenomic_CDS_base_file_location) . "\n\n\n"; // Read the content of base ontology.
$msc_owl = file_get_contents($MSC_classes_base_file_location) . "\n\n\n"; // Read the content of base ontology for encoding/decoding Medicine Safety Codes (MSC server).
$rule_owl = "\n\n";
$textual_rule = "\nClass: rule\n\tAnnotations:\n\t\trdfs:label \"CDS rule\",\n\t\trdfs:comment \"A rule is a logical description of a drug recommendation based on human genotype.\"\n\nClass: phenotype_rule\n\tAnnotations:\n\t\trdfs:label \"Phenotype rule\",\n\t\trdfs:comment \"A phenotype rule is a logical description of a patient phenotype based on its genotype.\"\n\n";

$report = "-- Report -- \n\n"; // Start with the content of the script report and error log.
$valid_polymorphism_variants = array(); // List of valid polymorphism variants from dbSNP (used to find errors and orientation mismatches).

$snps_covered_by_23andMe_v2 = read_file_into_array($snpCoveredBy23andMe_v2_file_location); // Read the content of SNPs in 23andMe v2 format.
$snps_covered_by_23andMe_v3 = read_file_into_array($snpCoveredBy23andMe_v3_file_location); // Read the content of SNPs in 23andMe v3 format.
$snps_covered_by_Affimetrix_DMET_chip = read_file_into_array($snpCoveredByAffimetrix_file_location); // Read the content of SNPs in Affimetrix format.
$snps_covered_by_University_of_Florida_and_Standford_chip = read_file_into_array($snpCoveredByUniOfFloridaAndStandford_file_location); // Read the content of the University of Florida and Standford format.
$snps_not_in_dbSNP = read_file_into_array($snps_not_in_dbSNP_file_location); //Read the file that contains the SNPs to skip during the processing of dbSNP file.

/***************************
 *******  Functions  *******
 ***************************/
//This function indicates if a SNP is part of the genotype of a patient in the ontology. This is needed because a patient's genotype is not only described using haplotypes, such as BRCA1*1, but also with SNPs, such as rs2297595.
function rankSNPInGenotype($rsid){
	$rank_snps = array("rs9923231","rs4149056","rs9934438","rs6025","rs12979860","rs67376798","rs2297595");
	foreach ($rank_snps as $element){
		if($element == $rsid){
			return true;
		}
	}
	return false;
}

//Find if the orientation of a SNPs description in the dbSNP xml file is incosnistent with the observed alleles and the reference allele. 
function is_ref_allele_inconsistent($list_variants,$ref_variant,$rsid){
	$orient = "reverse";
	for($i=0;$i<count($list_variants);$i++){
		if(preg_match("/".$ref_variant."/",$list_variants[$i])){
			$orient="forward";
			break;
		}
	}
	return ($orient=="reverse");
}

//Function that indicates if the variant belongs to a SNP column that was disabled in the file snps_not_in_dbSNP_file_location.
function in_array_skip_snps($variant,$list_skip_variants){
	for($i=0;$i<count($list_skip_variants);$i++){
		if(strpos($variant,$list_skip_variants[$i]."_")!==false){
			return true;
		}
	}
}

// Read file and transform it into an array of lines.
function read_file_into_array($file){
	$search = array("\r\n", "\n");
	$snp_array = [];
	$handle = fopen($file, 'r');
	if ($handle) {
		while (!feof($handle)) {
			$line = fgets($handle);
			$line = str_replace($search, $replace='', $line);
			$snp_array[] = $line;
		}
		fclose($handle);
	}
	return $snp_array;
}

// It annotates each SNP with the types of genotype file formats that support it.
function generate_assay_annotations($rsid_string){
	global $snps_covered_by_23andMe_v2;
	global $snps_covered_by_23andMe_v3;
	global $snps_covered_by_Affimetrix_DMET_chip;
	global $snps_covered_by_University_of_Florida_and_Standford_chip;
	
	$owl="";
	
	if (in_array($rsid_string, $snps_covered_by_23andMe_v2)) {
		$owl .= "\tAnnotations: can_be_tested_with 23andMe_v2 \n";
	}
	
	if (in_array($rsid_string, $snps_covered_by_23andMe_v3)) {
		$owl .= "\tAnnotations: can_be_tested_with 23andMe_v3 \n";
	}
	
	if (in_array($rsid_string, $snps_covered_by_Affimetrix_DMET_chip)) {
		$owl .= "\tAnnotations: can_be_tested_with Affymetrix_DMET_chip \n";
	}
	
	if (in_array($rsid_string, $snps_covered_by_University_of_Florida_and_Standford_chip)) {
		$owl .= "\tAnnotations: can_be_tested_with University_of_Florida_and_Stanford_University_chip \n";
	}
		
	return $owl;
}

// It transforms ids in order to be used in an ontology URI.
function make_valid_id($string) {
	$substitutions = array(
			"(" => "_",
			")" => "_",
			" " => "_",
			"[" => "_",
			"]" => "_",
			"/" => "_",
			":" => "_",
			"*" => "star_", 
			"#" => "_hash",
			"__"=> "_"
	);
	$string = trim($string);
	$string = strtr($string, $substitutions);
	if(substr($string,0,1) == "_"){
		$string = substr($string,1);
	}
	
	if(substr($string,strlen($string)-1,strlen($string)) == "_"){
		$string = substr($string,0,strlen($string)-1);
	}
	return strtr($string, $substitutions);
}

// It adapts '"' characters from comment annotations to avoid problems in the ontology.
function clean_comment_string($string){
	$substitutions = array("\"" => "\\\"");
	return strtr($string,$substitutions);
}

// Get URL strings and try to find if there are more than one in it.
function parse_multiple_URLs($string)	{
	if(empty($string)) return "";
	$list_elements = preg_split("/\n/", $string);
	$return_string = "";
	for($j=0;$j<count($list_elements);$j++){
		if(!empty($list_elements[$j])){
			$return_string .= "   Annotations: rdfs:seeAlso <" . $list_elements[$j] . ">\n";
		}
	}
	return $return_string;
}

// Change the SNP orientation taking into account the positions and nucleotides.
function flipOrientationOfStringRepresentation($string) {
	$substitutions = array(
			"A" => "T", 
			"T" => "A", 
			"C" => "G", 
			"G" => "C"
	);
	$value = strtr($string, $substitutions);
	if(strlen($value) > 1){
		$array_char = str_split($value);
		$value = "";
		for($i=(count($array_char)-1);$i>=0;$i--){
			$value .= $array_char[$i];
		}
	}
	return $value;
}

// Generate a set of disjoint classes from a list of classes of the ontology.
function generateDisjointClassesOWL($id_array) {
	$owl = "";
	if (count($id_array) >= 2) {
		$owl .= "Class: " . $id_array[0] . "\n";  // TODO: This should not be necessary. It is a fix for a bug in the OWLAPI/Protege Manchester Syntax parser (The last class before the DisjointClasses frame is taken into the disjoint). 
		$owl .= "DisjointClasses:" ."\n";
		$owl .= implode(",", $id_array) . "\n\n";
	}
	return $owl;
}

// Define all combinations of SNP variants related to every subclass of human_with_genotype_marker.
function make_safety_code_allele_combination_owl($human_with_genotype_at_locus, $decimal_code, $binary_code, $allele_1, $allele_2, $rs_id) {
	
	$variant_qname = "sc:human_with_genotype_rs" . $rs_id . "_variant_" . make_valid_id($allele_1 . "_" . $allele_2);
	$allele_1_id = "rs" . $rs_id . "_" . make_valid_id($allele_1);
	$allele_2_id = "rs" . $rs_id . "_" . make_valid_id($allele_2);

	$owl_fragment = "Class: $variant_qname \n" .
			"    SubClassOf: sc:$human_with_genotype_at_locus \n";
	if ($allele_1 == "null" or $allele_2 == "null") {
		// if information is absent, do not add OWL axiom
	}
	else if ($allele_1 == $allele_2) {
		// if homozygous...
		$owl_fragment .= "    SubClassOf: has exactly 2 $allele_1_id \n";
	}
	else {
		// if heterozygous...
		$owl_fragment .= "    SubClassOf: has some  $allele_1_id and has some $allele_2_id \n";
	}
	$owl_fragment .= 
			"    Annotations: rdfs:label \"human with rs" . $rs_id . "(" . $allele_1 . ";" . $allele_2 . ")\"  \n" .
			"    Annotations: sc:criteria_syntax \"rs" . $rs_id . "(" . $allele_1 . ";" . $allele_2 . ")\"  \n";
	return $owl_fragment;
}

//(Not in use) It provides a sound signal.
function beep ($int_beeps = 1) {
	$string_beeps = "";
    for ($i = 0; $i < $int_beeps; $i++): $string_beeps .= "\x07"; endfor;
    isset ($_SERVER['SERVER_PROTOCOL']) ? false : print $string_beeps;
}


/******************************************************
 **  Add version information (we are using the date  **
 **  of generation of the ontology for versioning)   **
 ******************************************************/

$owl .= "\n\n";
$owl .=  "Ontology: <http://www.genomic-cds.org/ont/genomic-cds.owl>\n";
$owl .=  "    Annotations: owl:versionInfo \"" . date("Y/m/d") . "\"\n";
$genotype_rank = 1;


/*********************************************
 *******  Read and convert dbSNP data  *******
 *********************************************/

$owl .= "\n\n#\n# dbSNP data\n#\n\n";
print("Processing dbSNP data" . "\n");

$xml = simplexml_load_file($db_snp_xml_input_file_location); // Parse xml file.

$polymorphism_disjoint_list = array();
$i = 0;
$nsnpsvariants = 0;
$nsnps = 0;
// For each SNP we obtain: (1) rsid; (2) class, "snp" or "in-del"; (3) Assembly element with reference = true (described below); 
foreach ($xml->Rs as $Rs) {
	$rs_id =  $Rs['rsId'];
	$human_with_genotype_at_locus = "human_with_genotype_at_rs" . $rs_id;
	$snp_class = $Rs['snpClass'];  //TODO: implement "in-del" snp class functionalities
	$observed_alleles = $Rs->Sequence->Observed;//Max length = 4;
	$nsnps = $nsnps+1;
	//** Get the right Assembly element with reference=true **//
	$fxn_sets = null; //$Rs->Assembly->Component->MapLoc->FxnSet;
	$assembly_genome_build = null; //$Rs->Assembly['genomeBuild'];
	$assembly_group_label = null; //$Rs->Assembly['groupLabel'];
	$orient = null; //$Rs->Assembly->Component->MapLoc['orient']; 
	$ref_allele = null; //$Rs->Assembly->Component->MapLoc['refAllele'];
	
	$assembly_sets = $Rs->Assembly;
	if(isset($assembly_sets)){
		foreach($assembly_sets as $assembly_set) {
			if(isset($assembly_set['reference']) && $assembly_set['reference'] == true){
				if(isset($assembly_set['genomeBuild'])){
					$assembly_genome_build = $assembly_set['genomeBuild'];
				}
				if(isset($assembly_set['groupLabel'])){
					$assembly_group_label = $assembly_set['groupLabel'];
				}
				$component_sets = $assembly_set->Component;
				if(isset($component_sets)){
					foreach($component_sets as $component_set){
						$maploc_sets = $component_set->MapLoc;
						if(isset($maploc_sets)){
							foreach($maploc_sets as $maploc_set){
								if(isset($maploc_set['refAllele'])){
									$ref_allele = $maploc_set['refAllele'];
								}
								if(isset($maploc_set['orient'])){
									$orient=$maploc_set['orient'];
								}
								$fxn_sets = $maploc_set->FxnSet;
								break;
							}
						}
						break;
					}
				}
				break;
			}
		}
	}
	if(!isset($fxn_sets) || !isset($assembly_genome_build) || !isset($assembly_group_label) || !isset($orient)){
		$report .= "\n	ERROR: We have found that the $rs_id content from dbSNP is not complete. We skip processing it.\n";
		print($i." Not processed rsid = ".$rs_id."\n");
		continue;
	}
	
	// Add gene symbols in this entry to array.
	foreach ($fxn_sets as $fxn_set) {
		if(isset($fxn_set["symbol"])){
			$gene_ids[] = make_valid_id($fxn_set["symbol"]);
		}
	}
	
	
	if(!(strpos($observed_alleles, "(") === false)){
		$snp_string = substr($observed_alleles,strpos($observed_alleles,"("),(strpos($observed_alleles,")")-strpos($observed_alleles,"("))+1);
		$sub_alleles = substr($observed_alleles,strpos($observed_alleles,")")+1);
		$new_list_alleles = preg_split("/\//", $sub_alleles);
		$observed_alleles = "";
		for($j=0;$j<count($new_list_alleles);$j++){
			if($new_list_alleles[$j] == "-"){
				$new_list_alleles[$j] = "D";
			}
			if(!empty($observed_alleles)) $observed_alleles.="/";
			$observed_alleles .= $snp_string . $new_list_alleles[$j];
		}
	}
	$observed_alleles = preg_split("/\//", $observed_alleles);
	// Normalize orientation to the plus strand.
	for($j=0;$j<count($observed_alleles);$j++){
		if($observed_alleles[$j] == "-"){
			$observed_alleles[$j] = "D";
		}
		$nsnpsvariants++;
	}	
	if ($orient == "reverse" || is_ref_allele_inconsistent($observed_alleles,$ref_allele,$rs_id)){
		if($orient!= "reverse"){
			$report .= "\n	ERROR: We have found that the $rs_id from dbSNP is inconsistent with the used alleles in the haplotype spreadsheet.\n";
		}
	
		for($j=0;$j<count($observed_alleles);$j++){
			$observed_alleles[$j] = flipOrientationOfStringRepresentation($observed_alleles[$j]);
		}
	}
	sort($observed_alleles, SORT_STRING);
		
	// Create OWL classes for all possible genotype variants
	$class_id = "rs" . $rs_id;		
	$owl .= "Class: " . $class_id . "\n";// Create subclasses of Polymorphism->(1)rsid; (2)rdfs:label; (3)can_be_tested_with; (4)relevant_for; (5)rdfs:seeAlso; (6)dbsnp_orientation_on_reference_genome;
	$owl .= "    SubClassOf: polymorphism" . "\n";
	$owl .= "    Annotations: rsid \"rs" . $rs_id . "\"\n";
	$owl .= "    Annotations: rdfs:label \"rs" . $rs_id . "\"\n";
	if(rankSNPInGenotype($class_id)){
		$owl .= "	Annotations: rank \"".$genotype_rank."\"\n";
		$genotype_rank++;
	}
	$owl .= generate_assay_annotations($class_id);
	
	
	$fxn_symbols=null;
	foreach ($fxn_sets as $fxn_set) {
		if(isset($fxn_set["symbol"])){
			$fxn_symbols[] = make_valid_id($fxn_set["symbol"]);
		}
	} 
	if(isset($fxn_symbols)){
		$fxn_symbols = array_unique($fxn_symbols);
		foreach ($fxn_symbols as $fxn_symbol){
			$owl .= "   Annotations: relevant_for " .$fxn_symbol. "\n";
		}
	}
	$owl .= "    Annotations: rdfs:seeAlso <http://bio2rdf.org/dbsnp:rs" . $rs_id . "> \n"; 
	$owl .= "    Annotations: dbsnp_orientation_on_reference_genome \"" . $orient . "\" \n\n";
	$polymorphism_disjoint_list [] = $class_id;
	
	/*
	$owl .= "Class: human \n";
	$owl .= "    SubClassOf: has exactly 2 " . $class_id . "\n\n";
	*/
	
	$polymorphism_variant_disjoint_list = array();
	foreach ($observed_alleles as $observed_allele) {
		$variant_class_id = $class_id . "_" . make_valid_id($observed_allele);
		$owl .= "Class: " . $variant_class_id . "\n";
		$owl .= "    SubClassOf: " . $class_id . "\n";
		
		if(strpos($observed_allele,trim($ref_allele)) !== false || $observed_allele == $ref_allele){
			$owl .= "    Annotations: vcf_format_reference \"true\" \n";
		}else{
		
		}
		$owl .= "    Annotations: rdfs:label \"" . $variant_class_id . "\" \n\n";
		$polymorphism_variant_disjoint_list[] = $variant_class_id;
		 
		$valid_polymorphism_variants[] = $variant_class_id;
	}	
	
	$owl .= generateDisjointClassesOWL($polymorphism_variant_disjoint_list);
	
	// Generate human genotype marker variations for Medicine Safety Code ontology
	$msc_owl .= "Class: sc:$human_with_genotype_at_locus \n";// Create subclasses of human_with_genotype_marker-> (1)rsid; (2)sc:rank; (3)dbsnp_orientation_on_reference_genome; (4)symbol_of_associated_gene; (5)sc:bit_length; (6)sc:position_in_base_2_string;
	$msc_owl .= "    SubClassOf: human_with_genotype_marker \n";
	$msc_owl .= "    Annotations: rsid \"rs" . $rs_id . "\" \n";
	$msc_owl .= "    Annotations: dbsnp_orientation_on_reference_genome \"" . $orient . "\" \n";
	
	$gene_symbols = array();
	foreach ($fxn_sets as $fxn_set) {
		$gene_symbol = $fxn_set["symbol"];
		if ($gene_symbol !== "") {
			$gene_symbols[] = $gene_symbol;
		}
	}
	$gene_symbols = array_unique($gene_symbols);
	foreach ($gene_symbols as $gene_symbol) {
		$msc_owl .= "    Annotations: symbol_of_associated_gene \"" . $gene_symbol . "\" \n";
	}
	
	switch (count($observed_alleles)) {//Define the corresponding subclasses of the human_with_genotype_marker class -> (1)rdfs:label; (2)sc:criteria_syntax
		case 2:
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 0, "00", "null", "null", $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 1, "01", $observed_alleles[0],  $observed_alleles[0], $rs_id);			
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 2, "10", $observed_alleles[0],  $observed_alleles[1], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 3, "11", $observed_alleles[1],  $observed_alleles[1], $rs_id);			
			$msc_owl .= "\n";
						
			break;
		case 3:
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 0, "000", "null", "null", $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 1, "001", $observed_alleles[0], $observed_alleles[0], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 2, "010", $observed_alleles[0], $observed_alleles[1], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 3, "011", $observed_alleles[0], $observed_alleles[2], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 4, "100", $observed_alleles[1], $observed_alleles[1], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 5, "101", $observed_alleles[1], $observed_alleles[2], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 6, "110", $observed_alleles[2], $observed_alleles[2], $rs_id);
			$msc_owl .= "\n";
			
			break;
		case 4:
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 0, "0000", "null", "null", $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 1, "0001", $observed_alleles[0], $observed_alleles[0], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 2, "0010", $observed_alleles[0], $observed_alleles[1], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 3, "0011", $observed_alleles[0], $observed_alleles[2], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 4, "0100", $observed_alleles[0], $observed_alleles[3], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 5, "0101", $observed_alleles[1], $observed_alleles[1], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 6, "0110", $observed_alleles[1], $observed_alleles[2], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 7, "0111", $observed_alleles[1], $observed_alleles[3], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 8, "1000", $observed_alleles[2], $observed_alleles[2], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 9, "1001", $observed_alleles[2], $observed_alleles[3], $rs_id);
			$msc_owl .= "\n";
			
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 10, "1010", $observed_alleles[3], $observed_alleles[3], $rs_id);
			$msc_owl .= "\n";
			
			break;
		default://ERROR if there a greater size of observed alleles
			$report .= ("WARNING: There  are less than 2 or more than 4 alleles for $class_id -- Medicine Safety Code class was not generated.\n");
	}
	$i = $i + 1;
}

$report .= ("We have defined $nsnps SNPs with $nsnpsvariants variants in the ontology.\n");

//Generate description of genes related to allele definitions.
$gene_ids[] = "HLA-B"; //We manually add this gene_id because it is used in haplotype definitions but it is not related to any SNPs
$gene_ids[] = "HLA-A"; //We manually add this gene_id because it is used in haplotype definitions but it is not related to any SNPs
$gene_ids = array_unique($gene_ids);
$ngenes = count($gene_ids);
$report .= ("We have defined $ngenes genes in the ontology.\n");
foreach ($gene_ids as $gene_id) {
	$owl .= "Class: " . $gene_id . "\n";
	$owl .= "    SubClassOf: allele \n";
	$owl .= "    Annotations: rdfs:label \"" . $gene_id . "\" \n";
	$owl .= "	 Annotations: rank \"" . $genotype_rank . "\" \n\n";
	$genotype_rank++;
}

/*$owl .= "Class: human" . "\n";
foreach ($gene_ids as $gene_id) {
	if($gene_id != "CYP2D6"){
		$owl .= "    SubClassOf: has exactly 2 " . $gene_id . "\n";
	}
}*/

/************************************************************
 ***** Read and convert data from haplotype spreadsheet *****
 ************************************************************/

$owl .= "\n\n#\n# Data from haplotype spreadsheet\n#\n\n";

$objPHPExcel = PHPExcel_IOFactory::load($haplotype_spreadsheet_file_location);
$snp_list = array(); //Needed to know which SNPs are used in the haplotype definitions and to search into DBSNP.
$allele_id_array = array(); // Needed for creating disjoints later on
$nalleles = 0;
foreach ($objPHPExcel->getWorksheetIterator() as $objWorksheet) {// We analyze every haplotype in the document.
	
	$homozygous_human_id_array = array(); // Needed for creating disjoints later on
	$worksheet_title = $objWorksheet->getTitle();
	
	print("Processing haplotype spreadsheet " . $worksheet_title . "\n");
	
	$haplotypes_disjoints = array();// Array to record the disjoints haplotypes
	if (strpos($worksheet_title,"_") === 0) {// Skip sheets starting with "_" (can be used for sheets that need more work etc.)
		$report .= "\n	We skip processing the haplotypes definitions of gene ".$worksheet_title."\n";
		//print("Skip ". $worksheet_title ."\n");
		continue; 
	};
	
	$header_array = array();
	
	foreach ($objWorksheet->getRowIterator() as $row) {
		$row_array = array();
		$error_during_processing = false;
		
		// Special processing of first row (header)
		if($row->getRowIndex() == 1) {
			$cellIterator = $row->getCellIterator();
			
			foreach ($cellIterator as $cell) {
				$header_array[$cell->getColumn()] = $cell->getValue();
			}
			continue;
		}
	
		// Processing of other rows (except the first) from here on
		$cellIterator = $row->getCellIterator();
		$cellIterator->setIterateOnlyExistingCells(true);
		
		foreach ($cellIterator as $cell) {
			$row_array[$cell->getColumn()] = $cell->getValue();
		}
		
		if(isset($row_array['A'])){
			$allele_status = trim($row_array['A']);
		}else{
			$allele_status = "enabled";
		}
		if(!isset($row_array['B'])||(trim($row_array['B'])=="")||!isset($row_array['D'])||(trim($row_array['D']) == "")){
			$report .= "\n	ERROR: We have found a haplotype definition from ".$worksheet_title." without gene or haplotype id defined. We skip processing it.\n";
			continue;
		}		
		
		$gene_label = trim($row_array['B']);
		$gene_id = make_valid_id($gene_label);
		
		$allele_label = trim($row_array['B'] . " " . $row_array['D']);
		$allele_id = make_valid_id($allele_label);
		
		$human_label = "human with " . $allele_label;
		$human_id = make_valid_id($human_label);
		$human_homozygous_label = "human with homozygous " . $allele_label;
		$human_homozygous_id = make_valid_id($human_homozygous_label);
		
		$allele_polymorphism_variants = array();
		
		foreach($row_array as $key=>$value) {
			// Skip first four columns 
			if (($key == "A") or ($key == "B") or ($key == "C") or ($key == "D")) continue; 
			
			if(!isset($header_array[$key])){
				$report .= "\n	ERROR: The header column in $key position is not defined for $worksheet_title. We skip processing the column values (if there are any).\n";
				//print($worksheet_title." in column $key is not defined.\n");
				continue;
			}
			
			$snp_value = trim(str_replace("[tag]","",$row_array[$key])); //We should have done this replacement but we repeat it just in case of an error.
			$snp_value = trim(str_replace("del","D",$snp_value));//We should have done this replacement but we repeat it just in case of an error.
			
			if($snp_value == "*"){
				//$report .= "\n	The value of the column in '$key' is not valid in $allele_id of $worksheet_title gene. We skip processing the column value.\n";
				continue;
			}
			
			$allele_polymorphism_variant = make_valid_id($header_array[$key] . "_" . $snp_value); // e.g. "rs12345_A"
			if(strpos($header_array[$key],'rs') !== false && strpos($header_array[$key],' ') === false){
				$snp_list[] = $header_array[$key];
			}
			
			//Do not process the SNP of an allele variant if it matchs one of following conditions:
			//1) The whole allele definition is disabled.
			//2) The SNP value is '*' that means it is not defined in the table.
			//3) The SNP column was disabled in the file: snps_not_in_dbSNP_file_location 
			//4) The SNP column does not contains a valid rs number id. i.e. -2808A>Cstar_A
			if($allele_status != "disabled" && $snp_value != "*" && (!in_array_skip_snps($allele_polymorphism_variant, $snps_not_in_dbSNP)) && preg_match_all("/^rs/",$allele_polymorphism_variant)){ //Skip the variants that are not recognized by dbSNP, and parse the variants that starts with a rs number.
				// Report an error if the allele polymorphism variant was not already generated during the dbSNP conversion (we want to make sure everything matches dbSNP. If it does not, this is an indication of an error in the data).
				if (in_array($allele_polymorphism_variant, $valid_polymorphism_variants) == false) {
					$report .= "ERROR: Polymorphism variant \"" . $allele_polymorphism_variant . "\" in allele " . $allele_label . " does not match dbSNP. Skipping conversion for the entire allele." . "\n";
					$error_during_processing = true;
				}else{
					// Add id of polymorphism to array
					$allele_polymorphism_variants[] = $allele_polymorphism_variant;
				}
			}
		}
		
		//Definition of subclasses of allele
		$allele_id_array[] = $allele_id;
		
		if (!isset($row_array['C']) || $row_array['C'] == "") {// If cell in superclass column is empty...
			if(isset($haplotypes_disjoints[0])){
				$array_aux = $haplotypes_disjoints[0];
				$array_aux[] = $allele_id;
				$haplotypes_disjoints[0] = $array_aux;
			}else{
				$array_aux = array();
				$array_aux[] = $allele_id;
				$haplotypes_disjoints[0] = $array_aux;
			}
			$owl .= "Class: " . $allele_id . "\n";
			$owl .= "    Annotations: rdfs:label \"" . $allele_label . "\"\n";
			$owl .= "    SubClassOf: " . $gene_id . "\n\n";
			
			if($gene_id == "CYP2D6"){
				$duplicated_allele_id = str_replace("CYP2D6","CYP2D6_duplicated",$allele_id);
				$duplicated_allele_label = str_replace("CYP2D6","CYP2D6 duplicated",$allele_label);
				$owl .= "Class: " . $duplicated_allele_id . "\n";
				$owl .= "    Annotations: rdfs:label \"" . $duplicated_allele_label . "\"\n";
				$owl .= "    SubClassOf: " . $gene_id . "\n\n";
			}
			
		}else {// If cell in superclass column is not empty (i.e., a superclass is defined)...
			
			$superclass_label = $row_array['B'] . " " . $row_array['C'];
			$superclass_id = make_valid_id($superclass_label);
			
			if(isset($haplotypes_disjoints[$superclass_id])){
				$array_aux = $haplotypes_disjoints[$superclass_id];
				$array_aux[] = $allele_id;
				$haplotypes_disjoints[$superclass_id] = $array_aux;
			}else{
				$array_aux = array();
				$array_aux[] = $allele_id;
				$haplotypes_disjoints[$superclass_id] = $array_aux;
			}
			
			if(isset($haplotypes_disjoints[0])){
				$array_aux = $haplotypes_disjoints[0];
				$array_aux[] = $superclass_id;
				$haplotypes_disjoints[0] = $array_aux;
			}else{
				$array_aux = array();
				$array_aux[] = $superclass_id;
				$haplotypes_disjoints[0] = $array_aux;
			}
			
			$owl .= "Class: " . $superclass_id . "\n";
			$owl .= "    Annotations: rdfs:label \"" . $superclass_label . "\"\n";
			$owl .= "    SubClassOf: " . $gene_id . "\n\n";
			
			$owl .= "Class: " . $allele_id . "\n";
			$owl .= "    Annotations: rdfs:label \"" . $allele_label . "\"\n";
			$owl .= "    SubClassOf: " . $superclass_id . "\n\n";
			
			if($gene_id == "CYP2D6"){
				$duplicated_allele_id = str_replace("CYP2D6","CYP2D6_duplicated",$allele_id);
				$duplicated_allele_label = str_replace("CYP2D6","CYP2D6 duplicated",$allele_label);
				$owl .= "Class: " . $duplicated_allele_id . "\n";
				$owl .= "    Annotations: rdfs:label \"" . $duplicated_allele_label . "\"\n";
				$owl .= "    SubClassOf: " . $superclass_id . "\n\n";
			}
		}
		
		if ($error_during_processing) { // CONTINUE if error occured (i.e., don't add any OWL expressions at all for this row)
			$report .= "\n	ERROR: In $allele_id of $worksheet_title gene.\n";
			//print("Error when processing the allele ". $allele_id ."\n");
			//continue; 
		}
		
		$homozygous_human_id_array[] = $human_homozygous_id;
		
		if($gene_id == "CYP2D6"){
			$duplicated_allele_id = str_replace("CYP2D6","CYP2D6_duplicated",$allele_id);
			$duplicated_allele_label = str_replace("CYP2D6","CYP2D6 duplicated",$allele_label);
			$owl .= "Class: human_with_" . $duplicated_allele_id . "\n";
			$owl .= "SubClassOf: human_with_genetic_polymorphism" . "\n";
			$owl .= "SubClassOf:" . "\n";
			$owl .= "has some " . $duplicated_allele_id . "\n";
			$owl .= "Annotations: rdfs:label \"human with " . $duplicated_allele_label . "\" \n\n";
		}
		
		// Rules for (at least) heterozygous polymorphisms
		$owl .= "Class: " . $human_id . "\n";
		$owl .= "SubClassOf: human_with_genetic_polymorphism" . "\n";
		$owl .= "Annotations: rdfs:label \"" . $human_label . "\"\n";
		
		
		if (!isset($row_array['A']) || $row_array['A'] != "disabled") {
		
			// If there are polymorphism variants...
			if (empty($allele_polymorphism_variants) == false) {
				$owl .= "EquivalentTo:" . "\n";
				$owl .= "has some " . implode(" and has some ", $allele_polymorphism_variants);
				$owl .= "\n\n";
			}
			else {
				$report .= "WARNING: No polymorphism variants found at all for " . $allele_id . "\n";
			}

			$owl .= "SubClassOf:" . "\n";
			$owl .= "has some " . $allele_id . "\n\n";			
		}else{
			$owl .= "SubClassOf:" . "\n";
			$owl .= "has some " . $allele_id . "\n\n";
		}
		
		if($gene_id == "CYP2D6"){
			$duplicated_allele_id = str_replace("CYP2D6","CYP2D6_duplicated",$allele_id);
			$duplicated_allele_label = str_replace("CYP2D6","CYP2D6 duplicated",$allele_label);
			$owl .= "Class: human_with_homozygous_" . $duplicated_allele_id . "\n";
			$owl .= "SubClassOf: human_with_genetic_polymorphism" . "\n";
			$owl .= "SubClassOf:" . "\n";
			$owl .= "has exactly 2 " . $duplicated_allele_id . "\n";
			$owl .= "Annotations: rdfs:label \"human with homozygous " . $duplicated_allele_label . "\" \n\n";
		}	
		
		// Rules for homozygous polymorphisms and alleles
		$owl .= "Class: " . $human_homozygous_id . "\n";
		$owl .= "SubClassOf: human_with_genetic_polymorphism" . "\n";
		$owl .= "Annotations: rdfs:label \"" . $human_homozygous_label . "\" \n";
				
		
				
		if (!isset($row_array['A']) || $row_array['A'] ==! "disabled") {
			// If there are tagging polymorphism variants...
			if (empty($allele_polymorphism_variants) == false) {
				$owl .= "EquivalentTo:" . "\n";
				$owl .= "has exactly 2 " . implode(" and has exactly 2 ", $allele_polymorphism_variants);
				$owl .= "\n\n";
			}
			$owl .= "SubClassOf:" . "\n";
			$owl .= "has exactly 2 " . $allele_id . "\n\n";

		}else{
			$owl .= "SubClassOf:" . "\n";
			$owl .= "has exactly 2 " . $allele_id . "\n\n";
		}
		$nalleles = $nalleles+1;
	}
	
	// Produce disjoints between alleles variants
	foreach($haplotypes_disjoints as $group_alleles_disjoints){
		$owl .=	generateDisjointClassesOWL($group_alleles_disjoints);
	}
	
	// NOTE: Disjoints between underdefined/overlapping alleles produce unsatisfiable homozygous humans.
	$owl .= "# homozygous human disjoints\n";
	$owl .= generateDisjointClassesOWL($homozygous_human_id_array);
}

$report .= ("We have defined $nalleles alleles variants in the ontology.\n");

/*********************************************
 ***** Processing CDS rules information ******
 *********************************************/

// NOTE: We modified the cds rules because in the new system the reasoning is focused only on SNP to Alleles and the rules are triggered with a different system.
 
$owl .= "\n\n#\n# Pharmacogenomics decision support table data\n#\n\n";
$objPHPExcel = PHPExcel_IOFactory::load($pharmacogenomics_decision_support_spreadsheet_file_location);

$objWorksheet = $objPHPExcel->getSheetByName("CDS rules");

$nrules = 0;
foreach ($objWorksheet->getRowIterator() as $row) {	
	$row_array = array();	
	// Skip first row
	if($row->getRowIndex() == 1) { continue; }
	
	// Processing of other rows (except the first) from here on
	$cellIterator = $row->getCellIterator();
	$cellIterator->setIterateOnlyExistingCells(true);
	foreach ($cellIterator as $cell) {
		$row_array[$cell->getColumn()] = $cell->getCalculatedValue();
	}
	if (!isset($row_array['B']) || $row_array['B'] == "disabled"){
		$report .= "\n	We skip processing cds rule ".$row_array['A']." because it was disabled.\n";
		continue;
	}
	$rule_status = $row_array['B'];
	
	if(!isset($row_array['A'])){
		$report .= "\n	ERROR: We have found a cds rule without name. We skip processing it.\n";
		continue;		
	}
	$human_class_label = trim($row_array['A']);
	
	if(!isset($row_array['C'])){
		$report .= "\n	ERROR: We skip processing cds rule ".$row_array['A']." because it has not any drug related.\n";
		continue;
	}
	$drug_label = $row_array['C'];
		
	$source_repository = $row_array['D'];
	
	$textual_description_of_genetic_attributes = $row_array['E'];

	$logical_description_of_genetic_attributes = trim($row_array['F']);
	
	if(isset($row_array['G'])){
		$phenotype_description = $row_array['G'];
	}else{
		$phenotype_description = "";
	}
	
	if(isset($row_array['H'])){
		$recommendation_in_english = $row_array['H'];
	}else{
		$recommendation_in_english = "";
	}
	
	if(isset($row_array['J'])){
		$recommendation_importance = $row_array['J'];
	}else{
		$recommendation_importance = "";
	}
	
	if(isset($row_array['L'])){
		$recommendation_URL = $row_array['L'];
	}else{
		$recommendation_URL = "";
	}
	
	if(isset($row_array['P'])){
		$date_of_evidence = date('d-M-Y',PHPExcel_Shared_Date::ExcelToPHP($row_array['P']));
	}else{
		$date_of_evidence = "";
	}
	
	if(isset($row_array['Q'])){
		$date_of_addition = date('d-M-Y',PHPExcel_Shared_Date::ExcelToPHP($row_array['Q']));
	}else{
		$date_of_addition = "";
	}
	
	if(isset($row_array['R'])){
		$date_last_validation = date('d-M-Y',PHPExcel_Shared_Date::ExcelToPHP($row_array['R']));
	}else{
		$date_last_validation = "";
	}
	
	if(isset($row_array['S'])){
		$author_last_validation = $row_array['S'];
	}else{
		$author_last_validation = "";
	}
	
	if(isset($row_array['T'])){
		$author_addition = $row_array['T'];
	}else{
		$author_addition = "";
	}	
	// Skip processing if not all required data items are present
	if ($human_class_label == "" or $recommendation_in_english == "") {
		$report .= "NOTE: Not all required values were found in the pharmacogenomics decision support spreadsheet row " . $row->getRowIndex() . ", skipping conversion of this row.\n";
		continue;
	}
	
	$nrules++;
		
	$textualtriggeringrule = "Class: " . make_valid_id($human_class_label) . "\n";
	$textualtriggeringrule .= "   SubClassOf: rule" . "\n";
	$textualtriggeringrule .= "   Annotations: rdfs:label \"" . $human_class_label . "\"\n";
	$textualtriggeringrule .= "   Annotations: rdfs:comment \"" . preg_replace('/\s+/', ' ', trim($logical_description_of_genetic_attributes)) . "\"\n";
	if ($drug_label ==! "") {
		$textualtriggeringrule .= "   Annotations: relevant_for " . make_valid_id($drug_label) . "\n";
		$drug_labels[] = make_valid_id($drug_label);
	}
	
	$humantriggeringrule = "Class: human_triggering_" . make_valid_id($human_class_label) . "\n";
	$humantriggeringrule .= "   SubClassOf: human_triggering_CDS_rule" . "\n";
	$humantriggeringrule .= "   Annotations: rdfs:label \"human triggering " . $human_class_label . "\"\n";
	$humantriggeringrule .= "   Annotations: relevant_for " . make_valid_id($drug_label) . "\n";
	if(strlen($logical_description_of_genetic_attributes)!=0){
		$humantriggeringrule .= "	EquivalentTo: human and (" . preg_replace('/\s+/', ' ', trim($logical_description_of_genetic_attributes)) . ")\n";
	}

	$textualtriggeringrule	.= "   Annotations: source \"" . $source_repository . "\"\n";
	$humantriggeringrule	.= "   Annotations: source \"" . $source_repository . "\"\n";
	
	if(isset($textual_description_of_genetic_attributes) && $textual_description_of_genetic_attributes != ""){
		$textualtriggeringrule	.= "   Annotations: textual_genetic_description \"" . clean_comment_string($textual_description_of_genetic_attributes) . "\"\n";
		$humantriggeringrule	.= "   Annotations: textual_genetic_description \"" . clean_comment_string($textual_description_of_genetic_attributes) . "\"\n";
	}
	
	if(isset($phenotype_description) && $phenotype_description != ""){
		$textualtriggeringrule	.= "   Annotations: phenotype_description \"" . clean_comment_string($phenotype_description) . "\"\n";
		$humantriggeringrule	.= "   Annotations: phenotype_description \"" . clean_comment_string($phenotype_description) . "\"\n";
	}
	
	if(isset($recommendation_importance) && $recommendation_importance != ""){
		$textualtriggeringrule	.= "   Annotations: recommendation_importance \"" . $recommendation_importance . "\"\n";
		$humantriggeringrule	.= "   Annotations: recommendation_importance \"" . $recommendation_importance . "\"\n";
	}
	
	if(isset($recommendation_URL) && $recommendation_URL != ""){
		$textualtriggeringrule	.= parse_multiple_URLs($recommendation_URL);
		$humantriggeringrule	.= parse_multiple_URLs($recommendation_URL);
	}
	
	if(isset($date_of_evidence) && $date_of_evidence != ""){
		$textualtriggeringrule	.= "   Annotations: date_of_evidence \"" . $date_of_evidence . "\"\n";
		$humantriggeringrule	.= "   Annotations: date_of_evidence \"" . $date_of_evidence . "\"\n";
	}
	
	if(isset($date_of_addition) && $date_of_addition != ""){
		$textualtriggeringrule	.= "   Annotations: date_of_addition \"" . $date_of_addition . "\"\n";
		$humantriggeringrule	.= "   Annotations: date_of_addition \"" . $date_of_addition . "\"\n";
	}
	
	if(isset($date_last_validation) && $date_last_validation != ""){
		$textualtriggeringrule	.= "   Annotations: date_last_validation \"" . $date_last_validation . "\"\n";
		$humantriggeringrule	.= "   Annotations: date_last_validation \"" . $date_last_validation . "\"\n";
	}
	
	if(isset($author_last_validation) && $author_last_validation != ""){
		$textualtriggeringrule	.= "   Annotations: author_last_validation \"" . $author_last_validation . "\"\n";
		$humantriggeringrule	.= "   Annotations: author_last_validation \"" . $author_last_validation . "\"\n";
	}
	
	if(isset($author_addition) && $author_addition != ""){
		$textualtriggeringrule	.= "   Annotations: author_addition \"" . $author_addition . "\"\n";
		$humantriggeringrule	.= "   Annotations: author_addition \"" . $author_addition . "\"\n";
	}
	
	$textualtriggeringrule	.= "   Annotations: CDS_message \"" . addslashes($recommendation_in_english) . "\"\n\n";
	$humantriggeringrule	.= "   Annotations: CDS_message \"" . addslashes($recommendation_in_english) . "\"\n\n";
	
	$textual_rule .= $textualtriggeringrule . "\n\n";
	$rule_owl .= $humantriggeringrule ."\n\n";
}

$drug_labels = array_unique($drug_labels);
foreach ($drug_labels as $drug_label) {
	$owl .= "Class: " . make_valid_id($drug_label) . "\n";
	$owl .= "    Annotations: rdfs:label \"" . $drug_label . "\"\n";
	$owl .= "    SubClassOf: drug" . "\n\n";
}

$owl .= "# drug disjoints\n";
$owl .= generateDisjointClassesOWL($drug_labels);

$ndrugs = count($drug_labels);
$report .= ("We have defined $ndrugs drugs in the ontology.\n");
$report .= ("We have defined $nrules rules in the ontology.\n");

/******************************
 ****** Processing drugs ******
 ******************************/

$objWorksheet = $objPHPExcel->getSheetByName("Drugs");

foreach ($objWorksheet->getRowIterator() as $row) {
	$row_array = array();
	
	// Skip first row
	if($row->getRowIndex() == 1) {
		continue;
	}
	
	// Processing of other rows (except the first) from here on
	$cellIterator = $row->getCellIterator();
		
	foreach ($cellIterator as $cell) {
		$row_array[$cell->getColumn()] = $cell->getCalculatedValue();
	}
	
	$entity_label = $row_array["A"];
	$comment = $row_array["B"];
	$external_URL = $row_array["C"];
	
	$owl .= "Class: " . make_valid_id($entity_label) . "\n";
	$owl .= "    Annotations: rdfs:label \"" . $entity_label . "\"\n";
	$owl .= parse_multiple_URLs($external_URL);
	$owl .= "    Annotations: rdfs:comment \"" . $comment . "\"\n\n";
}

/**********************************************
 ****** Processing phenotype information ******
 **********************************************/
$objWorksheet = $objPHPExcel->getSheetByName("Phenotypes");
$nrules = 0;
foreach ($objWorksheet->getRowIterator() as $row) {
	$row_array = array();
	
	// Skip first row
	if($row->getRowIndex() == 1) {
		continue;
	}
	
	// Processing of other rows (except the first) from here on
	$cellIterator = $row->getCellIterator();
		
	foreach ($cellIterator as $cell) {
		$row_array[$cell->getColumn()] = $cell->getCalculatedValue();
	}
	
	if(!isset($row_array['A']) || $row_array['A'] == ""){
		$report .= "\n	ERROR: We have found a phenotype rule without name. We skip processing it.\n";
		continue;
	}
	$phenotype_label = $row_array["A"];
	
	if(isset($row_array["B"])){
		$phenotype_source = $row_array["B"];
	}else{
		$phenotype_source ="";
	}
	
	if(isset($row_array["C"])){
		$phenotype_textual_description = $row_array["C"];
	}else{
		$phenotype_textual_description = "";
	}
	
	$phenotype_logical_statements = trim($row_array["D"]);
	
	if(isset($row_array["F"])){
		$phenotype_URL = $row_array["F"];
	}else{
		$phenotype_URL = "";
	}
	
	if(isset($row_array["G"])){
		$date_of_evidence = date('d-M-Y',PHPExcel_Shared_Date::ExcelToPHP($row_array['G']));
	}else{
		$date_of_evidence = "";
	}
	
	if(isset($row_array["H"])){
		$date_of_addition = date('d-M-Y',PHPExcel_Shared_Date::ExcelToPHP($row_array['H']));
	}else{
		$date_of_addition = "";
	}
	
	if(isset($row_array["I"])){
		$date_last_validation = date('d-M-Y',PHPExcel_Shared_Date::ExcelToPHP($row_array['I']));
	}else{
		$date_last_validation = "";
	}
	
	if(isset($row_array["J"])){
		$author_last_validation = $row_array['J'];
	}else{
		$author_last_validation = "";
	}
	
	if(isset($row_array["K"])){
		$author_addition = $row_array['K'];
	}else{
		$author_addition = "";
	}
	$nrules = $nrules + 1;
	
	$textualtriggeringrule	= "Class: " . make_valid_id($phenotype_label) . "\n";
	$textualtriggeringrule	.= "	SubClassOf: phenotype_rule " . "\n";
	$textualtriggeringrule	.= "	Annotations: rdfs:comment \"" . preg_replace('/\s+/', ' ', trim($phenotype_logical_statements)) . "\"\n";
	$textualtriggeringrule	.= "	Annotations: rdfs:label \"" . $phenotype_label . "\"\n";
	
	$humantriggeringrule	= "Class: human_with_" . make_valid_id($phenotype_label) . "\n";
	$humantriggeringrule	.= "   SubClassOf: human_triggering_phenotype_inference_rule" . "\n";
	$humantriggeringrule	.= "   Annotations: rdfs:label \"human with " . $phenotype_label . "\"\n";
	if(strlen($phenotype_logical_statements) != 0){
		$humantriggeringrule	.= "	EquivalentTo: human and (" . preg_replace('/\s+/', ' ', trim($phenotype_logical_statements)) . ")\n";
	}
	$textualtriggeringrule	.= "   Annotations: source \"" . $phenotype_source . "\"\n";
	$humantriggeringrule	.= "   Annotations: source \"" . $phenotype_source . "\"\n";
	$textualtriggeringrule	.= parse_multiple_URLs($phenotype_URL);
	$humantriggeringrule	.= parse_multiple_URLs($phenotype_URL);
	
	if(isset($date_of_evidence) && $date_of_evidence != ""){
		$textualtriggeringrule	.= "   Annotations: date_of_evidence \"" . $date_of_evidence . "\"\n";
		$humantriggeringrule	.= "   Annotations: date_of_evidence \"" . $date_of_evidence . "\"\n";
	}	
	if(isset($date_of_addition) && $date_of_addition != ""){
		$textualtriggeringrule	.= "   Annotations: date_of_addition \"" . $date_of_addition . "\"\n";
		$humantriggeringrule	.= "   Annotations: date_of_addition \"" . $date_of_addition . "\"\n";
	}	
	if(isset($date_last_validation) && $date_last_validation != ""){
		$textualtriggeringrule	.= "   Annotations: date_last_validation \"" . $date_last_validation . "\"\n";
		$humantriggeringrule	.= "   Annotations: date_last_validation \"" . $date_last_validation . "\"\n";
	}	
	if(isset($author_last_validation) && $author_last_validation != ""){
		$textualtriggeringrule	.= "   Annotations: author_last_validation \"" . $author_last_validation . "\"\n";
		$humantriggeringrule	.= "   Annotations: author_last_validation \"" . $author_last_validation . "\"\n";
	}	
	if(isset($author_addition) && $author_addition != ""){
		$textualtriggeringrule	.= "   Annotations: author_addition \"" . $author_addition . "\"\n";
		$humantriggeringrule	.= "   Annotations: author_addition \"" . $author_addition . "\"\n";
	}
	$textualtriggeringrule	.= "	Annotations: textual_genetic_description \"" . clean_comment_string($phenotype_textual_description) . "\"\n\n";
	$humantriggeringrule	.= "	Annotations: textual_genetic_description \"" . clean_comment_string($phenotype_textual_description) . "\"\n\n";
	
	$textual_rule .= $textualtriggeringrule . "\n\n";
	$rule_owl .= $humantriggeringrule ."\n\n";
}
$report .= ("We have defined $nrules phenotype rules in the ontology.\n");

/**********************************
 ******* Generate disjoints *******
 **********************************/

$owl .= "\n#\n# Disjoints\n#\n\n";
$owl .= "# polymorphism disjoints\n";
$owl .= generateDisjointClassesOWL($polymorphism_disjoint_list);
$owl .= "# gene/allele disjoints\n";
$owl .= generateDisjointClassesOWL($gene_ids);

/*****************************
 ******* Write to disk *******
 *****************************/
$snp_list = array_unique($snp_list);
$results = "";
foreach($snp_list as $snp_element){
	$results .= $snp_element."\n";
}

file_put_contents($light_rule_file_location, $owl . $rule_owl); //light version of the ontology without subclasses of genotype marker variation.
file_put_contents($light_rule_demo_file_location, $owl . $rule_owl . file_get_contents($pharmacogenomic_CDS_demo_additions_file_location)); //light version of the ontology with the demo example.

file_put_contents($full_rule_file_location, $owl . $msc_owl . $rule_owl); // Full version of the ontology without textual description of the genomic and phenotype rules.
file_put_contents($full_rule_demo_file_location, $owl . $msc_owl . $rule_owl . file_get_contents($CDS_rule_demo_additions_file_location)); // Full version of the ontology without textual description of the genomic and phenotype rules, and with a the demo example.


file_put_contents($CDS_server_file_location, $owl . $msc_owl . $textual_rule); // Full version of the ontology but with only the textual description of the genomic and phenotype rules.
file_put_contents($CDS_server_demo_file_location, $owl . $msc_owl . $textual_rule . file_get_contents($CDS_rule_demo_additions_file_location)); // Full version of the ontology with only textual description of rules and the demo example.

file_put_contents($report_file_location, $report);
beep(2);
?> 