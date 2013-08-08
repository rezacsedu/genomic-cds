Example of execution:

Parameters:

input:		d:/Genomic-CDS/input				Input folder where the 23andMe files are stored. The files must have the sufix "23andme.txt"
ontology:	d:/Genomic-CDS/MSC_classes.ttl		The ontology that will be used to obtain the inferences.
output:		d:/Genomic-CDS/output/results		The file where the results of the execution are stored. The polymorphisms, alleles and rules of each patient were gathered. It also provides the lines processed, the matched snps and the wrong SNPs variations found in each file. The output file will have the .csv suffix.
nThreads:	3									Number of threads that will be used in the execution. In case of wrong number of threads, the default value is 3.

d:\> java -Xmx3072m -Xms3072m -jar d:/Genomic-CDS/Genomic-CDS.jar input ontology output nThreads