import java.io.BufferedReader;
import java.io.FileReader;


public class demoPrueba {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			BufferedReader br = new BufferedReader(new FileReader("D:/workspace/Genomic CDS/knowledge-base/trunk/ontology/genomic-cds_demo_additions_new_v2.owl"));
			String linea = "";
			while((linea=br.readLine())!=null){
				if(linea.contains("has ")){
					String variant1 = "";
					String variant2 = "";
					String rsnumber = "";
					if(linea.contains("has exactly 2")){
						linea = linea.substring(linea.indexOf("2")+1);
						rsnumber = linea.substring(0,linea.indexOf("_"));
						variant1 = linea.substring(linea.indexOf("_")+1,linea.indexOf(","));
						variant2 = variant1;
					}else{
						if(linea.contains("has some")){
							linea = linea.substring(linea.indexOf("some ")+5);
							rsnumber = linea.substring(0,linea.indexOf("_"));
							variant1 = linea.substring(linea.indexOf("_")+1,linea.indexOf(")"));
							linea = linea.substring(linea.indexOf("some ")+5);
							variant2 = linea.substring(linea.indexOf("_")+1,linea.indexOf(")"));
						}else{
							System.out.println("ERROR: linea = "+linea);
						}
					}
					System.out.println("sc:human_with_genotype_"+rsnumber+"_variant_"+variant1+"_"+variant2+",");
				}
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
