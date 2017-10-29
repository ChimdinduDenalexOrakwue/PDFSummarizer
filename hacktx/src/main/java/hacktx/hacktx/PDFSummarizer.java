package hacktx.hacktx;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class PDFSummarizer {
	private static PDFParser parser;
	
	public static void main(String[] args) throws IOException {
		parser = new PDFParser();
		JFileChooser fc = new JFileChooser();
		String message = "Select the action to be performed";
		String[] options = new String[] {"Extract Images", "Summarize"};
		int choice = JOptionPane.showOptionDialog(null, message, "Action Selector", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);
		FileNameExtensionFilter ef = new FileNameExtensionFilter(".pdf", "pdf");
		fc.setFileFilter(ef);
		int bs = fc.showOpenDialog(null);
		File fileToGet = fc.getSelectedFile();
		parser.readFile(fileToGet.getAbsolutePath());
		
		if (choice == 0) {
			fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    fc.setAcceptAllFileFilterUsed(false);
		    bs = fc.showOpenDialog(null);
		    
			File directory = fc.getSelectedFile();
			parser.getImages(directory.getAbsolutePath());
		} else {
			ArrayList<String> sentences = new ArrayList<String>();
			LinkedHashMap<String, Integer> freqs = getFreqs(parser, sentences);
			LinkedHashMap<String, Integer> sentenceValue = getSentenceValue(sentences, freqs);
			HashSet<String> finalText = chooseSentences(sentences, sentenceValue);
			parser.mapText();
			parser.detectLinks();
			parser.createNewDocument(finalText, fc.getCurrentDirectory().getAbsolutePath(), fileToGet.getName().substring(0, fileToGet.getName().length() - 4) + "_summary.pdf");
		}		
	}
	
	public static HashSet<String> chooseSentences(ArrayList<String> sentences, LinkedHashMap<String, Integer> vals){
		ArrayList<Integer> finalIndices = new ArrayList<Integer>();
		int maxNumChars = 600;
		while (maxNumChars > 0 && finalIndices.size() < vals.size()){
			int currMaxIndex = 0;
			int currIndex = 0;
			for (String curr : vals.keySet()){
				if (curr.length() > sentences.get(currMaxIndex).length() && !finalIndices.contains(currIndex)){
					currMaxIndex = currIndex;
				}
				currIndex++;
			}
			finalIndices.add(currMaxIndex);
			maxNumChars -= sentences.get(currMaxIndex).length();
		}
		Collections.sort(finalIndices);
		HashSet<String> finalSentences = new HashSet<String>();
		for (int index : finalIndices){
			finalSentences.add(sentences.get(index));
		}
		finalSentences.remove("");
		return finalSentences;

	}
	
	public static LinkedHashMap<String, Integer> getSentenceValue(ArrayList<String> sentences, HashMap<String, Integer> wordVals){
		LinkedHashMap<String, Integer> sentenceValue = new LinkedHashMap<String, Integer>();
		for (String sentence : sentences){
			if  (sentence.length() > 0) {
				int sentenceVal = 0;
				String[] wordsInSentence = sentence.split("\\s|\\r|\\n|\\t|,|;|\\.|\\?|!|-|:|@|\\[|\\]|\\(|\\)|\\{|\\}|_|\\*|/| ");
				for (String word : wordsInSentence){
					if (wordVals.containsKey(word))
						sentenceVal += wordVals.get(word);
				}
				sentenceValue.put(sentence, sentenceVal / (sentence.length()));
			}
		}
		return sentenceValue;
	}

	public static LinkedHashMap<String, Integer> getFreqs(PDFParser parser, ArrayList<String> sentences){
		LinkedHashMap<String, Integer> freqs = new LinkedHashMap<String, Integer>();
		String[] words = parser.pdfText.split("\\s|\\r|\\n|\\t|,|;|\\.|\\?|!|-|:|@|\\[|\\]|\\(|\\)|\\{|\\}|_|\\*|/| ");
		String sentence = "";
		for (String word : words) {
			if (word.length() == 0)
				continue;
			char start = word.charAt(0);
			char end = word.charAt(word.length() - 1);
			if (start < 32 || start == 33 || start == 46 || start == 63 || start > 127) {
				while (word.length() >= 2 && (start < 34 || start == 46 || start == 63 || start > 127)) {
					word = word.substring(1);
					start = word.charAt(0);
				}
				sentences.add(sentence.toString());
				sentence = word + " ";
			} else if (end < 32 || end == 33 || end == 46 || end == 63 || end > 127) {
				while (word.length() >= 2 && (end < 34 || end == 46 || end == 63 || end > 127)) {
					word = word.substring(0, word.length() - 1);
					end = word.charAt(word.length() - 1);
				}
				sentence += word;
				sentences.add(sentence.toString());
				sentence = "";
			} else {
				sentence += word + " ";
			}
			if (freqs.containsKey(word)) {
				freqs.put(word, freqs.get(word) + 1);
			} else {
				freqs.put(word, 1);
			}
		}
		return freqs;
	}
}
