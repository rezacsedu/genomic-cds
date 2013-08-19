package bulk;

public class ProcessRawData {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 2){
			System.err.println("ERROR: Wrong number of parameters.");
			System.err.println("Number of parameters = "+args.length);
			for(int i=0;i<args.length;i++){
				System.err.println("args["+i+"]="+args[i]);
			}
			System.out.println("usage: ProcessRawData.jar rawdata_file_input ontology_file_input ");
			System.exit(1);
		}

		String input_rawdata = args[0];
		String ontology_model = args[1];


		CoordinateExecution coordinator = new CoordinateExecution(ontology_model);
		coordinator.processRawData(input_rawdata);
		System.out.println("End");
	}

}
