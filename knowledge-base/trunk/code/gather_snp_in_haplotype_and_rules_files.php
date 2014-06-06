<?php
require_once 'Classes/PHPExcel/IOFactory.php';
error_reporting(E_ALL);
date_default_timezone_set('Europe/London');

/***** INPUT FILES ******/
$haplotype_spreadsheet_file_location = "..\\data\\PharmGKB\\haplotype_spreadsheet_new_v2.xlsx";
$pharmacogenomics_decision_support_spreadsheet_file_location = "..\\data\\decision-support-rules\\Pharmacogenomics decision support spreadsheet_v2.xlsx";

/***** OUTPUT FILES ******/
$list_snps_file = "..\\data\\dbSNP\\list_snps.txt";

/************************
 * Read data from haplotype spreadsheet
 ************************/


$objPHPExcel = PHPExcel_IOFactory::load($haplotype_spreadsheet_file_location);

$list_rs = array();
foreach ($objPHPExcel->getWorksheetIterator() as $objWorksheet) {

	$worksheet_title = $objWorksheet->getTitle();
	print("worksheet = ".$worksheet_title."\n");
		
	$lastColumn = $objWorksheet->getHighestColumn();
	$lastColumn++;
	
	for ($column = 'E'; $column != $lastColumn; $column++) {
		$cell_head = $objWorksheet->getCell($column."1")->getValue();
		if(!isset($cell_head)) break;
		if(preg_match_all("/^rs/",$cell_head)){
			$list_rs[] = $cell_head;
		}
	}
}


/******************************************************************************
 ******************************************************************************/

 /**********************************************
 ****** Processing cds rules information ******
 **********************************************/
 
$objPHPExcel = PHPExcel_IOFactory::load($pharmacogenomics_decision_support_spreadsheet_file_location);

$objWorksheet = $objPHPExcel->getSheetByName("CDS rules");

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
	
	if(!isset($row_array['F'])){
		continue;		
	}
	$logical_description_of_genetic_attributes = $row_array['F'];
	if(preg_match("/rs\d+/",$logical_description_of_genetic_attributes,$tokens)){
		for($i=0;$i<count($tokens);$i++){
			$list_rs[] = $tokens[$i];
		}
	}
}

/**********************************************
 ****** Processing phenotype information ******
 **********************************************/
$objWorksheet = $objPHPExcel->getSheetByName("Phenotypes");
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
		continue;		
	}
	
	if(!isset($row_array['D'])){
		continue;
	}
	$phenotype_logical_statements = $row_array["D"];
	if(preg_match("/rs\d+/",$phenotype_logical_statements,$tokens)){
		for($i=0;$i<count($tokens);$i++){
			$list_rs[] = $tokens[$i];
		}
	}
	
}

$report = "";
$list_rs = array_unique($list_rs);
for($i=0;$i<count($list_rs);$i++){
	$report .= $list_rs[$i]."\n";
}

/*$list_snp = array();
for($i=0;$i<count($list_rs);$i++){
	$contains = false;
	for($j=0;$j<count($list_snp);$j++){
		if($list_snp[$j] == $list_rs[$i]){
			$contains = true;
			break;
		}
	}
	if(!$contains){
		$list_snp[] = $list_rs[$i];
		$report =$report . $list_rs[$i]."\n";
	}
}*/


file_put_contents($list_snps_file, $report);

?> 