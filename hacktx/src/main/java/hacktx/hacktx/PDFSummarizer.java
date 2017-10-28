package hacktx.hacktx;

import java.io.IOException;

public class PDFSummarizer {
	private static PDFParser parser;
	
	public static void main(String[] args) throws IOException {
		parser = new PDFParser();
		parser.readFile("C:\\Users\\Denalex\\Desktop\\SamplePDF\\Samples\\GAN.pdf");
		//parser.printSplitText();
		parser.printText();
		parser.detectLinks();
		parser.mapText();
		//parser.printLinks();
		//parser.getImages("C:\\Users\\Denalex\\Desktop\\SamplePDF\\Images");
		parser.createNewDocument("C:\\Users\\Denalex\\Desktop\\SamplePDF\\Output", "test.pdf");
	}
}
