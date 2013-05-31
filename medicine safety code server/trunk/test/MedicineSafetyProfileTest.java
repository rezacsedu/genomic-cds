import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 */

/**
 * @author m
 *
 */
public class MedicineSafetyProfileTest {
	
	final String myBase64TestString1 = "qm70W00I2KZ0u-A01N103HmXRp0W80080zIe0oWW0vL4kC8441HTCCCfOA03U0uW020013mICe00I8wyFnk6Qo4m20e7Nq0m0500G03RG90e56407xu6O0000000000000000000000000000";
	
	MedicineSafetyProfile myProfile = new MedicineSafetyProfile();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void Base64ProfileStringRoundtrip() throws Exception {
		MedicineSafetyProfile myProfile = new MedicineSafetyProfile();
		myProfile.readBase64ProfileString(myBase64TestString1);
		assertEquals(myBase64TestString1, myProfile.getBase64ProfileString());
	}

	/**
	 * Test method for {@link MedicineSafetyProfile#read23AndMeFileStream(java.io.InputStream, java.lang.String)}.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	@Test
	public void testRead23AndMeFileStreamInputStreamString() throws FileNotFoundException, IOException {
		fail("Not yet implemented");
		/*
		myProfile.read23AndMeFileStream(new FileInputStream("C:\\Users\\m\\Documents\\Doc\\Gesundheit\\Matthias SNPs\\genome_M_Sa_Full_20120217024430.txt"));
		*/
	}

}
