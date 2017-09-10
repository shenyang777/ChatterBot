// This code contains all the basic things we need for a chatter bot with clean format and comments.
// Related to my interest: Artificial Intelligence and Natrual Language Processing.
// The first code I wrote after attending graduate school
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

public class chatbot {
	static voice newvoice=new voice();
	static int[][] record=new int[1000][10];
	static int answers=3;
	static int user_repeatition=0;
	static List<String> memoryofdialog = new ArrayList<String>();
	static String lastword="", lastContext = "", learning = null;
	static boolean questionAsked = false;
	private static String transposList[][] = Config.TransposeList;

	// initiator indicates whether the bot is intitiating in conversation. If it is, then
	// we can expect any input. If not, then we can distinguish input depending on whether
	// it follows a question or not.
	static String findMatch(String str, int save, int initiator) {
		String basic_str=str;
		str = Helpers.cleanse(str);
		float max=0;
		int[] matchnum=new int[2];
		matchnum[0]=-1;
		matchnum[1]=0;

		if(str.replace(" ",	"").length()==0)
		{
			str="NULL INPUT";
		}

		///////////////////continuous repetition////////////////////////////////////
		//System.out.Println(lastword + "  " + str + "  " + Helpers.levenshtein(lastword,str));
		if(Helpers.levenshtein(lastword,str) > Config.SimilarityThreshhold || (lastword.equals("NULL INPUT")&&str.equals("NULL INPUT")))
		{
			user_repeatition++;

			//System.out.Println(user_repeatition+"???");
			if(user_repeatition >= 2)
			{
				if(!str.contains("NULL INPUT"))
				{
					str="REPETITION T1";
					return findMatch(str,0,0);
				}
				else
				{
					str="NULL INPUT REPETITION";
					return findMatch(str,0,0);
				}
			}
		}
		else
		{
			if (!Helpers.isRepetition(str)) {
				user_repeatition=0;
			}
			/////////////////history repetition/////////////////////////////////////////
			if(memoryofdialog.contains(str) && !Helpers.isRepetition(str) &&
					!Helpers.isBotDontUnderstood(str) && !Helpers.isQuestionAnswer(str))
			{
				//System.out.Println("found " + memoryofdialog.get(memoryofdialog.indexOf(str)));
				str="REPETITION T2";
				return findMatch(str,0,0);
			}
			else
			{
				if(memoryofdialog.size() < Config.MemorySize)
				{
					memoryofdialog.add(str);
				}
				else
				{
					memoryofdialog.remove(0);
					memoryofdialog.add(str);
				}
			}
			//////////////////////////////////////////////////////////
		}


		//////////////////////////simple question answer case///////////////////////////////
		if (questionAsked) {
			questionAsked = false;
			for (String ans : Config.QuestionAnswers) {
				for (String word : str.split(" ")) {
					if (Helpers.levenshtein(ans, word) > Config.SimilarityThreshhold) {
						str = "RECEIVED ANSWER INPUT";
						return findMatch(str,0,0);
					}
				}
			}
		}

		for(int i=0;i<100;i++)
		{
			for(int j=0;j<10;j++)
			{
				record[i][j]=0;
			}
		}
		String result = "";
		for(int i = 0; i<Config.KB.length; ++i) {
			//if(KB[i][0].equalsIgnoreCase(str))
			String[] keyWordList = Config.KB[i][0];
			for(int j=0;j<keyWordList.length;j++)
			{
				String keyword=keyWordList[j];
				String origin_keyword=keyword;
				keyword=keyword.replaceAll("_","");

				//				float similarity = Config.SimilarityWeights[0]*levenshtein(keyword,str) 
				//						+ Config.SimilarityWeights[1]*contextMeasure(str);
				float similarity = Helpers.levenshtein(keyword, str);
				if(similarity>Config.SimilarityThreshhold||str.contains(keyword))
				{

					if(similarity>Config.SimilarityThreshhold&&!str.contains(keyword))
					{
						if(max<similarity)
						{
							//System.out.Println("similarity: "+similarity+" "+origin_keyword);
							max=similarity;
							matchnum[0]=i;
							matchnum[1]=j;
						}
					}
					else
					{
						int keywordPos=str.indexOf(keyword);
						if(!wrong_location(origin_keyword,keywordPos,str,similarity))
						{
							if(((str.contains(keyword+" ") ||
									str.contains(" "+keyword))&&similarity>0.2) ||
									(similarity>Config.SimilarityThreshhold&&str.contains(keyword)))
							{
								if(max<similarity)
								{
									//System.out.Println("similarity2: "+similarity+" "+origin_keyword);
									max=similarity;
									matchnum[0]=i;
									matchnum[1]=j;
								}
							}
						}
					}
				}
			}
		}
		///////////////////////////////////////

		if(matchnum[0]!=-1)
		{
			Random generator = new Random();
			int nSelection, retries = 0;
			nSelection = generator.nextInt(Config.KB[matchnum[0]][1].length);
			while(nSelection==record[matchnum[0]][matchnum[1]] && retries < Config.MaxRespRetries)
			{
				nSelection = generator.nextInt(Config.KB[matchnum[0]][1].length);
				retries++;

			}
			//System.out.Println(matchnum[0]+" "+matchnum[1]+" "+nSelection);  

			result = Config.KB[matchnum[0]][1][nSelection];

			record[matchnum[0]][matchnum[1]]=nSelection;
			result=preprocess_response(result,basic_str,Config.KB[matchnum[0]][0][matchnum[1]]);

		}

		///////////////////////////////////////
		if(save==1)
		{lastword=str;}

		questionAsked = false;
		if (initiator != 1 && result.length() > 0 && result.charAt(result.length()-1) == '?') {
			questionAsked = true;
		}

		lastContext = Helpers.removeStopwords(result);

		return result;
	}

	static String insert_space(String str)
	{
		StringBuffer temp = new StringBuffer(str);
		temp.insert(0, ' ');
		temp.insert(temp.length(), ' ');
		return temp.toString();
	}
	public static String transpose( String str )
	{
		String res = "";

		for (String word : str.split(" ")) {
			for(int i = 0; i<transposList.length; ++i)
			{
				String first = transposList[i][0];
				String second = transposList[i][1];

				if (word.equals(first)) {
					word = second;
				} else if (word.equals(second)) {
					word = first;
				}
			}

			res += word + " ";
		}

		return res.substring(0, res.length()-1);
	}

	public static String find_subject(String sInput,String sKeyWord)
	{
		//System.out.Println("find subject after " + sKeyWord + " in " + sInput);
		String sSubject = ""; // resets subject variable
		int pos = sInput.indexOf(sKeyWord);
		if(pos != -1)
		{
			sSubject = sInput.substring(pos + sKeyWord.length(),sInput.length());		
		}
		return sSubject;
	}

	public static String preprocess_response(String sResponse,String sInput,String sKeyWord)
	{
		sInput=sInput.toUpperCase();
		String sSubject = "";
		if(sResponse.contains("*"))
		{
			// extracting from input
			sSubject=find_subject(sInput,sKeyWord.replace("_", "")); 
			// conjugating subject

			sSubject = transpose(sSubject); 
			//System.out.Println(sSubject+" haha");
			//System.out.Println(sResponse+" hehe");

			sResponse = sResponse.replaceFirst("\\*",sSubject);
		}
		return sResponse;
	}

	public static boolean wrong_location(String origin_keyword,int keywordPos,String str,float similarity)
	{
		boolean wrongPos=false;
		String keyword=origin_keyword.replaceAll("_","");
		if(origin_keyword.charAt(0)=='_'&&origin_keyword.charAt(origin_keyword.length()-1)=='_')
		{
			if(similarity<1)
			{
				wrongPos=true;
			}
		}
		else if(origin_keyword.charAt(0)=='_'&&origin_keyword.charAt(origin_keyword.length()-1)!='_')
		{
			//System.out.Println("similarity"+similarity+" "+origin_keyword);
			if(Math.abs(keywordPos+keyword.length()-str.length())<=2)
			{
				wrongPos=true;
			}
		}
		else if(origin_keyword.charAt(0)!='_'&&origin_keyword.charAt(origin_keyword.length()-1)=='_')
		{
			if(keywordPos==0)
			{
				wrongPos=true;
			}
		}

		return wrongPos;
	}

	// fonction for handling context
	public static float contextMeasure(String str)
	{
		String context = Helpers.removeStopwords(str);

		if (lastContext.length() == 0 || context.length() == 0) {
			return (float)0.;
		}

		float matches = (float)0.;
		String keywords[] = context.split(" ");
		for (String keyword : keywords) {
			matches += (lastContext.contains(keyword) ? 1. : 0.);
		}

		//		//System.out.Println(lastContext + " MEASURE " + matches/keywords.length);
		return matches/keywords.length;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Timer timer;
		Config.LoadKB();

		String sResponse = findMatch("SIGNON",0,1);
		newvoice.say(sResponse);

		while(true) {
			timer = new Timer("INACTIVE USER TIMER");
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						String prompt;
						if ((prompt = Helpers.readUnknown()) != null) {
							learning = prompt;
						} else {
							prompt = Config.SmallTalk[new Random().nextInt(Config.SmallTalk.length)];
						}
						newvoice.say(prompt);
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}, Config.InactiveUserTime);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String sInput = in.readLine();
			timer.cancel();
			Random generator = new Random();

			if (learning != null) {
				Helpers.learnUnknown(learning, sInput);
				learning = null;
			}
			
			sResponse = findMatch(sInput,1,0);

			if(sResponse.contains("EXIT")) {
				sResponse=sResponse.substring(0, sResponse.length()-4);
				newvoice.say(sResponse);
				Helpers.writeUnknown();
				System.exit(1);
			} else {
				if(sResponse.equals(""))
				{
					sResponse = findMatch("BOT DON'T UNDERSTAND",0,0);
					newvoice.say(sResponse);
					Helpers.writeTemp(sInput);
				}
				else
				{
					newvoice.say(sResponse);
				}
			}
		}
	}
}
