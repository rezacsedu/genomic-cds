package bulk;

import java.io.File;

 public class BulkClass {

	
	/**
	 * Application that defines a workflow to parse a bulk of patient's genotype files and to calculate their corresponding statistics.	 
	 *  
	 * @param args The params represent: (1) the directory of all input files; (2) the location of the ontology model; (3) the prefix of the output results; (4) The number of threads to be created.
	 * */
	public static void main(String[] args) {
		
		if(args.length < 4){
			System.err.println("ERROR: Wrong number of parameters.");
					
			System.err.println("Number of parameters = "+args.length);
			for(int i=0;i<args.length;i++){
				System.err.println("args["+i+"]="+args[i]);
			}
			System.out.println("usage: Genomic-CDS.jar 23AndMe_folder_input ontology_file_input statistics_file_output NumberOfThreads");
			System.exit(1);
		}
		
		String parsing_folder = args[0];
		String ontology_model = args[1];
		String output_results = args[2]+".csv";
		int nThreads=3;
		try{
			nThreads = Integer.parseInt(args[3]);
		}catch(Exception e){
			System.out.println("Warning: The number of threads is not correct default value of 3 was set.");
			nThreads=3;
		}
		
		File folder = new File(parsing_folder);
		if(!folder.isDirectory()){
			System.err.println("ERROR: The first parameter ("+parsing_folder+") is not a directory.");
			System.out.println("usage: Genomic-CDS.jar 23AndMe_folder_input ontology_file_input statistics_file_output NumberOfThreads");
			System.exit(1);
		}
		
		CoordinateExecution coordinator = new CoordinateExecution(ontology_model);
		coordinator.execute(output_results, folder, nThreads);
		coordinator.processRawData(output_results);
		
		System.out.println("End execution");
	}

}
