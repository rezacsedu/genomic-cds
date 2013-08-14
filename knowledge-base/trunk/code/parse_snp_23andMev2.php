<?php
error_reporting(E_ALL);
require_once 'Classes/PHPExcel/IOFactory.php';
date_default_timezone_set('Europe/London');

/*
 * Input file locations
 */
$haplotype_spreadsheet_file_location = "..\\data\\PharmGKB\\haplotype_spreadsheet.xlsx";


/*
 * Output file locations
 */
$haplotype_spreadsheet_file_location_v2 = "..\\data\\PharmGKB\\haplotype_spreadsheet_v3.xlsx";

/*
 * Initializing important variables
 */

/*
 * Functions
 */

function get_list_snps_for_allele($allele_name){
	$allele_snps_file = "..\\data\\PharmGKB\\allele_snps.txt";
	$results = array();
	$content_alleles_snps = file_get_contents($allele_snps_file);
	$content_alleles_snps = preg_split("/\n/", $content_alleles_snps);
	for($j = 0; $j<count($content_alleles_snps); ++$j){
		$row_allele_snps = $content_alleles_snps[$j];
		$row_allele_snps_list = preg_split("/\t/", $row_allele_snps);
		if( $row_allele_snps_list[0] == $allele_name){
			for($i = 1; $i < count($row_allele_snps_list); ++$i) {
				$results[]=$row_allele_snps_list[$i];
			}
		}
	}
	return $results;
}
 
 
/************************
 * Read and convert data from haplotype spreadsheet
 ************************/



$objPHPExcel = PHPExcel_IOFactory::load($haplotype_spreadsheet_file_location);

$allele_id_array = array(); // Needed for creating disjoints later on

foreach ($objPHPExcel->getWorksheetIterator() as $objWorksheet) {
	
	$homozygous_human_id_array = array(); // Needed for creating disjoints later on
	
	$worksheet_title = $objWorksheet->getTitle();
	
	// Skip sheets starting with "_" (can be used for sheets that need more work etc.)
	if (strpos($worksheet_title,"_") === 0) { 
		continue; 
	};
	$array_snps = get_list_snps_for_allele($worksheet_title);
	if(empty($array_snps)){		
		continue;
	}
	//print("worksheet = ".$worksheet_title."\n");
	$lastRow = $objWorksheet->getHighestRow();
	for ($row = 2; $row <= $lastRow; $row++) {
		$superClass_cell = $objWorksheet->getCell('C'.$row);
		$haplotype_cell = $objWorksheet->getCell('D'.$row);
		if($superClass_cell->getValue() == "*1" || $haplotype_cell->getValue() == "*1"){
			//print("matched C".$row." o D".$row."\n");
			$lastColumn = $objWorksheet->getHighestColumn();
			$lastColumn++;
			for ($column = 'E'; $column != $lastColumn; $column++) {
				$header_column = $objWorksheet->getCell($column."1");
				for($i=0;$i<count($array_snps);$i++){
					$label_snps = $array_snps[$i];
					if($label_snps == $header_column->getValue()){
						$cell_value =  $objWorksheet->getCell($column.$row)->getValue();
						if (strpos($cell_value,'[tag]') == false) {
							$objWorksheet->setCellValue($column.$row,$cell_value." [tag]");
							//print("Celda [$column,$row]= ".$cell_value." [tag]\n");
						}
					}
				}
			}
		}
	}
}	

$writer = PHPExcel_IOFactory::createWriter($objPHPExcel, 'Excel2007');
$writer->save($haplotype_spreadsheet_file_location_v2);

?> 