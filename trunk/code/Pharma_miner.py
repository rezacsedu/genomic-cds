#!usr/bin/env/python
import MySQLdb

#open mysql connection
connection = MySQLdb.connect("localhost","root","password","database")
cursor = connection.cursor()
#open the file including entrez gene ids of the genes in question
FILE=open("EntrezIDs.txt","r")
#create a file to write results
FILEresults=open("pharmgkbDB.txt", "w")
for line in FILE.readlines():
	line=line.strip()
	#extract pharmagkb ids from gene table for each entrez ids
	query="SELECT * FROM gene WHERE EntrezID='"+line+"'"
	#print query
	cursor.execute(query)
	results = cursor.fetchall()
	if results:
		for i in results:
			print i[0]
			print i[3]
			link="http://www.pharmgkb.org/gene/"+i[0]
			print link
			FILEresults.write(i[0])
			FILEresults.write("\t")
			FILEresults.write(i[4])
			FILEresults.write("\t")
			FILEresults.write(link)			
			FILEresults.write("\n")
	else:
		FILEresults.write("NA")
		FILEresults.write("\t")
		FILEresults.write("NA")
		FILEresults.write("\t")
		FILEresults.write("NA")
		FILEresults.write("\n")
		
#close EntrezIDs.txt file
FILE.close()	
#close pharmgkbDB.txt file
FILEresults.close()

#close mysql connection
connection.close()
