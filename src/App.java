import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;  
import java.util.Date;  
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

class BaseSystem{
    static void encrypt(String inputFile, String[] keyArray, String outputFile, String algo) throws Exception{

        String input = Utils.stringToHex(new String(Files.readAllBytes(Paths.get(inputFile))));

        int chunk_size = 0;
        if(algo == "AES") chunk_size = 32;
        else chunk_size = 16;

        ArrayList<String> list_data = new ArrayList<>(Arrays.asList(input.split("(?<=\\G.{" + chunk_size + "})")));

        AESRunnable r1 = null; BlowfishRunnable r2 = null;
        Thread t = null;
        if(algo == "AES"){
            r1 =  new AESRunnable(keyArray[0],"encrypt",list_data);
            t = new Thread(r1);
        }
        else{
            r2 = new BlowfishRunnable(keyArray[1],"encrypt",list_data);
            t = new Thread(r2);
        }
        t.start();
        
        boolean completed;
        while (true) {
            completed = true;
            completed &= !t.isAlive();
            if (completed) break;
        }

        ArrayList<String> result = null;
        if(algo == "AES") result = r1.getResult();
        else result = r2.getResult();

        String encrypted_data = String.join("",result).toLowerCase();

        FileWriter out = new FileWriter(outputFile);
        out.write(encrypted_data);
        out.close();
    }

    static void decrypt(String inputFile, String[] keyArray, String outputFile, String algo) throws Exception{
        
        String input = new String(Files.readAllBytes(Paths.get(inputFile)));

        String iv = null, randbin = null;
        if(algo == "AES"){
            iv = input.substring(0, 32);
            input = input.substring(32,input.length());
        }
        else{
            randbin = input.substring(input.length()-16);
            input = input.substring(0,input.length()-16);
        }

        int chunk_size = 0;
        if(algo == "AES") chunk_size = 32;
        else chunk_size = 16;

        ArrayList<String> list_data = new ArrayList<>(Arrays.asList(input.split("(?<=\\G.{" + chunk_size + "})")));

        AESRunnable r1 = null; BlowfishRunnable r2 = null;
        Thread t = null;
        if(algo == "AES"){
            r1 =  new AESRunnable(keyArray[0],"decrypt",list_data, iv);
            t = new Thread(r1);
        }
        else{
            r2 = new BlowfishRunnable(keyArray[1],"decrypt",list_data, randbin);
            t = new Thread(r2);
        }
        t.start();
        
        boolean completed;
        while (true) {
            completed = true;
            completed &= !t.isAlive();
            if (completed) break;
        }

        ArrayList<String> result = null;
        if(algo == "AES") result = r1.getResult();
        else result = r2.getResult();

        String decrypted_data = Utils.hexToString(String.join("",result));

        FileWriter out = new FileWriter(outputFile);
        out.write(decrypted_data);
        out.close();
    }
}

class ProposedSystem{

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
        // String encrypted_data = TranspositionCipher.encrypt(String.join("",result),keyArray);
        String encrypted_data = String.join("", result).toLowerCase();
        FileWriter out = new FileWriter(outputFile);
        out.write(encrypted_data);
        out.close();
    }
            // input = TranspositionCipher.decrypt(input,keyArray);

    static void decrypt(String inputFile, String[] keyArray, String outputFile) throws Exception{
        
        String input = new String(Files.readAllBytes(Paths.get(inputFile)));
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
}

class Metrics{

    static void computeAvalancheEffect(String file1, String file2) throws IOException{
        
        String input1 = new String(Files.readAllBytes(Paths.get(file1)));
        String input2 = new String(Files.readAllBytes(Paths.get(file2)));
        int mismatch = 0;
        for(int i = 0; i < input1.length();i++) if(input1.charAt(i) != input2.charAt(i)) mismatch +=1;
        float avalancheEffect = (float)mismatch/input1.length()*100;
        System.out.println("Avalance effect : " + avalancheEffect);
    }

    static void log(String filename, long startTime, long endTime, String[] args){
        
        long timeTaken = endTime - startTime;
        float throughput = (float)((new File(filename).length())/(timeTaken));
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
        Date date = new Date(); 
        String log = System.lineSeparator() + formatter.format(date) + " : " + String.join(" ",args) + " -tt " + timeTaken + " -tp " + throughput;
        
        Path p = Paths.get("data\\logfile.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(p, StandardOpenOption.APPEND)) {
            writer.write(log);
        } catch (IOException ioe) {
                System.err.format("IOException: %s%n", ioe);
        }
    }

}


public class App {
    public static void main(String[] args) throws Exception {

        Metrics.computeAvalancheEffect("data\\100Kb\\file1.enc", "data\\100Kb\\file2.enc");


        List<String> keyList = Files.readAllLines(Paths.get("data\\keyfile.txt"));
        String[] keyArray = keyList.toArray(new String[]{});

        String path = "data\\" + args[1] + "\\file"; 

        String plainfile = path + ".txt", encFile = path + ".enc", decFile = path + ".dec";

        long startTime = System.currentTimeMillis();

        if(args[2].equals("base")){

            if(args[3].equals("AES")){
                
                if (args[0].equals("encrypt")) BaseSystem.encrypt(plainfile,keyArray,encFile,"AES");

                else if(args[0].equals("decrypt")) BaseSystem.decrypt(encFile,keyArray,decFile,"AES");

            }else{
                
                if (args[0].equals("encrypt")) BaseSystem.encrypt(plainfile,keyArray,encFile,"Blowfish");
                
                else if(args[0].equals("decrypt")) BaseSystem.decrypt(encFile,keyArray,decFile,"Blowfish");

            }

        }else{
            
            if (args[0].equals("encrypt")) ProposedSystem.encrypt(plainfile,keyArray,encFile);
            
            else if(args[0].equals("decrypt")) ProposedSystem.decrypt(encFile,keyArray,decFile);

        }

        long endTime = System.currentTimeMillis();

        if (args[0].equals("encrypt")) Metrics.log(plainfile, startTime, endTime, args);
        else Metrics.log(encFile, startTime, endTime, args);
    }
}   

