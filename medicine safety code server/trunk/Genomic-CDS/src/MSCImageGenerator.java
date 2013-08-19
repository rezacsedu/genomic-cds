import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Servlet implementation class MSCImageGenerator
 * It generates the QR image with the corresponding URL.
 */
public class MSCImageGenerator extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MSCImageGenerator() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String code = URLDecoder.decode(request.getParameter("url"), "UTF-8");
		ServletOutputStream out = response.getOutputStream();
		
		//request.setAttribute("Cache-control", "private");
		
		Charset charset = Charset.forName("ISO-8859-1");
		CharsetEncoder encoder = charset.newEncoder();
		byte[] b = null;
		try {
			// Convert string of ProfileURL to ISO-8859-1 bytes in a ByteBuffer
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(code));
			b = bbuf.array();
		} catch (CharacterCodingException e) {
			System.out.println(e.getMessage());
		}

		String data;
		try {
			data = new String(b, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return;
		}

		// get a BitMatrix for the data
		BitMatrix matrix = null;
		int h = 200;
		int w = 200;
		com.google.zxing.Writer writer = new QRCodeWriter();
		try {
			matrix = writer.encode(data,
					com.google.zxing.BarcodeFormat.QR_CODE, w, h);
		} catch (com.google.zxing.WriterException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			return;
		}
			
		try {
			String path = this.getServletContext().getRealPath("/");
	        path=path.replaceAll("\\\\", "/");
			InputStream is = new FileInputStream(path+"images/safetyCodeFrameImage2.png");
			BufferedImage frameImage = ImageIO.read(is);
			BufferedImage finalImage = new BufferedImage(frameImage.getWidth(), frameImage.getHeight(), BufferedImage.TYPE_INT_RGB);
			BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(matrix);
			
			Graphics g = finalImage.getGraphics();
			g.drawImage(frameImage, 0, 0, null); // draw the frame and logo of the Medicine Safety Code
			g.drawImage(barcodeImage, 3, 28, null); // position barcode inside frame
			ImageIO.write(finalImage, "PNG", out);		
			g.dispose();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} 		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
