#!usr/bin/env/python
import MySQLdb

#MysQLdb modul is needed by the script
#open mysql connection
#change USERNAME. PASSWORD, DATABASE NAME
connection = MySQLdb.connect("localhost","root","gkn551","OMIM")
cursor = connection.cursor()

#read gene symbols line by line from human_genes.txt
disease_related_genes=[]
tmp=[]
FILE=open("human_genes_II.txt","r")
FILE2=open("mining_results","w")
for line in FILE.readlines():
	line=line.strip()
	line_replaced=line.replace("\t", ",")
	#split gene symbols if there is other aliases of the gene in question
	tmp=line_replaced.split(",")
	#make a search on the OMIM text bodies by using gene_symbols and their aliases. if the gene_symbol is found in text, cut the corresponding part of the text.
	for i in tmp:
		i=i.strip()
		gene_symbol=i
		if gene_symbol:
			i=" "+i
			#MIMno=114500 is the accession number of CRC
			query="SELECT * FROM omimtext WHERE MIMno = '114500' AND (text_body COLLATE latin1_bin LIKE '%"+i+" %' OR text_body COLLATE latin1_bin LIKE '%"+i+".%' OR text_body COLLATE latin1_bin LIKE '%"+i+",%' OR text_body COLLATE latin1_bin LIKE '%"+i+";%')"
			cursor.execute(query)
			results = cursor.fetchall()
			#if the query retrieve a result
			if results: 
				print "a result found for the gene ",gene_symbol
				for line in results:
					disease_related_genes.append(gene_symbol)
					#write the gene symbol if it exist in text body
					FILE2.write(gene_symbol)
					FILE2.write("\t")
					
					tmp2=line[1]
					tmp2=tmp2.split("||||")
					
					case1=" "+gene_symbol+" "
					case2=" "+gene_symbol+"."
					case3=" "+gene_symbol+","
					case4=" "+gene_symbol+";"
					
					for element in tmp2:
						if case1 in element:
							FILE2.write(element)
							FILE2.write("\t")
							#print element
						elif case2 in element:
							FILE2.write(element)
							FILE2.write("\t")
							#print element
						elif case3 in element:
							FILE2.write(element)
							FILE2.write("\t")
							#print element
						elif case4 in element:
							FILE2.write(element)
							FILE2.write("\t")
							#print element
					FILE2.write("\n")

FILE2.close()
FILE.close()
print disease_related_genes

#close mysql connection
connection.close()
