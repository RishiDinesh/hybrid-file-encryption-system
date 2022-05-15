import java.util.*;

public class BlowFish{
    
    BlowFish(String key){
        Utils.keyGenerate(key);
    }

	ArrayList<String> encrypt(ArrayList<String> data){

        ArrayList<String> encryptedData = new ArrayList<String>();

        Random rand = new Random();
        int seed = rand.nextInt(65536);
        String randbin = Integer.toBinaryString(seed);
        randbin = String.join("", Collections.nCopies(16-randbin.length(), "0")) + randbin;
        char[] flag = randbin.toCharArray();

        System.out.println("randbin : " + randbin);

        for(String line : data){

            if (line.length() < 16) {
                line = String.join("", Collections.nCopies(16-line.length(), "0")) + line;
            }

            for (int i = 0; i < 16; i++)
            line = Utils.modified_round(i, line, flag[i]);
            // line = Utils.round(i, line);

            String right = line.substring(0, 8);
            String left = line.substring(8, 16);
            right = Utils.xor(right, Constants.P[16]);
            left = Utils.xor(left, Constants.P[17]);
            encryptedData.add(left + right);
        }
        encryptedData.add(randbin);
        return encryptedData;
	}
    
    ArrayList<String> decrypt(ArrayList<String> data, String randbin){

        ArrayList<String> decryptedData = new ArrayList<String>();
        char[] flag = randbin.toCharArray();

        System.out.println("randbin : " + randbin);
        
        for(String line : data){
           
            for (int i = 17; i > 1; i--)
            // line = Utils.round(i, line);
            line = Utils.modified_round(i, line,flag[i-2]);

            String right = line.substring(0, 8);
            String left = line.substring(8, 16);
            right = Utils.xor(right, Constants.P[1]);
            left = Utils.xor(left, Constants.P[0]);
            decryptedData.add(left + right);
        }

        String lastElement = decryptedData.get(decryptedData.size()-1);
        decryptedData.set(decryptedData.size()-1, lastElement.replaceAll("^0+(?!$)", ""));

        return decryptedData;
    }
}

