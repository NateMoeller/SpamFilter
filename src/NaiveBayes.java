import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NaiveBayes {


	public ArrayList<ArrayList<String>> trainingSet;
	HashMap<String, Integer> spamWordsBody;
	HashMap<String, Integer> hamWordsBody;
	HashMap<String, Integer> spamWordsSub;
	HashMap<String, Integer> hamWordsSub;
	int totalEmails;

	public NaiveBayes(ArrayList<ArrayList<String>> ts){
		//get database parameters here
		MySQLAccess mysql = new MySQLAccess();
		spamWordsBody = new HashMap<String, Integer>();
		hamWordsBody = new HashMap<String, Integer>();
		spamWordsSub = new HashMap<String, Integer>();
		hamWordsSub = new HashMap<String, Integer>();
		try {
			//spot 0 is subject, 1 is body, and 2 is spam
			//trainingSet = mysql.getTrainingSet();
			trainingSet = ts;
			totalEmails = trainingSet.size();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getVocabBody(){

		for(int i = 0; i < trainingSet.size(); i++){
			//parse the words in the description
			//System.out.println(trainingSet.get(i).get(1));
			String[] words = trainingSet.get(i).get(1).split(" "); //split based off of a space

			for(int j = 0; j < words.length; j++){
				if(words[j].length() >= 3){ //ONLY CONSIDER WORDS THAT ARE LONGER THAN 2 CHARACTERS
					if(Integer.parseInt(trainingSet.get(i).get(2)) == 1){
						if(spamWordsBody.get(words[j]) == null){
							spamWordsBody.put(words[j], 1);
						}
						else{
							spamWordsBody.put(words[j], spamWordsBody.get(words[j]) + 1);
						}
					}
					else if(Integer.parseInt(trainingSet.get(i).get(2)) == 0){
						if(hamWordsBody.get(words[j]) == null){
							hamWordsBody.put(words[j], 1);
						}
						else{
							hamWordsBody.put(words[j], hamWordsBody.get(words[j]) + 1);
						}
					}
					else{
						System.out.println("something went wrong");
					}
				}
			}
		}
	}

	public void getVocabSubject(){
		for(int i = 0; i < trainingSet.size(); i++){
			//parse the words in the description
			//System.out.println(trainingSet.get(i).get(1));
			String[] words = trainingSet.get(i).get(0).split(" "); //split based off of a space

			for(int j = 0; j < words.length; j++){
				if(words[j].length() >= 3){ //ONLY CONSIDER WORDS THAT ARE LONGER THAN 2 CHARACTERS
					if(Integer.parseInt(trainingSet.get(i).get(2)) == 1){
						if(spamWordsSub.get(words[j]) == null){
							spamWordsSub.put(words[j], 1);
						}
						else{
							spamWordsSub.put(words[j], spamWordsSub.get(words[j]) + 1);
						}
					}
					else if(Integer.parseInt(trainingSet.get(i).get(2)) == 0){
						if(hamWordsSub.get(words[j]) == null){
							hamWordsSub.put(words[j], 1);
						}
						else{
							hamWordsSub.put(words[j], hamWordsSub.get(words[j]) + 1);
						}
					}
					else{
						System.out.println("something went wrong");
					}
				}
			}
		}
	}

	public int classifyEmail(String subject, String body){
		getVocabBody();
		getVocabSubject();

		//numSpam and numHam is the same for subject and body
		int numSpam = 0;
		int numHam = 0;
		for(int i = 0; i < totalEmails; i++){
			if(Integer.parseInt(trainingSet.get(i).get(2)) == 1){
				numSpam++;
			}
			else if(Integer.parseInt(trainingSet.get(i).get(2)) == 0){
				numHam++;
			}
		}

		double probabilityOfHamTS = ((double)numHam / totalEmails);
		double probabilityOfSpamTS = ((double)numSpam / totalEmails);
		//compute priors
		double hamPrior = Math.log(probabilityOfHamTS);
		double spamPrior = Math.log(probabilityOfSpamTS);
		
		
		//BODY
		//count the number of words in non-spam emails
		int hamTotalBody = 0;
		for(Map.Entry<String, Integer> entry : hamWordsBody.entrySet()){
			//System.out.printf("Key : %s and Value: %s %n", entry.getKey(), entry.getValue());
			hamTotalBody += entry.getValue();
		}
		//count the number of words in spam emails
		int spamTotalBody = 0;
		for(Map.Entry<String, Integer> entry : spamWordsBody.entrySet()){
			//System.out.printf("Key : %s and Value: %s %n", entry.getKey(), entry.getValue());
			spamTotalBody += entry.getValue();
		}
		//String bodyText = body;
		String bodyText = body.replaceAll("[^a-z0-9]", " "); //COMMENT OR UNCOMMENT THIS LINE WHEN SWITCHING CLASSIFIERS
		//System.out.println("Body: " + bodyText);
		String[] bodyWords = bodyText.split(" ");
		double spamSumLikelihoodBody = 0;
		double hamSumLikelihoodBody = 0;
		for(int i = 0; i < bodyWords.length; i++){
			//skip all spaces
			if(bodyWords[i].compareTo("") == 0){
				continue;
			}
			//System.out.println("word: " + bodyWords[i]);
			int spamWordCountBody = (spamWordsBody.get(bodyWords[i]) != null) ? spamWordsBody.get(bodyWords[i]) : 0;
			int hamWordCountBody = (hamWordsBody.get(bodyWords[i]) != null) ? hamWordsBody.get(bodyWords[i]) : 0;
			//compute the likelihood that this word is in a spam message
			double spamWordProbabilityBody = ((double)(spamWordCountBody + 1)/spamTotalBody);
			double spamProbPriorBody = Math.log(spamWordProbabilityBody);
			//compute the liklihood that this word is not a spam message
			double hamWordProbabilityBody = ((double)(hamWordCountBody + 1)/ hamTotalBody);
			double hamProbPriorBody = Math.log(hamWordProbabilityBody);	
			//compute numerator by adding logs
			double numeratorSpamBody = spamProbPriorBody + spamPrior;
			spamSumLikelihoodBody += numeratorSpamBody;
			double numeratorHamBody = hamProbPriorBody + hamPrior;
			hamSumLikelihoodBody += numeratorHamBody;

		}
		
		//SUBJECT
		//count the number of words in non-spam emails
		int hamTotalSub = 0;
		for(Map.Entry<String, Integer> entry : hamWordsSub.entrySet()){
			//System.out.printf("Key : %s and Value: %s %n", entry.getKey(), entry.getValue());
			hamTotalSub += entry.getValue();
		}
		int spamTotalSub = 0;
		for(Map.Entry<String, Integer> entry : spamWordsSub.entrySet()){
			spamTotalSub += entry.getValue();
		}
		//String subjectText = subject;
		String subjectText = subject.replaceAll("[^a-z0-9]", " "); //COMMENT OR UNCOMMENT THIS LINE WHEN SWITCHING CLASSIFIERS
		//System.out.println("Subject: " + subjectText);
		String[] subWords = subjectText.split(" ");
		double spamSumLikelihoodSub = 0;
		double hamSumLikelihoodSub = 0;
		for(int i = 0; i < subWords.length; i++){
			//skip all spaces
			if(subWords[i].compareTo("") == 0){
				continue;
			}
			//System.out.println("word: " + subWords[i]);
			int spamWordCountSub = (spamWordsSub.get(subWords[i]) != null) ? spamWordsSub.get(subWords[i]) : 0;
			int hamWordCountSub = (hamWordsSub.get(subWords[i]) != null) ? hamWordsSub.get(subWords[i]) : 0;
			//compute the likelihood that this word is in a spam message
			double spamWordProbabilitySub = ((double)(spamWordCountSub + 1)/spamTotalSub);
			double spamProbPriorSub = Math.log(spamWordProbabilitySub);
			//compute the liklihood that this word is not a spam message
			double hamWordProbabilitySub = ((double)(hamWordCountSub + 1)/ hamTotalSub);
			double hamProbPriorSub = Math.log(hamWordProbabilitySub);
			//compute numerator by adding logs
			double numeratorSpamSub = spamProbPriorSub + spamPrior;
			spamSumLikelihoodSub += numeratorSpamSub;
			double numeratorHamSub = hamProbPriorSub + hamPrior;
			hamSumLikelihoodSub += numeratorHamSub;
		}
		
		
		//print everything out at the end
		/*
		System.out.println("spamSumLikelihoodBody: " + spamSumLikelihoodBody);
		System.out.println("hamSumLikelihoodBody: " + hamSumLikelihoodBody);
		System.out.println();
		System.out.println("spamSumLikelihoodSub: " + spamSumLikelihoodSub);
		System.out.println("hamSumLikelihoodSub: " + hamSumLikelihoodSub);
		*/
		
		double spamSumLikelihoodTotal = spamSumLikelihoodBody + spamSumLikelihoodSub;
		double hamSumLikelihoodTotal = hamSumLikelihoodBody + hamSumLikelihoodSub;
		
		if(hamSumLikelihoodTotal <= spamSumLikelihoodTotal){
			return 1;
		}
		else if (hamSumLikelihoodTotal > spamSumLikelihoodTotal){
			return 0;
		}
		 
		return -1;
	}
}
