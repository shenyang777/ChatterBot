import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Config {
	
	static int MemorySize = 5;
	static int MaxRespRetries = 5;
	static long InactiveUserTime = 20000;
	static double SimilarityThreshhold = 0.6;
	static float[] SimilarityWeights = {(float).5, (float).5};
	static String kbFile = "data/corpus.txt";
	static String unknownFile = "data/unknown.txt";
	static String temp = "data/temp.txt";
	static String tempfordeleteFile = "data/tempfordelete.txt";
	static String KB[][][];
	
	public static String SmallTalk[] = {
		"USER? HELLO?",
		"USER? WHAT DO YOU WANT?",
		"USER? ASK ME ANYTHING",
		"USER? ARE YOU BORED?",
		"USER? HOW IS YOUR DAY GOING?",
		"USER? DO YOU NOT LIKE ROBOTS?",
		"USER? DO I INTIMIDATE YOU?"
	};
	
	public static String TransposeList[][] = {
		{"I'M", "YOU'RE"},
		{"AM", "ARE"},
		{"WERE", "WAS"},
		{"ME", "YOU"},
		{"YOURS", "MINE"},
		{"YOUR", "MY"},
		{"I'VE", "YOU'VE"},
		{"I", "YOU"},
		{"AREN'T", "AM NOT"},
		{"WEREN'T", "WASN'T"},
		{"I'D", "YOU'D"},
		{"DAD", "FATHER"},
		{"MOM", "MOTHER"},
		{"DREAMS", "DREAM"},
		{"MYSELF", "YOURSELF"}
	};
	
	public static String Stopwords[] = {
		"a", "an", "and", "so", "as", "at", "be", "by", "for", "from",
		"he", "in", "is", "it", "its", "of", "on", "that", "the", "to",
	};
	
	public static String QuestionAnswers[] = {
		"YES", "YA", "YEAH", "NO", "NAH", "NOPE", "NOTHING", "PERHAPS", "MAYBE", 
		"RIGHT", "TRUE", "GOOD", "WELL", "OK", "OKAY"
	};

	public static String[][][] LoadKB() throws IOException  {
		String line;
		
		List<String[][]> entries = new ArrayList<String[][]>();
		
		BufferedReader fin = new BufferedReader(new FileReader(kbFile));
		while ((line = fin.readLine()) != null) {
			if (line.length() > 0) {
				String entry[][] = new String[2][];
				
				String inout[] = line.split("\\|\\|");
				entry[0] = inout[0].split("\\|");
				entry[1] = inout[1].split("\\|");

				entries.add(entry);
			}
		}
		
		KB = new String[entries.size()][][];
		for (int i = 0; i < entries.size(); i++) {
			KB[i] = entries.get(i);
		}
		
		fin.close();
		return KB;
	}
	
	public static void WriteKB() throws IOException  {
		// Clear file
		BufferedWriter fout = new BufferedWriter(new FileWriter(kbFile, false));
		fout.write("");
		fout.close();
		
		for (String[][] entry : KB) {
			WriteToKB(entry);
		}
	}
	
	public static void WriteToKB(String entry[][]) throws IOException {
		BufferedWriter fout = new BufferedWriter(new FileWriter(kbFile, true));
		String line = "";
		boolean first = true;

		for (String keyword : entry[0]) {
			if (first) {
				line += keyword;
				first = false;
			} else {
				line += "|" + keyword;
			}
		}

		line += "||";
		first = true;

		for (String response : entry[1]) {
			if (first) {
				line += response;
				first = false;
			} else {
				line += "|" + response;
			}
		}
		
		fout.append(line + "\r\n");
		fout.close();
		
		String newKB[][][] = new String[Config.KB.length + 1][][];
		System.arraycopy(Config.KB, 0, newKB, 0, Config.KB.length);
		newKB[Config.KB.length] = entry;
		Config.KB = newKB;
	}
}