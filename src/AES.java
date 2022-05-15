// changing the substitution function to a transposition-substituion 

import java.util.*;

public class AES {

    String key;
    String iv;

    AES(String k){
        key = k;
    }

    ArrayList<String> encrypt(ArrayList<String> data){
            
        ArrayList<String> encryptedData = new ArrayList<>();

        iv = Utils.generateIV();

        System.out.println("iv : " + iv);

        encryptedData.add(iv);
        
        int numRounds = 10 + (((key.length() * 4 - 128) / 32));
        int[][] state, initvector = new int[4][4];
        int[][] keymatrix = Utils.keySchedule(key);
        
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 4; j++) {
                initvector[j][i] = Integer.parseInt(iv.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
            }
        }

        for(String line : data){
                
            if (line.length() < 32) {
                line = String.join("", Collections.nCopies(32-line.length(), "0")) + line;
            }
            
            state = new int[4][4];

            for (int i = 0; i < 4; i++){
                for (int j = 0; j < 4; j++) {
                    state[j][i] = Integer.parseInt(line.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
                }
            }
            
            Utils.addRoundKey(state, initvector);
            Utils.addRoundKey(state, Utils.subKey(keymatrix, 0)); 

            for (int i = 1; i < numRounds; i++) {
                // Utils.subBytes(state);
                Utils.TransportSubBytes(state,Utils.subKey(keymatrix, i));
                Utils.shiftRows(state); 
                Utils.mixColumns(state);
                Utils.addRoundKey(state, Utils.subKey(keymatrix, i));
            }
            // Utils.subBytes(state);
            Utils.TransportSubBytes(state,Utils.subKey(keymatrix, numRounds)); 
            Utils.shiftRows(state); 
            Utils.addRoundKey(state, Utils.subKey(keymatrix, numRounds));
            
            initvector = state;

            encryptedData.add(Utils.MatrixToString(state)); 
        }
        return encryptedData;
    }

    ArrayList<String> decrypt(ArrayList<String> data, String iv){

        ArrayList<String> decryptedData = new ArrayList<>();
        System.out.println("iv : " + iv);
        
        int numRounds = 10 + (((key.length() * 4 - 128) / 32));
        int[][] state = new int[4][4];
        int[][] initvector = new int[4][4];
        int[][] nextvector = new int[4][4];
        int[][] keymatrix = Utils.keySchedule(key);

        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                initvector[j][i] = Integer.parseInt(iv.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
            }
        }        

        for(String line : data){
            
            state = new int[4][4];
            for (int i = 0; i < state.length; i++){
                for (int j = 0; j < state[0].length; j++) {
                    state[j][i] = Integer.parseInt(line.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
                }
            }
            
            Utils.deepCopy2DArray(nextvector,state);
            Utils.addRoundKey(state, Utils.subKey(keymatrix, numRounds));
            
            for (int i = numRounds - 1; i > 0; i--) {
                Utils.invShiftRows(state);
                // Utils.invSubBytes(state);
                Utils.InvTransportSubBytes(state,Utils.subKey(keymatrix, i));
                Utils.addRoundKey(state, Utils.subKey(keymatrix, i));
                Utils.invMixColumns(state);
            }

            Utils.invShiftRows(state);
            // Utils.invSubBytes(state);
            Utils.InvTransportSubBytes(state,Utils.subKey(keymatrix, 0));
            Utils.addRoundKey(state, Utils.subKey(keymatrix, 0));
            
            Utils.addRoundKey(state, initvector);
            Utils.deepCopy2DArray(initvector,nextvector);

            decryptedData.add(Utils.MatrixToString(state));
        }
        
        String lastElement = decryptedData.get(decryptedData.size()-1);
        decryptedData.set(decryptedData.size()-1, lastElement.replaceAll("^0+(?!$)", ""));
        
        return decryptedData;
    }
}

