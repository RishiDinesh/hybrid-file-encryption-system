import java.io.*;
import java.nio.file.*;
import java.util.*;

class AESRunnable implements Runnable{

    AES aes;
    String mode, initvector;
    ArrayList<String> data;
    private volatile ArrayList<String> result;
    
    AESRunnable(String key, String m, ArrayList<String> d){
        aes = new AES(key);
        mode = m;
        data = d;
    }
    
    AESRunnable(String key, String m, ArrayList<String> d, String iv){
        aes = new AES(key);
        mode = m;
        data = d;
        initvector = iv;
    }

    public void run(){
        if(mode == "encrypt") result = aes.encrypt(data);
        else if (mode == "decrypt") result = aes.decrypt(data, initvector);
    }

    public ArrayList<String> getResult(){
        return result;
    }
}

class BlowfishRunnable implements Runnable{

    BlowFish blowfish;
    String mode, randbin;
    ArrayList<String> data;
    private volatile ArrayList<String> result;
    
    BlowfishRunnable(String key, String m, ArrayList<String> d){
        blowfish = new BlowFish(key);
        mode = m;
        data = d;
    }

    BlowfishRunnable(String key, String m, ArrayList<String> d, String rb){
        blowfish = new BlowFish(key);
        mode = m;
        data = d;
        randbin = rb;
    }

    public void run(){
        if(mode == "encrypt") result = blowfish.encrypt(data);
        else if (mode == "decrypt") result = blowfish.decrypt(data,randbin);
    }

    public ArrayList<String> getResult(){
        return result;
    }
}

public class App {

    static void encrypt(String inputFile, String[] keyArray, String outputFile) throws Exception{

        String input = Utils.stringToHex(new String(Files.readAllBytes(Paths.get(inputFile))));
        String[] data = input.split("(?<=\\G.{" + input.length()/2 + "})");

        ArrayList<String> AES_data = new ArrayList<>(Arrays.asList(data[0].split("(?<=\\G.{" + 32 + "})")));
        ArrayList<String> Blowfish_data = new ArrayList<>(Arrays.asList(data[1].split("(?<=\\G.{" + 16 + "})")));

        AESRunnable r1 =  new AESRunnable(keyArray[0],"encrypt",AES_data);
        Thread t1 = new Thread(r1);

        BlowfishRunnable r2 = new BlowfishRunnable(keyArray[1],"encrypt",Blowfish_data);
        Thread t2 = new Thread(r2);

        t1.start();
        t2.start();
        
        boolean completed;
        while (true) {
            completed = true;
            completed &= !t1.isAlive();
            completed &= !t2.isAlive();
            if (completed) break;
        }

        ArrayList<String> result = r1.getResult();
        result.addAll(r2.getResult());
        String encrypted_data = TranspositionCipher.encrypt(String.join("",result),keyArray);

        FileWriter out = new FileWriter(outputFile);
        out.write(encrypted_data);
        out.close();
    }

    static void decrypt(String inputFile, String[] keyArray, String outputFile) throws Exception{
        
        String input = new String(Files.readAllBytes(Paths.get(inputFile)));
        input = TranspositionCipher.decrypt(input,keyArray);
        
        String iv = input.substring(0, 32);
        String randbin = input.substring(input.length()-16);
        input = input.substring(32,input.length()-16);

        String[] data = input.split("(?<=\\G.{" + input.length()/2 + "})");

        ArrayList<String> AES_data = new ArrayList<>(Arrays.asList(data[0].split("(?<=\\G.{" + 32 + "})")));
        ArrayList<String> Blowfish_data = new ArrayList<>(Arrays.asList(data[1].split("(?<=\\G.{" + 16 + "})")));

        AESRunnable r1 =  new AESRunnable(keyArray[0],"decrypt",AES_data,iv);
        Thread t1 = new Thread(r1);

        BlowfishRunnable r2 = new BlowfishRunnable(keyArray[1],"decrypt",Blowfish_data,randbin);
        Thread t2 = new Thread(r2);

        t1.start();
        t2.start();

        boolean completed;
        while (true) {
            completed = true;
            completed &= !t1.isAlive();
            completed &= !t2.isAlive();
            if (completed) break;
        }

        ArrayList<String> result = r1.getResult();
        result.addAll(r2.getResult());
        String decrypted_data = Utils.hexToString(String.join("",result));

        FileWriter out = new FileWriter(outputFile);
        out.write(decrypted_data);
        out.close();
    }
    // 90% for 1 bit change in first key - AES
    // 92% for 1 bit change in second key - Blowfish
    // 92% for 1 bit change in both
    static void computeAvalancheEffect(String file1, String file2) throws IOException{
        
        String input1 = new String(Files.readAllBytes(Paths.get(file1)));
        String input2 = new String(Files.readAllBytes(Paths.get(file2)));
        int mismatch = 0;
        for(int i = 0; i < input1.length();i++) if(input1.charAt(i) != input2.charAt(i)) mismatch +=1;
        System.out.println("Avalance effect : " + (float)mismatch/input1.length()*100);
    }


    public static void main(String[] args) throws Exception {


        List<String> keyList = Files.readAllLines(Paths.get("data\\keyfile.txt"));
        String[] keyArray = keyList.toArray(new String[]{});

        long startTime = System.currentTimeMillis();

        if (args[0].equals("encrypt")) encrypt("data\\file.txt",keyArray,"data\\file.enc");
        
        else if(args[0].equals("decrypt")) decrypt("data\\file.enc",keyArray,"data\\file.dec");

        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");

        // computeAvalancheEffect("data\\file1.enc","data\\file2.enc");
    }
}   

