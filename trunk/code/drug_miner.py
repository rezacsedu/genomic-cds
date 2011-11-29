#!usr/bin/env/python
import MySQLdb

#MysQLdb modul is needed by the script
#open mysql connection
#change USERNAME. PASSWORD, DATABASE NAME
connection = MySQLdb.connect("localhost","root","username","dbname")
cursor = connection.cursor()

#read drug names line by line from CRC_drug_list.txt

FILE=open("CRC_drug_list.txt","r")
FILE2=open("mining_results1","w")
FILE3=open("mining_results2", "w")
for drug_name in FILE.readlines():
	drug_name=drug_name.strip()
	#query retreives details (e.g. brand name, pharmagkb id ...) of the drug in question
	query="select * from drugs where Name = '"+drug_name+"'"
	#query2 retreives all relationships (drug-drug, drug-gene) of the drug in question
	query2="select * from relationships where Entity1_Name = '"+drug_name+"' OR Entity2_Name = '"+drug_name+"'"
	
	cursor.execute(query)
	results = cursor.fetchall()
	#if the query retrieve a result
	if results:
		print "a result was found for ", drug_name
		for rows in results:
			for row in rows:
				row=row.strip()
				if (row.find("PA") != -1):
					#create a link to gene tests page of PharmaGKB
					tmp="http://www.pharmgkb.org/drug/"+row+"#subtab=34"
					FILE2.write(tmp)
				else:
					FILE2.write(row)
				FILE2.write("\t")
		FILE2.write("\n")
	else:
		print "no result for ", drug_name
		
	cursor.execute(query2)
	results = cursor.fetchall()
	if results:
		print "relationship/relationships was found for ", drug_name
		for rows in results:
			for row in rows:
				row=row.strip()
				FILE3.write(row)
				FILE3.write("\t")
			FILE3.write("\n")
	else:
		print "no result for ", drug_name
		
#close files
FILE.close()		
FILE2.close()
FILE3.close()	

#close mysql connection
connection.close()
