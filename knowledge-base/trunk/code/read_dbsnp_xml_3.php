<?php

$db_snp_xml_input_file_location = "C:\\Users\\m\Documents\\workspace\\safety-tag\\Data\\dbSNP\\core_rsid_data_from_dbsnp.xml";
$output_file_location_1 = "C:\\Users\\m\Documents\\workspace\\safety-tag\\Data\\";
$output_file_location_2 = "C:\\Users\\m\\Documents\\workspace\\medicine safety code server\\src\\";
$output_file_location_3 = "C:\\Users\\m\\TBCFreeWorkspace\\TMO\\ontology\\extensions\\";

function make_qname_safe($input_string) {
	$string = str_replace("(", "_", $input_string);
	$string = str_replace(")", "_", $string);
	return $string;
}

function make_allele_combination_rdf($genotype_class_qname, $decimal_code, $binary_code, $allele_1, $allele_2, $rs_id) {
	$variant_qname = "sc:genotype_rs" . $rs_id . "_variant_" . make_qname_safe($allele_1 . "_" . $allele_2);
	
    $rdf_fragment = $variant_qname . " \n" .
      	    "    a owl:Class; \n" .
      		"    rdfs:subClassOf $genotype_class_qname ; \n" .
			"    sc:decimal_code \"$decimal_code\"^^xsd:integer; \n" .
			"    sc:bit_code \"$binary_code\"; \n" .
			"    genomic:allele_1 \"$allele_1\"; \n" .
			"    genomic:allele_2 \"$allele_2\"; \n" .
			"    genomic:allele_1_and_2 \"" . $allele_1 . ";" . $allele_2 . "\"; \n" .
			"    rdfs:label \"rs" . $rs_id . "(" . $allele_1 . ";" . $allele_2 . ")\" ; \n" .
			"    genomic:criteria_syntax \"rs" . $rs_id . "(" . $allele_1 . ";" . $allele_2 . ")\" . \n\n";
    
    //generate dummy profile for testing 
    if ($decimal_code == 1) {
    	$GLOBALS['example_safety_code_profile_rdf'] .= "sc:this_profile sc:has_variant " . $variant_qname . " . \n\n" ;
    }
	return $rdf_fragment;
}

$xml = simplexml_load_file($db_snp_xml_input_file_location);
$prefixes= "
# baseURI: http://translationalmedicineontology.googlecode.com/svn/trunk/ontology/extensions/core_pharmacogenomics.ttl
# imports: http://www.w3.org/2001/sw/hcls/ns/transmed/tmo

@prefix tmo: <http://www.w3.org/2001/sw/hcls/ns/transmed/tmo/> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . 
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . 
@prefix owl: <http://www.w3.org/2002/07/owl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .

@prefix genomic: <http://translationalmedicineontology.googlecode.com/svn/trunk/ontology/extensions/core_pharmacogenomics.ttl#> .
@prefix sc: <http://safety-code.org/ontology/safety-code.ttl#> .

<http://translationalmedicineontology.googlecode.com/svn/trunk/ontology/extensions/core_pharmacogenomics.ttl>
      rdf:type owl:Ontology ;
      owl:imports <http://www.w3.org/2001/sw/hcls/ns/transmed/tmo> .
";

$allele_rdf = $prefixes;
$example_safety_code_profile_rdf = $prefixes;

$allele_rdf .=
"
genomic:criteria_syntax a owl:DatatypeProperty ;
rdfs:label \"criteria syntax\" .

genomic:variant_string a owl:DatatypeProperty ;
rdfs:label \"variant syntax\" .

genomic:dbsnp_rsid a owl:AnnotationProperty ;
rdfs:label \"dbSNP reference sequence ID\" .
";

$i = 0;
$position_in_base_2_string = 0;
$codes = array();

$example_safety_code_profile_rdf.= "sc:this_profile a sc:profile . \n\n" ;

foreach ($xml->Rs as $Rs) {
	
	/*
	 * Generate RDF for alleles
	* */
	
	$rs_id =  $Rs['rsId'];
	$genotype_class_qname = "genomic:genotype_rs" . $rs_id;
	$snp_class = $Rs['snpClass'];
	$observed_alleles = $Rs->Sequence->Observed;
	$gene_symbol = $Rs->Assembly->Component->MapLoc->FxnSet['symbol']; // TODO: This does not give results for a few Rs numbers
	$assembly_genome_build = $Rs->Assembly['genomeBuild'];
	$assembly_group_label = $Rs->Assembly['groupLabel'];
	$orient = $Rs->Assembly->Component->MapLoc['orient']; // TODO: Check if this works as intended
	
	$allele_rdf.= "\n\n";
	
	$allele_rdf .= $genotype_class_qname . " a owl:Class . \n";
	$allele_rdf .= $genotype_class_qname . " genomic:dbsnp_rsid \"rs" . $rs_id . "\" . \n";
	$allele_rdf .= $genotype_class_qname . " rdfs:seeAlso <http://bio2rdf.semanticscience.org:8006/describe/?url=http%3A%2F%2Fbio2rdf.org%2Fdbsnp%3Ars" . $rs_id . "> . \n";
	$allele_rdf .= $genotype_class_qname . " sc:rank \"" . $i . "\"^^xsd:integer . \n";
	$allele_rdf .= $genotype_class_qname . " genomic:assembly_genome_build \"" . $assembly_genome_build . "\" . \n";
	$allele_rdf .= $genotype_class_qname . " genomic:assembly_group_label \"" . $assembly_group_label . "\" . \n";
	$allele_rdf .= $genotype_class_qname . " genomic:symbol_of_associated_gene \"" . $gene_symbol . "\" . \n";
	$allele_rdf .= $genotype_class_qname . " genomic:orientation_on_reference_genome \"" . $orient . "\" . \n";
	
	
	$observed_alleles = preg_split("/\//", $observed_alleles);
	sort($observed_alleles, SORT_STRING);
		
	/*
	 * Generate RDF of possible genotypes (allele combinations) and their binary codes, add to code array
	 **/

	$bit_length = 0;
		
	switch ($Rs['snpClass']) {
		case "snp":
			$allele_rdf .= $genotype_class_qname . " rdfs:subClassOf <http://www.w3.org/2001/sw/hcls/ns/transmed/TMO_0038> . \n";
			break;
		case "in-del":
			$allele_rdf .= $genotype_class_qname . " rdfs:subClassOf <http://www.w3.org/2001/sw/hcls/ns/transmed/TMO_0225> . \n";
			break;
		default:
			$allele_rdf .= $genotype_class_qname . " rdfs:subClassOf <http://www.w3.org/2001/sw/hcls/ns/transmed/TMO_0172> . \n";
			print("Notice: Unrecognized variation class: " . $Rs['snpClass'] . "\n");
	}
	
	switch (count($observed_alleles)) {
	case 2: 
		$bit_length = 2;
		$allele_rdf .= $genotype_class_qname . " sc:bit_length \"" . $bit_length . "\"^^xsd:integer . \n";
		$allele_rdf .= $genotype_class_qname . " sc:position_in_base_2_string \"" . $position_in_base_2_string . "\"^^xsd:integer . \n";
		
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 0, "00", "null", "null", $rs_id);	
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 1, "01", $observed_alleles[0],  $observed_alleles[0], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 2, "10", $observed_alleles[0],  $observed_alleles[1], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 3, "11", $observed_alleles[1],  $observed_alleles[1], $rs_id);
		
		$position_in_base_2_string = $position_in_base_2_string + $bit_length;
		break;
	case 3:
		$bit_length = 3;
		$allele_rdf .= $genotype_class_qname . " sc:bit_length \"" . $bit_length . "\"^^xsd:integer . \n";
		$allele_rdf .= $genotype_class_qname . " sc:position_in_base_2_string \"" . $position_in_base_2_string . "\"^^xsd:integer . \n";
		
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 0, "000", "null", "null", $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 1, "001", $observed_alleles[0], $observed_alleles[0], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 2, "010", $observed_alleles[0], $observed_alleles[1], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 3, "011", $observed_alleles[0], $observed_alleles[2], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 4, "100", $observed_alleles[1], $observed_alleles[1], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 5, "101", $observed_alleles[1], $observed_alleles[2], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 6, "110", $observed_alleles[2], $observed_alleles[2], $rs_id);

		$position_in_base_2_string += $bit_length;
		break;
	case 4:
		$bit_length = 4;
		$allele_rdf .= $genotype_class_qname . " sc:bit_length \"" . $bit_length . "\"^^xsd:integer . \n";
		$allele_rdf .= $genotype_class_qname . " sc:position_in_base_2_string \"" . $position_in_base_2_string . "\"^^xsd:integer . \n";
		
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 0, "0000", "null", "null", $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 1, "0001", $observed_alleles[0], $observed_alleles[0], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 2, "0010", $observed_alleles[0], $observed_alleles[1], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 3, "0011", $observed_alleles[0], $observed_alleles[2], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 4, "0100", $observed_alleles[0], $observed_alleles[3], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 5, "0101", $observed_alleles[1], $observed_alleles[1], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 6, "0110", $observed_alleles[1], $observed_alleles[2], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 7, "0111", $observed_alleles[1], $observed_alleles[3], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 8, "1000", $observed_alleles[2], $observed_alleles[2], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 9, "1001", $observed_alleles[2], $observed_alleles[3], $rs_id);
		$allele_rdf .= make_allele_combination_rdf($genotype_class_qname, 10, "1010", $observed_alleles[3], $observed_alleles[3], $rs_id);

		$position_in_base_2_string += $bit_length;
		break;
	default:
		print("Warning: None of the options for observedAlleles matched. Are there less than 2 or more than 4 alleles?\n");
	}
	$i = $i + 1;
}

file_put_contents($output_file_location_1 . "core_pharmacogenomics.ttl", $allele_rdf);
//file_put_contents($output_file_location_1 . "example_safety_code_profile.ttl", $example_safety_code_profile_rdf);
file_put_contents($output_file_location_2 . "core_pharmacogenomics.ttl", $allele_rdf);
//file_put_contents($output_file_location_2 . "example_safety_code_profile.ttl", $example_safety_code_profile_rdf);
file_put_contents($output_file_location_3 . "core_pharmacogenomics.ttl", $allele_rdf);


// print($rdf);
// print($json);


?>