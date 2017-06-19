import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;


public class Helpers {
	public static boolean isRepetition(String str) {
		return str.equals("REPETITION T1") || str.equals("REPETITION T2") || str.equals("NULL INPUT REPETITION");
	}

	public static boolean isBotDontUnderstood(String str) {
		return str.equals("BOT DONT UNDERSTAND");
	}

	public static boolean isQuestionAnswer(String str) {
		for (String ans : Config.QuestionAnswers) {
			if (levenshtein(str, ans) > Config.SimilarityThreshhold) {
				return true;
			}
		}

		return false;
	}

	public static String cleanse(String str)
	{
		String clean=str.toUpperCase();//change all letters to upper ones
		clean=clean.replaceAll(" +", " ");//remove extra space
		clean=clean.replaceAll("[\\pP¡®'¡°¡±]", "");//remove all punctuations
		return clean;
	}

	public static String removeStopwords(String str) {
		String res = "";

		for (String word : cleanse(str).split(" ")) {
			boolean isStopWord = false;

			for (String stopword : Config.Stopwords) {
				if (word.equals(stopword.toUpperCase())) {
					isStopWord = true;
					break;
				}
			}

			if (!isStopWord) {
				res += word + " ";
			}
		}

		if (res.charAt(res.length()-1) == ' ') {
			return cleanse(res.substring(0, res.length()-1));
		}
		else 
		{
			return cleanse(res);
		}
	}

	public static float levenshtein(String str1, String str2) {  
		//
		int len1 = str1.length();  
		int len2 = str2.length();  
		//
		int[][] dif = new int[len1 + 1][len2 + 1];  
		//
		for (int a = 0; a <= len1; a++) {  
			dif[a][0] = a;  
		}  
		for (int a = 0; a <= len2; a++) {  
			dif[0][a] = a;  
		}  
		//
		int temp;  
		for (int i = 1; i <= len1; i++) {  
			for (int j = 1; j <= len2; j++) {  
				if (str1.charAt(i - 1) == str2.charAt(j - 1)) {  
					temp = 0;  
				} else {  
					temp = 1;  
				}  
				//
				dif[i][j] = min(dif[i - 1][j - 1] + temp, dif[i][j - 1] + 1,  
						dif[i - 1][j] + 1);  
			}
		}
		
		float similarity = 1 - (float) dif[len1][len2] / Math.max(str1.length(), str2.length());  
		
		return similarity;
	} 

	static int min(int a, int b, int c) {  
		if(a < b) {  
			if(a < c)  
				return a;  
			else   
				return c;  
		} else {  
			if(b < c)  
				return b;  
			else   
				return c;  
		}  
	}  

	public static String readUnknown() throws IOException  {
		File unknown = new File(Config.unknownFile);
		File tempfordelete = new File(Config.tempfordeleteFile);
		
		BufferedReader fin = new BufferedReader(new FileReader(unknown));
		BufferedWriter fout = new BufferedWriter(new FileWriter(tempfordelete, false));
		String _line, line  = fin.readLine();
		int i=0;
		Random generator = new Random();
		int ran_num=generator.nextInt(10);
		
		while ((_line = fin.readLine()) != null) {
			if(i==0)
			{
			i=1;
			}
			else
			{
			fout.append(_line + "\r\n");
			}
			
		}
		
		fin.close();
		fout.close();
		BufferedReader fin1 = new BufferedReader(new FileReader(tempfordelete));
		BufferedWriter fout1 = new BufferedWriter(new FileWriter(unknown, false));
		
		String line1="";
		while ((line1 = fin1.readLine()) != null)
		{
			fout1.append(line1 + "\r\n");
		}
		fin1.close();
		fout1.close();
		
		//temp.renameTo(unknown);

		return line;
	}

	public static void writeUnknown() throws IOException {
		BufferedReader fin = new BufferedReader(new FileReader(Config.temp));
		BufferedWriter fout = new BufferedWriter(new FileWriter(Config.unknownFile, true));
		String line1="";
		while ((line1 = fin.readLine()) != null)
		{
			fout.append(line1 + "\r\n");
		}
		fin.close();
		fout.close();
		
	}
	public static void writeTemp(String keywords) throws IOException {
		BufferedWriter fout = new BufferedWriter(new FileWriter(Config.temp, false));
		fout.append(keywords.toUpperCase() + "\r\n");
		fout.close();
	}

	public static void learnUnknown(String input, String resp) throws IOException {
		//		input = removeStopwords(input);

		for (int i = 0; i < Config.KB.length; i++) {
			for (int j = 0; j < Config.KB[i][0].length; j++) {
				if (levenshtein(Config.KB[i][0][j], input) > Config.SimilarityThreshhold) {
					// Add input keywords
					String kwords[] = new String[Config.KB[i][0].length+1];
					for (int k = 0; k < Config.KB[i][0].length; k++) {
						kwords[k] = Config.KB[i][0][k];
					}
					kwords[Config.KB[i][0].length] = cleanse(input);
					Config.KB[i][0] = kwords;
					
					// Add response
					String resps[] = new String[Config.KB[i][1].length+1];
					for (int k = 0; k < Config.KB[i][1].length; k++) {
						resps[k] = Config.KB[i][1][k];
					}
					resps[Config.KB[i][1].length] = resp;
					Config.KB[i][1] = resps;
					Config.WriteKB();
					
					return;
				}
			}
		}

		// Add new entry
		String newEntry[][] = new String[2][];
		newEntry[0] = new String[1];
		newEntry[1] = new String[1];
		newEntry[0][0] = cleanse(input);
		newEntry[1][0] = resp.toUpperCase();

		Config.WriteToKB(newEntry);
	}
}
