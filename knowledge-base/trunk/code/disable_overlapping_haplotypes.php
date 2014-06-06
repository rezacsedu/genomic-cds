<?php
require_once 'Classes/PHPExcel/IOFactory.php';
error_reporting(E_ALL);
date_default_timezone_set('Europe/London');

/***** INPUT FILES ******/
$haplotype_spreadsheet_file_location = "..\\data\\PharmGKB\\haplotype_spreadsheet_disabled_overlappings.xlsx";
$snps_not_in_dbSNP_file_location = "..\\data\\dbSNP\\list_snps_not_in_dbSNP.txt";

/***** OUTPUT FILES ******/
$haplotype_spreadsheet_file_location_v2 = "..\\data\\PharmGKB\\haplotype_spreadsheet_disabled_overlappings.xlsx";;

/*
 * Functions
 */
  
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

//Find overlapping haplotypes descriptions
function overlaps($array1,$array2){
	$overlaps = true;
	if(count($array2) <= 1) return true;
	if(count($array1) == 0) return false;
	for($i=0;$i<count($array1);$i++){
		for($j=1; $j<count($array2);$j++){
			$overlaps = true;
			if(($array1[$i][$j] != $array2[$j]) && ($array1[$i][$j] != "*") && ($array2[$j] != "*")){
				$overlaps = false;				
				break;
			}
		}
		if($overlaps) return true;
	}
	return $overlaps;
}

/*function overlaps($array1,$array2){
	if(count($array2) <= 1) return true;
	if(count($array1) == 0) return false;
	
	$total_diff = 2;
	
	for($i=0;$i<count($array1);$i++){
		$current_diff = 0;
		for($j=1; $j<count($array2);$j++){
			if(($array1[$i][$j] != $array2[$j]) && ($array1[$i][$j] != "*") && ($array2[$j] != "*")){
				$current_diff++;
			}
		}
		if($current_diff < $total_diff) return true;
	}
	return false;
}*/

 
/************************
 * Read data from haplotype spreadsheet
 ************************/

$snps_not_in_dbSNP = read_file_into_array($snps_not_in_dbSNP_file_location); //Read the file that contains the SNPs to skip during the processing of dbSNP file.

$objPHPExcel = PHPExcel_IOFactory::load($haplotype_spreadsheet_file_location);
foreach ($objPHPExcel->getWorksheetIterator() as $objWorksheet) {
	$worksheet_title = $objWorksheet->getTitle();
	print("worksheet = ".$worksheet_title."\n");
	
	$array_polymorphisms = array();
	$lastRow = $objWorksheet->getHighestRow();
	for ($row = 2; $row <= $lastRow; $row++) {
		$lastColumn = $objWorksheet->getHighestColumn();
		$lastColumn++;
		
		$array_variant = array();
		$array_variant[0] = $objWorksheet->getCell("D".$row)->getValue();

		$i = 1;
		for ($column = 'E'; $column != $lastColumn; $column++) {
			$cell_head = $objWorksheet->getCell($column."1")->getValue();
			if(preg_match_all("/^rs/",$cell_head) && !in_array($cell_head,$snps_not_in_dbSNP)){
				$cell_value =  $objWorksheet->getCell($column.$row)->getValue();
				$cell_value = trim(str_replace("[tag]","",$cell_value));
				$cell_value = trim(str_replace("del","D",$cell_value));
								
				if(empty($cell_value) || !(preg_match_all("/^[ACGTD]+$/",$cell_value) || preg_match_all("/^\([ACGT]+\)\d+$/",$cell_value))){
					$cell_value = "*";
				}
				$array_variant[$i] = $cell_value;
				$objWorksheet->setCellValue($column.$row,$cell_value);
				
				$i++;
			}
		}
		if(overlaps($array_polymorphisms,$array_variant)){
			$objWorksheet->setCellValue("A".$row,"disabled");
		}else{
			$objWorksheet->setCellValue("A".$row,"");
			$array_polymorphisms[count($array_polymorphisms)] = $array_variant;
		}
	}
}


$writer = PHPExcel_IOFactory::createWriter($objPHPExcel, 'Excel2007');
$writer->save($haplotype_spreadsheet_file_location_v2);


?> 