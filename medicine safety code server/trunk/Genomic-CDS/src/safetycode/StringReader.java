package safetycode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringReader {
	 String readFile(String fileName) throws IOException {
		InputStream stream = this.getClass().getResourceAsStream(fileName);
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		return stringBuilder.toString();
	}
}
