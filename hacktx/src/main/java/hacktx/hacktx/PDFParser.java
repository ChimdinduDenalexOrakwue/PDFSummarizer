package hacktx.hacktx;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFParser 
{
	PDDocument document;
	String path;
	PDFTextStripper pdfStripper;
	Map<String, Long> wordMap;
	List<String> links;
	String pdfText;
	HashSet<String> nonTermSet = new HashSet<String>(Arrays.asList("the", "there", "it", "is", "of", " ",
																	"thus", "these", "for", "be", "an", "by", "with", "if",
																	"in", "example", "and", "this", "to", 
																	"same", "well", "better", "then", "have", "each", 
																	"one", "two", "three", "four", "five", "six", "seven",
																	"eight", "nine" , "ten", "we", "are", "all", "as", 
																	"from", "using", "al", "at", "et", "that", "on", "or",
																	"not"));
	
	public PDFParser() {
		wordMap = new HashMap<String, Long>();
		links = new ArrayList<String>();
	}
	
	public PDFParser(String path) {
		wordMap = new HashMap<String, Long>();
		links = new ArrayList<String>();
	}
	
	public void readFile(String path) throws IOException {
		// read the PDF from a file
		File file = new File(path);
		document = PDDocument.load(file);
		
		// get the text from the PDF
		pdfStripper = new PDFTextStripper();
		pdfText = pdfStripper.getText(document);
		
		// close document
		// document.close();
	}
	
	// maps each word in the text to its frequency
	// in the document
	public void mapText() {
		String[] words = pdfText.split("\\t|,|;|\\.|\\?|!|-|:|@|\\[|\\]|\\(|\\)|\\{|\\}|_|\\*|/| ");
		for (String word : words) {
			if (checkValidTerm(word)) {
				if (wordMap.containsKey(word.toLowerCase())) {
					long frequency = wordMap.get(word.toLowerCase());
					wordMap.put(word.toLowerCase(), frequency + 1L);
				} else {
					wordMap.put(word.toLowerCase(), 1L);
				}
			}
		}
	}
	
	public boolean checkValidTerm(String word) {
		if (word.length()<= 1) {return false;}
		
	    for (int i=0; i< word.length(); i++) {
	        char c = word.charAt(i);
	        if (!Character.isLetter(c))
	            return false;
	    }
	    
	    if (nonTermSet.contains(word.toLowerCase())) {
	    	return false;
	    }

		return true;
	}
	
	public void printSplitText() {
		String[] words = pdfText.split("\\t|,|;|\\.|\\?|!|-|:|@|\\[|\\]|\\(|\\)|\\{|\\}|_|\\*|/| ");
		System.out.println(Arrays.toString(words));
	}
	
	public void printText() {
		System.out.println(pdfText);
	}
	
	public void printLinks() {
		for (String link : links) {
			System.out.println(link);
		}
	}
	
	public void detectLinks() {
		Pattern urlPattern = Pattern.compile(
		        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
		                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
		                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
		        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		
		Matcher matcher = urlPattern.matcher(pdfText);
		while (matcher.find()) {
		    int matchStart = matcher.start(1);
		    int matchEnd = matcher.end();
		    
		    links.add(pdfText.substring(matchStart, matchEnd));
		}
	}
	
	public void getImages(String path) throws IOException {
	    PDPageTree list = document.getPages();
	    for (PDPage page : list) {
	        PDResources pdResources = page.getResources();
	        for (COSName c : pdResources.getXObjectNames()) {
	            PDXObject o = pdResources.getXObject(c);
	            if (o instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
	                File file = new File(path + "//" + System.nanoTime() + ".png");
	                ImageIO.write(((org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject)o).getImage(), "png", file);
	            }
	        }
	    }
	}
	
	public void createNewDocument(String path, String name) throws IOException {
		PDDocument newDocument = new PDDocument();
		PDPage page = new PDPage();
		newDocument.addPage(page);
		
		PDPageContentStream contentStream = new PDPageContentStream(newDocument, newDocument.getPage(0));
		contentStream.beginText();
		contentStream.newLineAtOffset(100, 700);
		
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
		contentStream.showText("Key Terms:");
		contentStream.setFont(PDType1Font.HELVETICA, 11);
		int count = 0;
		for (String word : wordMap.keySet()) {
			if (count < 10) {
				if (wordMap.get(word) > (wordMap.size() * 0.02)) {
					try {
						contentStream.newLineAtOffset(0, -15);
						contentStream.showText(word);
						count++;
					} catch (Exception e) {
						continue;
					}
				}
			} else {
				break;
			}
		}
		
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
		contentStream.newLineAtOffset(0, -30);
		contentStream.showText("Links:");
		contentStream.setFont(PDType1Font.HELVETICA, 11);
		for (String link : links) {
			contentStream.newLineAtOffset(0, -15);
			contentStream.showText(link);
		}
		
		///////////////////////////////////////////////////
		contentStream.endText();
		contentStream.close();
		
		page = new PDPage();
		newDocument.addPage(page);
		
		contentStream = new PDPageContentStream(newDocument, newDocument.getPage(1));
		contentStream.beginText();
		contentStream.newLineAtOffset(25, 700);
		
		contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
		contentStream.showText("Images:");
		contentStream.endText();
		
		PDPageTree list = document.getPages();
	    for (PDPage page1 : list) {
	        PDResources pdResources = page1.getResources();
	        int x = 25;
	        int y = 550;
	        for (COSName c : pdResources.getXObjectNames()) {
	            PDXObject o = pdResources.getXObject(c);
	            if (o instanceof org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject) {
	                contentStream.drawImage((PDImageXObject) o, x, y, 75, 75);
	                x += 105;
	                if (x >= 600) {
	                	x = 25;
	                	y -= 105;
	                }
	            }
	        }
	    }
	    contentStream.close();
	    
		newDocument.save(path + "\\" + name);
		newDocument.close();
	}
}
