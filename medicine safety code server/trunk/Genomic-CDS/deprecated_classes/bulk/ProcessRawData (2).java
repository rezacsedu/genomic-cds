package bulk;


/**
 * Application to process the raw data and calculate the statistics of the inferred patient's genotype axioms.
 * 
 * @author Jose Antonio Miñarro Giménez
 * @version 2.0
 * @date 15/09/2014
 */ 
public class ProcessRawData {

	/**
	 * Constructor of the class
	 * 
	 * @param args	It needs two parameters: (1) the folder that contains the rawdata genetic files to process; (2) The name of the ontology model; (3) the name of the output file where the results will be stored.
	 */
	public static void main(String[] args) {
		if(args.length < 3){
			System.err.println("ERROR: Wrong number of parameters.");
			System.err.println("Number of parameters = "+args.length);
			for(int i=0;i<args.length;i++){
				System.err.println("args["+i+"]="+args[i]);
			}
			System.out.println("usage: ProcessRawData.jar input_folder ontology_file output_file");
			System.exit(1);
		}

		//String input_folder = args[0];
		//String ontology_file = args[1];
		String output_file = args[2];
	
		//CoordinateExecution coordinator = new CoordinateExecution(ontology_file);
		//coordinator.execute(output_file,input_folder);
		
		StatisticsBulkResults sbr = new StatisticsBulkResults(output_file);
		sbr.calculateStatistics();		
		
		System.out.println("End");
	}
}
