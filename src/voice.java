import java.io.File;

public class voice{

	voice()
	{

	}

	void say(String phrase)
	{
		File directory = new File("."); 


		try{
			String command = directory.getCanonicalPath(); 

			command=command+"\\bin\\espeak \"" + phrase + "\"";
			Runtime r=Runtime.getRuntime();
			Process p=null;
			p=r.exec(command); 
			System.out.println(phrase);
		}catch(Exception e){ 
			System.out.println("error:"+e.getMessage()); 
			e.printStackTrace(); 
		}
	}



}