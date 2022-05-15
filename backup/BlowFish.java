import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;

// blowfish - 64 bits = 8
// AES - 128 bits = 16

class BlowFish{

    String key = "7A24432646294A404E635266556A586E";
    static SecretKeySpec keySpec;
    static Cipher cipher;
    
    BlowFish(){
        try{
            byte[] keyData = key.getBytes();
            keySpec = new SecretKeySpec(keyData, "Blowfish");
            cipher = Cipher.getInstance("Blowfish");
        }catch(Exception e){
            System.out.print(e);
        }
    }

    String stringToHex(String str){
        StringBuffer sb = new StringBuffer();
        char ch[] = str.toCharArray();
        for(int i = 0; i < ch.length; i++) {
         String hexString = Integer.toHexString(ch[i]);
         sb.append(hexString);
        }
        String result = sb.toString();
        return result;
    }

    String hexToString(String hex){
        int l = hex.length();
        byte[] data = new byte[l / 2];
        for (int i = 0; i < l; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return new String(data);
    }

    void run(String filename, String mode){
        try {
            if(mode == "encrypt"){
                BufferedReader fileReader = new BufferedReader(new FileReader(filename));
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                FileWriter out = new FileWriter(filename.replaceFirst("[.][^.]+$", "") + ".enc");
                String line = fileReader.readLine();
                while(line != null){
                    String encrypted = new String(cipher.doFinal(line.getBytes()));
                    encrypted = stringToHex(encrypted);
                    out.write(encrypted + "\n");
                    line = fileReader.readLine();
                }
                fileReader.close();
                out.close();
            }
            else if(mode == "decrypt"){
                BufferedReader fileReader = new BufferedReader(new FileReader(filename));
                cipher.init(Cipher.DECRYPT_MODE, keySpec);
                FileWriter out = new FileWriter(filename.replaceFirst("[.][^.]+$", "") + ".dec");
                String line = fileReader.readLine();
                line = hexToString(line);
                while(line != null){
                    String decrypted = new String(cipher.doFinal(line.getBytes()));
                    out.write(decrypted + "\n");
                    line = fileReader.readLine();
                    line = hexToString(line);
                }
                fileReader.close();
                out.close();
            }
        } catch (Exception e) {
            System.out.print(e);
        }

    }
}



