<?php
error_reporting(E_ALL);
require_once 'Classes/PHPExcel/IOFactory.php';
date_default_timezone_set('Europe/London');

/*
 * Input file locations
 */
$db_snp_xml_input_file_location = "..\\data\\dbSNP\\core_rsid_data_from_dbsnp.xml";
$pharmacogenomic_CDS_base_file_location = "..\\ontology\\genomic-cds_base.owl";
$MSC_classes_base_file_location = "..\\ontology\\MSC_classes_base.owl";
$haplotype_spreadsheet_file_location = "..\\data\\PharmGKB\\haplotype_spreadsheet_vAuto.xlsx";
$pharmacogenomics_decision_support_spreadsheet_file_location = "..\\data\\decision-support-rules\\Pharmacogenomics decision support spreadsheet.xlsx";
$pharmacogenomic_CDS_demo_additions_file_location = "..\\ontology\\genomic-cds_demo_additions.owl";

/*
 * Output file locations
 */
$pharmacogenomic_CDS_file_location = "..\\ontology\\genomic-cds.owl";
$MSC_classes_file_location = "..\\ontology\\MSC_classes.owl";
$report_file_location = "..\\ontology\\generate_genomic_CDS.report.txt";
$pharmacogenomic_CDS_demo_file_location = "..\\ontology\\genomic-cds-demo.owl";

/*
 * Initializing important variables
 */
$owl = file_get_contents($pharmacogenomic_CDS_base_file_location) . "\n\n\n"; // Content of main ontology (pharmacogenomic_CDS.owl) will be appended to this variable

$msc_owl = file_get_contents($MSC_classes_base_file_location) . "\n\n\n"; // Content of additional classes for encoding/decoding Medicine Safety Codes (MSC_classes.owl) will be appended to this variable

$report = "-- Report -- \n\n"; // Content of Report and error log (generate_genomic_CDS.report.txt) will be appended to this variable

$valid_polymorphism_variants = array(); // List of valid polymorphism variants from dbSNP (used to find errors and orientation mismatches)


/*
 * Functions
 */

function generate_assay_annotations($rsid_string){
	$owl="";
	
	$search = array("\r\n", "\n");
	
	$snps_covered_by_23andMe_v2 = "..\\data\\assay-information\\SNPs covered by 23andMe v2.txt";
	$snps_covered_by_23andMe_v3 = "..\\data\\assay-information\\SNPs covered by 23andMe v3.txt";
	$snps_covered_by_Affimetrix_DMET_chip = "..\\data\\assay-information\\SNPs covered by Affymetrix DMET chip - PMID 20217574.txt";
	$snps_covered_by_University_of_Florida_and_Standford_chip = "..\\data\\assay-information\\SNPs covered by University of Florida and Stanford University chip - PMID 22910441.txt";
	
	
	$handle = fopen($snps_covered_by_23andMe_v2, 'r');
	if ($handle) {
		while (!feof($handle)) {
			$line = fgets($handle);
			$line = str_replace($search, $replace='', $line);
			if($line == $rsid_string){
				$owl .= "\tAnnotations: can_be_tested_with 23andMe_v2 \n";
				break;
			}
		}
		fclose($handle);
	}
	
	$handle = fopen($snps_covered_by_23andMe_v3, 'r');
	if ($handle) {
		while (!feof($handle)) {
			$line = fgets($handle);
			$line = str_replace($search, $replace='', $line);
			if($line == $rsid_string){
				$owl .= "\tAnnotations: can_be_tested_with 23andMe_v3 \n";
				break;
			}
		}
		fclose($handle);
	}
		
	$handle = fopen($snps_covered_by_Affimetrix_DMET_chip, 'r');
	if ($handle) {
		while (!feof($handle)) {
			$line = fgets($handle);
			$line = str_replace($search, $replace='', $line);
			if($line == $rsid_string){
				$owl .= "\tAnnotations: can_be_tested_with Affymetrix_DMET_chip \n";
				break;
			}
		}
		fclose($handle);
	}
	
	$handle = fopen($snps_covered_by_University_of_Florida_and_Standford_chip, 'r');
	if ($handle) {
		while (!feof($handle)) {
			$line = fgets($handle);
			$line = str_replace($search, $replace='', $line);
			if($line == $rsid_string){
				$owl .= "\tAnnotations: can_be_tested_with University_of_Florida_and_Stanford_University_chip \n";
				break;
			}
		}
		fclose($handle);
	}
	
	return $owl;
}
 
function beep ($int_beeps = 1) {
	$string_beeps = "";
    for ($i = 0; $i < $int_beeps; $i++): $string_beeps .= "\x07"; endfor;
    isset ($_SERVER['SERVER_PROTOCOL']) ? false : print $string_beeps;
} 
 
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
			"#" => "_hash"
	);
	return strtr($string, $substitutions);
}

function flipOrientationOfStringRepresentation($string) {
	$substitutions = array(
			"A" => "T", 
			"T" => "A", 
			"C" => "G", 
			"G" => "C"
	);
	return strtr($string, $substitutions);
}

function generateDisjointClassesOWL($id_array) {
	$owl = "";
	if (empty($id_array) == false) {
		$owl .= "Class: " . $id_array[0] . "\n";  // TODO: This should not be necessary. It is a fix for a bug in the OWLAPI/Protege Manchester Syntax parser (The last class before the DisjointClasses frame is taken into the disjoint). 
		$owl .= "DisjointClasses:" ."\n";
		$owl .= implode(",", $id_array) . "\n\n";
	}
	return $owl;
}

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
//			"    Annotations: sc:decimal_code \"$decimal_code\"^^xsd:integer \n" . //TODO: Remove
			"    Annotations: sc:bit_code \"$binary_code\" \n" .
			"    Annotations: rdfs:label \"human with rs" . $rs_id . "(" . $allele_1 . ";" . $allele_2 . ")\"  \n" .
			"    Annotations: sc:criteria_syntax \"rs" . $rs_id . "(" . $allele_1 . ";" . $allele_2 . ")\"  \n\n";
	return $owl_fragment;
}

/************************
 * Add version information (for reasons of simplicity, we are using the date of generation of the ontology for versioning)
************************/

$owl .= "\n\n";
$owl .=  "Ontology: <http://www.genomic-cds.org/ont/genomic-cds.owl>\n";
$owl .=  "    Annotations: owl:versionInfo \"" . date("Y/m/d") . "\"\n";


/************************
 * Read and convert dbSNP data
 ************************/

//Read snps processing chips
//$snps_23andMe_v2 = file_get_contents($snps_covered_by_23andMe_v2);
//$snps_23andMe_v2 = preg_split("/[\n|\s]/",$snps_23andMe_v2);
//$snps_23andMe_v3 = file_get_contents($snps_covered_by_23andMe_v3);
//$snps_23andMe_v3 = preg_split("/[\n|\s]/",$snps_23andMe_v3);
//$snps_Affimetrix_DMET_chip = file_get_contents($snps_covered_by_Affimetrix_DMET_chip);
//$snps_Affimetrix_DMET_chip = preg_split("/[\n|\s]/",$snps_Affimetrix_DMET_chip);
//$snps_University_of_Florida_and_Standford_chip = file_get_contents($snps_covered_by_University_of_Florida_and_Standford_chip);
//$snps_University_of_Florida_and_Standford_chip = preg_split("/[\n|\s]/",$snps_University_of_Florida_and_Standford_chip);

$owl .= "\n\n#\n# dbSNP data\n#\n\n";

print("Processing dbSNP data" . "\n");

$xml = simplexml_load_file($db_snp_xml_input_file_location);

$polymorphism_disjoint_list = array();
$i = 0;
$position_in_base_2_string = 0; // TODO: Remove?

foreach ($xml->Rs as $Rs) {
	$rs_id =  $Rs['rsId'];
	$human_with_genotype_at_locus = "human_with_genotype_at_rs" . $rs_id;
	$snp_class = $Rs['snpClass'];
	$observed_alleles = $Rs->Sequence->Observed;
	
	//** Get the right Assembly element with reference=true **//
	$fxn_sets = null; //$Rs->Assembly->Component->MapLoc->FxnSet;
	$assembly_genome_build = null; //$Rs->Assembly['genomeBuild'];
	$assembly_group_label = null; //$Rs->Assembly['groupLabel'];
	$orient = null; //$Rs->Assembly->Component->MapLoc['orient']; 
	
	
	$assembly_sets = $Rs->Assembly;
	if(isset($assembly_sets)){
		foreach($assembly_sets as $assembly_set) {
			if(isset($assembly_set['reference']) && $assembly_set['reference'] == true){
				$assembly_genome_build = $assembly_set['genomeBuild'];
				$assembly_group_label = $assembly_set['groupLabel'];
				$component_sets = $assembly_set->Component;
				if(isset($component_sets)){
					foreach($component_sets as $component_set){
						$maploc_sets = $component_set->MapLoc;
						if(isset($maploc_sets)){
							foreach($maploc_sets as $maploc_set){
								$orient=$maploc_set['orient'];
								$fxn_sets = $maploc_set->FxnSet;
							}
						}
					}
				}
			}
		}
	}
	if(!isset($fxn_sets) || !isset($assembly_genome_build) || !isset($assembly_group_label) || !isset($orient)){
		print($i." Not processed.\n");
		continue;
	}
	
	// Add gene symbols in this entry to array.
	foreach ($fxn_sets as $fxn_set) {
		$gene_ids[] = make_valid_id($fxn_set["symbol"]);
	}
	
	// Normalize orientation to the plus strand.
	if ($orient == "reverse") {
		$observed_alleles = flipOrientationOfStringRepresentation($observed_alleles);
	}
		
	$observed_alleles = preg_split("/\//", $observed_alleles);
	sort($observed_alleles, SORT_STRING);
	
	// Create OWL for possible genotypes

	$class_id = "rs" . $rs_id;
	
	$owl .= "Class: " . $class_id . "\n";
	$owl .= "    SubClassOf: polymorphism" . "\n";
	$owl .= "    Annotations: rsid \"rs" . $rs_id . "\"\n";
	$owl .= "    Annotations: rdfs:label \"rs" . $rs_id . "\"\n";
	$owl .= generate_assay_annotations($class_id);
	
	$fxn_symbols=null;
	foreach ($fxn_sets as $fxn_set) {
		$fxn_symbols[] = make_valid_id($fxn_set["symbol"]);
	} 
	if(isset($fxn_symbols)){
		$fxn_symbols = array_unique($fxn_symbols);
		foreach ($fxn_symbols as $fxn_symbol){
			$owl .= "    Annotations: relevant_for " .$fxn_symbol. "\n";
		}
	}
	$owl .= "    Annotations: rdfs:seeAlso \<http://bio2rdf.org/dbsnp:rs" . $rs_id . ">\n"; 
	$owl .= "    Annotations: dbsnp_orientation_on_reference_genome \"" . $orient . "\" \n\n";
	$polymorphism_disjoint_list [] = $class_id;
	
	$owl .= "Class: human \n";
	$owl .= "    SubClassOf: has exactly 2 " . $class_id . "\n\n";
	
	$polymorphism_variant_disjoint_list = array();
	foreach ($observed_alleles as $observed_allele) {
		$variant_class_id = $class_id . "_" . make_valid_id($observed_allele);
		$owl .= "Class: " . $variant_class_id . "\n";
		$owl .= "    SubClassOf: " . $class_id . "\n";
		$owl .= "    Annotations: rdfs:label \"" . $variant_class_id . "\" \n\n";
		$polymorphism_variant_disjoint_list[] = $variant_class_id;
		 
		$valid_polymorphism_variants[] = $variant_class_id;
	}	
	
	$owl .= generateDisjointClassesOWL($polymorphism_variant_disjoint_list);
	
	// Generate Medicine Safety Code classes
	
	$msc_owl .= "Class: sc:$human_with_genotype_at_locus \n";
	//$msc_owl .= "    SubClassOf: human \n";
	$msc_owl .= "    SubClassOf: human_with_genotype_marker \n";
	$msc_owl .= "    Annotations: rsid \"rs" . $rs_id . "\" \n";
	$msc_owl .= "    Annotations: sc:rank \"" . $i . "\"^^xsd:integer \n";
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
	
	switch (count($observed_alleles)) {
		case 2:
			$bit_length = 2;
			$msc_owl .= "    Annotations: sc:bit_length \"" . $bit_length . "\"^^xsd:integer \n";
			$msc_owl .= "    Annotations: sc:position_in_base_2_string \"" . $position_in_base_2_string . "\"^^xsd:integer \n\n";
	
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 0, "00", "null", "null", $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 1, "01", $observed_alleles[0],  $observed_alleles[0], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 2, "10", $observed_alleles[0],  $observed_alleles[1], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 3, "11", $observed_alleles[1],  $observed_alleles[1], $rs_id);
			$msc_owl .= "\n";
			
			$position_in_base_2_string = $position_in_base_2_string + $bit_length;
			break;
		case 3:
			$bit_length = 3;
			$msc_owl .= "    Annotations: sc:bit_length \"" . $bit_length . "\"^^xsd:integer \n";
			$msc_owl .= "    Annotations: sc:position_in_base_2_string \"" . $position_in_base_2_string . "\"^^xsd:integer \n\n";
	
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 0, "000", "null", "null", $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 1, "001", $observed_alleles[0], $observed_alleles[0], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 2, "010", $observed_alleles[0], $observed_alleles[1], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 3, "011", $observed_alleles[0], $observed_alleles[2], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 4, "100", $observed_alleles[1], $observed_alleles[1], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 5, "101", $observed_alleles[1], $observed_alleles[2], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 6, "110", $observed_alleles[2], $observed_alleles[2], $rs_id);
			$msc_owl .= "\n";
			
			$position_in_base_2_string += $bit_length;
			break;
		case 4:
			$bit_length = 4;
			$msc_owl .= "    Annotations: sc:bit_length \"" . $bit_length . "\"^^xsd:integer \n";
			$msc_owl .= "    Annotations: sc:position_in_base_2_string \"" . $position_in_base_2_string . "\"^^xsd:integer \n\n";
	
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 0, "0000", "null", "null", $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 1, "0001", $observed_alleles[0], $observed_alleles[0], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 2, "0010", $observed_alleles[0], $observed_alleles[1], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 3, "0011", $observed_alleles[0], $observed_alleles[2], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 4, "0100", $observed_alleles[0], $observed_alleles[3], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 5, "0101", $observed_alleles[1], $observed_alleles[1], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 6, "0110", $observed_alleles[1], $observed_alleles[2], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 7, "0111", $observed_alleles[1], $observed_alleles[3], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 8, "1000", $observed_alleles[2], $observed_alleles[2], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 9, "1001", $observed_alleles[2], $observed_alleles[3], $rs_id);
			$msc_owl .= make_safety_code_allele_combination_owl($human_with_genotype_at_locus, 10, "1010", $observed_alleles[3], $observed_alleles[3], $rs_id);
			$msc_owl .= "\n";
			
			$position_in_base_2_string += $bit_length;
			break;
		default:
			$report .= ("WARNING: There  are less than 2 or more than 4 alleles for $class_id -- Medicine Safety Code class was not generated.\n");
	}
	$i = $i + 1;
}

$gene_ids = array_unique($gene_ids);

foreach ($gene_ids as $gene_id) {
	$owl .= "Class: " . $gene_id . "\n";
	$owl .= "    SubClassOf: allele \n";
	$owl .= "    Annotations: rdfs:label \"" . $gene_id . "\"\n\n";
}

$owl .= "Class: human" . "\n";
foreach ($gene_ids as $gene_id) {
	$owl .= "    SubClassOf: has exactly 2 " . $gene_id . "\n";
}

/************************
 * Read and convert data from haplotype spreadsheet
 ************************/

$owl .= "\n\n#\n# Data from haplotype spreadsheet\n#\n\n";

$objPHPExcel = PHPExcel_IOFactory::load($haplotype_spreadsheet_file_location);

$allele_id_array = array(); // Needed for creating disjoints later on

foreach ($objPHPExcel->getWorksheetIterator() as $objWorksheet) {
	
	$homozygous_human_id_array = array(); // Needed for creating disjoints later on
	
	$worksheet_title = $objWorksheet->getTitle();
	
	//print("Processing haplotype spreadsheet " . $worksheet_title . "\n");
	// Array to record the disjoints haplotypes
	$haplotypes_disjoints = array();
	
	// Skip sheets starting with "_" (can be used for sheets that need more work etc.)
	if (strpos($worksheet_title,"_") === 0) { 
		continue; 
	};
	
	$header_array = array();
	
	foreach ($objWorksheet->getRowIterator() as $row) {
		$row_array = array();
		$error_during_processing = false;
		
		// Special processing of first row (header)
		if($row->getRowIndex() == 1) {
			$cellIterator = $row->getCellIterator();
			
			// TODO: decide if setIterateOnlyExistingCells should be enabled or not
			// $cellIterator->setIterateOnlyExistingCells(true);
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
			$allele_status = $row_array['A'];
		}else{
			$allele_status = "enabled";
		}
		$gene_label = $row_array['B'];
		$gene_id = make_valid_id($gene_label);
		$allele_label = $row_array['B'] . " " . $row_array['D'];
		$allele_id = make_valid_id($allele_label);
	
		$human_label = "human with " . $allele_label;
		$human_id = make_valid_id($human_label);
		$human_homozygous_label = "human with homozygous " . $allele_label;
		$human_homozygous_id = make_valid_id($human_homozygous_label);
		
		$allele_polymorphism_variants = array();
		$allele_tag_polymorphism_variants = array();
		
	
		foreach($row_array as $key=>$value) {
			// Skip first four columns 
			if (($key == "A") or ($key == "B") or ($key == "C") or ($key == "D")) continue; 
			
			$allele_polymorphism_variant = make_valid_id($header_array[$key] . "_" . trim(str_replace("[tag]", "", $row_array[$key]))); // e.g. "rs12345_A"
			
			// Report an error if the allele polymorphism variant was not already generated during the dbSNP conversion (we want to make sure everything matches dbSNP. If it does not, this is an indication of an error in the data).
			if (in_array($allele_polymorphism_variant, $valid_polymorphism_variants) == false) {
				$report .= "ERROR: Polymorphism variant \"" . $allele_polymorphism_variant . "\" in allele " . $allele_label . " does not match dbSNP. Skipping conversion for the entire allele." . "\n";
				$error_during_processing = true;
			}
			
			// Add id of polymorphism to array
			$allele_polymorphism_variants[] = $allele_polymorphism_variant;
				
			if (strpos($value,'[tag]') !== false) {
				// Add id of tagging polymorphism to tag array
				$allele_tag_polymorphism_variants[] = $allele_polymorphism_variant;
			}			
		}
		
		// CONTINUE if error occured (i.e., don't add any OWL expressions at all for this row)
		if ($error_during_processing) { continue; }
		
		$allele_id_array[] = $allele_id;
		$homozygous_human_id_array[] = $human_homozygous_id;
		
		/* 
		 * Allele class basics
		 */
		
		// If cell in superclass column is empty...
		if (isset($row_array['C']) == false) {
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
		}
		
		/*
		 * Rules for (at least) heterozygous polymorphisms
		 */ 
		
		$owl .= "Class: " . $human_id . "\n";
		$owl .= "SubClassOf: human_with_genetic_polymorphism" . "\n";
		$owl .= "Annotations: rdfs:label \"" . $human_label . "\"\n";
		
		if (!isset($row_array['A']) || $row_array['A'] ==! "disabled") {
		
			// If there are polymorphism variants...
			if (empty($allele_polymorphism_variants) == false) {
				$owl .= "SubClassOf:" . "\n";
				// TODO: Introduce logic for phasing (if it ever works...)
				//$owl .= "has some (" . implode(" that (taken_by some " . $allele_id . ")) and has some (", $allele_polymorphism_variants) . " that (taken_by some " . $allele_id . "))";
				$owl .= "has some " . implode(" and has some ", $allele_polymorphism_variants);
				$owl .= "\n\n";
			}
			else {
				$report .= "WARNING: No polymorphism variants found at all for " . $allele_id . "\n";
			}

			$owl .= "SubClassOf:" . "\n";
			$owl .= "has some " . $allele_id . "\n\n";
			
			// If there are tagging polymorphism variants...
			if (empty($allele_tag_polymorphism_variants) == false) {
				$owl .= "EquivalentTo:" . "\n";
				$owl .= "has some " . implode(" and has some ", $allele_tag_polymorphism_variants);
				$owl .= "\n\n";
			}
			else {
				$report .= "INFO: No tagging polymorphism variants found for " . $allele_id . "\n";
			}
		}
		
		
		/*
		 * Rules for homozygous polymorphisms and alleles
		 */
		
		$owl .= "Class: " . $human_homozygous_id . "\n";
		$owl .= "SubClassOf: human_with_genetic_polymorphism" . "\n";
		$owl .= "Annotations: rdfs:label \"" . $human_homozygous_label . "\" \n";
		
		if (!isset($row_array['A']) || $row_array['A'] ==! "disabled") {
			// If there are tagging polymorphism variants...
			if (empty($allele_tag_polymorphism_variants) == false) {
				$owl .= "EquivalentTo:" . "\n";
				$owl .= "has exactly 2 " . implode(" and has exactly 2 ", $allele_tag_polymorphism_variants);
				$owl .= "\n\n";
			}
			
			$owl .= "EquivalentTo:" . "\n";
			$owl .= "has exactly 2 " . $allele_id . "\n\n";
		}
	}
	
	
	// Produce disjoints between alleles variants
	foreach($haplotypes_disjoints as $group_alleles_disjoints){
		/*print("disjoints->");
		foreach($group_alleles_disjoints as $allele_disjoint){
			print($allele_disjoint." ");
		}
		print("\n");*/
		$owl .=	generateDisjointClassesOWL($group_alleles_disjoints);
	}
	
	
	// NOTE: Disjoints between underdefined/overlapping alleles produce unsatisfiable homozygous humans.
	$owl .= "# homozygous human disjoints\n";
	$owl .= generateDisjointClassesOWL($homozygous_human_id_array);
}

/************************
 * Read and convert data from pharmacogenomics decision support table
************************/

$owl .= "\n\n#\n# Pharmacogenomics decision support table data\n#\n\n";

$objPHPExcel = PHPExcel_IOFactory::load($pharmacogenomics_decision_support_spreadsheet_file_location);
$objWorksheet = $objPHPExcel->getSheetByName("CDS rules");

$drug_ids = array();

foreach ($objWorksheet->getRowIterator() as $row) {
	
	$row_array = array();
	
	// Skip first row
	if($row->getRowIndex() == 1) { continue; }
	
	// Processing of other rows (except the first) from here on
	$cellIterator = $row->getCellIterator();
	
	// TODO: decide if setIterateOnlyExistingCells should be enabled or not
	//$cellIterator->setIterateOnlyExistingCells(true);
	
	foreach ($cellIterator as $cell) {
		$row_array[$cell->getColumn()] = $cell->getCalculatedValue();
	}
	if(!isset($row_array['A'])){
		continue;		
	}
	$human_class_label = $row_array['A'];
	
	if(!isset($row_array['C'])){
		continue;		
	}
	$drug_label = $row_array['C'];
	
	if(!isset($row_array['F'])){
		continue;		
	}
	$logical_description_of_genetic_attributes = $row_array['F'];
	
	if(!isset($row_array['G'])){
		continue;		
	}
	$recommendation_in_english = $row_array['G'];
	
	// Skip processing if not all required data items are present
	if ($human_class_label == ""
			or $logical_description_of_genetic_attributes == ""
			or $recommendation_in_english == "") {
		$report .= "NOTE: Not all required values were found in the pharmacogenomics decision support spreadsheet row " . $row->getRowIndex() . ", skipping conversion of this row.\n";
		continue;
	}
			
	$owl .= "Class: " . make_valid_id($human_class_label) . "\n";
	$owl .= "    SubClassOf: human_triggering_CDS_rule" . "\n";
	$owl .= "    Annotations: rdfs:label \"" . $human_class_label . "\"\n";
	if ($drug_label ==! "") {
		$owl .= "    Annotations: relevant_for " . make_valid_id($drug_label) . "\n";
		$drug_labels[] = $drug_label;
	}
	$owl .= "	EquivalentTo: " . preg_replace('/\s+/', ' ', trim($logical_description_of_genetic_attributes)) . "\n";
	$owl .= "	Annotations: CDS_message \"" . addslashes($recommendation_in_english) . "\"\n\n";	
}

$drug_labels = array_unique($drug_labels);
foreach ($drug_labels as $drug_label) {
	$owl .= "Class: " . make_valid_id($drug_label) . "\n";
	$owl .= "    Annotations: rdfs:label \"" . $drug_label . "\"\n";
	$owl .= "    SubClassOf: drug" . "\n\n";
}

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
	$owl .= "    Annotations: rdfs:seeAlso <" . $external_URL . ">\n";
	$owl .= "    Annotations: rdfs:comment \"" . $comment . "\"\n\n";
}

/************************
 * Generate disjoints
************************/

$owl .= "\n#\n# Disjoints\n#\n\n";
$owl .= "# polymorphism disjoints\n";
$owl .= generateDisjointClassesOWL($polymorphism_disjoint_list);
$owl .= "# gene/allele disjoints\n";
$owl .= generateDisjointClassesOWL($gene_ids);

/************************
 * Write to disk
************************/

file_put_contents($pharmacogenomic_CDS_file_location, $owl);
file_put_contents($MSC_classes_file_location, $owl . $msc_owl); // $owl and $msc_owl are merged
file_put_contents($pharmacogenomic_CDS_demo_file_location, $owl . file_get_contents($pharmacogenomic_CDS_demo_additions_file_location));
file_put_contents($report_file_location, $report);

beep(2);
?> 