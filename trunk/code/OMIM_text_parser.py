#!usr/bin/env/python

#read txt file downloaded from OMIM ftp site
FILE=open("omim.txt","r")
#create parsed txt file for OMIM entries
FILEyaz=open("omim_parsed.txt","w")
control1=0

for line in FILE.readlines():
	line=line.strip()
	if control1==1:
		print line
		FILEyaz.write(line)
		FILEyaz.write("\t")
		control1=0
	#change abbreviations into their expanded form 
	elif line=="*RECORD*":
		print "\n"
		FILEyaz.write("\n")
	elif line=="*FIELD* NO":
		control1=1
	elif line=="*FIELD* TI":
		FILEyaz.write("TITLE ")
	elif line=="*FIELD* TX":
		FILEyaz.write("DESCRIPTION ")
	elif line=="*FIELD* CD":
		FILEyaz.write("CREATED ")	
	elif line=="*FIELD* CN":
		FILEyaz.write("CONTRIBUTORS ")
	elif line=="*FIELD* ED":
		FILEyaz.write("EDITED ")
	elif line=="*FIELD* SA":
		FILEyaz.write("ADDITIONAL REFERENCES ")
	elif line=="*FIELD* CS":
		FILEyaz.write("CLINICAL SYMPTOMS ")
	elif line=="*FIELD* MN":
		FILEyaz.write("MINIMIM ")
	elif line=="*FIELD* RF":
		FILEyaz.write("REFERENCE ")
	elif line=="*FIELD* AV":
		FILEyaz.write("ALLELIC VARIANT ")
	else:
	#rather than "\n" char. append "||"	
		FILEyaz.write(line)
		FILEyaz.write("||")

			
FILE.close()
FILEyaz.close()	
