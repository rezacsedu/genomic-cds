<?php
error_reporting(E_ALL);
require_once 'Classes/PHPExcel/IOFactory.php';
date_default_timezone_set('Europe/London');
/*
if(!isset($argv) || ($argc != 3)){
	print ("Error: The script needs two parameters:\n\tExample: parse_snp_vcf.php input.xlsx results.xlsx");
	exit;
}
$haplotype_spreadsheet_file_location = $argv[1];
$haplotype_spreadsheet_file_location_v2 = $argv[2];
*/

/*
 * Input file locations
 */
$haplotype_spreadsheet_file_location = "..\\data\\PharmGKB\\haplotype_spreadsheet_Auto_v1.xlsx";


/*
 * Output file locations
 */
$haplotype_spreadsheet_file_location_v2 = "..\\data\\PharmGKB\\haplotype_spreadsheet_AutoPattern_v1.xlsx";

/*
 * Initializing important variables
 */

/*
 * Functions
 */

 function value_match($string1, $string2){
	$array_1 = explode("/",$string1);
	$array_2 = explode("/",$string2);
	for($i=0;$i<count($array_1);$i++){
		$val_aux_1 = $array_1[$i];
		if($val_aux_1 == "*") return true;
		for($j=0;$j<count($array_2);$j++){
			$val_aux_2 = $array_2[$j];
			if($val_aux_2 == "*") return true;
			if($val_aux_1 == $val_aux_2){
				return true;
			}
		}
	}
	return false;
 }

 function clean_tagged($poly_value){
	$substitutions = array(" [tag]" => "");
	return strtr($poly_value, $substitutions);
 }

 
 function printArray($arrayVar){
	print("[");
	foreach($arrayVar as $item){
		print($item." ");
	}
	print("]\n");
 }
 
 
 function no_matched_pattern($basic_pattern,$basic_tagged_pattern,$array_polymorphisms, $array_tagged_poly){
	foreach($basic_tagged_pattern as $pattern){
		if(count($pattern)==0) continue;
		$match=true;
		for($i=0;$i<count($pattern);$i++){
			$value1 = $pattern[$i];
			$value2 = $array_polymorphisms[$i];
			if(value_match($value1,$value2) == false){
				$match=false;
				break;
			}
		}
		if($match){
			return false;
		}		
	}
	if(count($array_tagged_poly)>0){
		foreach($basic_pattern as $pattern){
			$match=true;
			for($i=0;$i<count($pattern);$i++){
				$value1 = $pattern[$i];
				$value2 = $array_tagged_poly[$i];
				if(value_match($value1,$value2) == false){
					$match=false;
					break;
				}
			}
			if($match){
				return false;
			}
		}
	}
	return true;
 }
 

 
/************************
 * Read and convert data from haplotype spreadsheet
 ************************/


$objPHPExcel = PHPExcel_IOFactory::load($haplotype_spreadsheet_file_location);

foreach ($objPHPExcel->getWorksheetIterator() as $objWorksheet) {
	
	$basic_pattern = array(); // Needed for defining the basic pattern to keep enabled in the model
	$basic_tagged_pattern = array();
	
	$worksheet_title = $objWorksheet->getTitle();
	
	// Skip sheets starting with "_" (can be used for sheets that need more work etc.)
	if (strpos($worksheet_title,"_") === 0) { 
		continue; 
	}
		
	print("worksheet = ".$worksheet_title."\n");
	
	$lastRow = $objWorksheet->getHighestRow();
	for ($row = 2; $row <= $lastRow; $row++) {
		$array_polymorphisms = array();
		$array_tagged_poly = array();
		$is_tagged = false;
		$lastColumn = $objWorksheet->getHighestColumn();
		$lastColumn++;
		for ($column = 'E'; $column != $lastColumn; $column++) {
			$cell_value =  $objWorksheet->getCell($column.$row)->getValue();
			if (strpos($cell_value,'[tag]') == true) {
				$is_tagged = true;
			}
			$array_polymorphisms[] = clean_tagged($cell_value);
		}
		
		for ($column = 'E'; $column != $lastColumn; $column++) {
			$cell_value =  $objWorksheet->getCell($column.$row)->getValue();			
			if (strpos($cell_value,'[tag]') == true) {
				$array_tagged_poly[] = clean_tagged($cell_value);
			}else{
				if($is_tagged){
					$array_tagged_poly[] = "*";
				}
			}
		}
		
		if(no_matched_pattern($basic_pattern,$basic_tagged_pattern,$array_polymorphisms,$array_tagged_poly)){
			$basic_tagged_pattern[] = $array_tagged_poly;
			$basic_pattern[] = $array_polymorphisms;
			
			$objWorksheet->setCellValue("A".$row,"");
		}else{
			$objWorksheet->setCellValue("A".$row,"disabled");
		}
	}
}	

$writer = PHPExcel_IOFactory::createWriter($objPHPExcel, 'Excel2007');
$writer->save($haplotype_spreadsheet_file_location_v2);


?> 