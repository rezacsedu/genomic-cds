package test;

import java.io.File;

public class Prueba3_absolutePath {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path = new File(".").getAbsolutePath();
		path = path.substring(0,path.lastIndexOf("."));
		System.out.println("Path = "+path);
	}

}
