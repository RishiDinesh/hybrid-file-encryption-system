// changing the substitution function to a transposition-substituion 

import java.io.*;


public class MAES {

    String keyfile, key;
    int length;

    MAES(String k, int l){
        
        keyfile = k;
        length = l;

        try {
            
            BufferedReader keyReader = new BufferedReader(new FileReader(keyfile));

            key = keyReader.readLine();
            keyReader.close();
            
            if(key.length() * 4 != length)
                throw new Exception("Error: Attemping to use a " + key.length() * 4 + "-bit key with AES-"+ length);
            
        } catch (Exception e) { System.err.println(e.getMessage()); }

    }

    void run(String filename, String mode) throws IOException{
        
        BufferedReader fileReader = new BufferedReader(new FileReader(filename));
        
        if(mode =="encrypt"){

            String iv = Utils.generateIV();
            
            FileWriter out = new FileWriter(filename.replaceFirst("[.][^.]+$", "") + ".enc");
            out.write(iv + "\n"); 
            
            int numRounds = 10 + (((key.length() * 4 - 128) / 32));
            int[][] state, initvector = new int[4][4];
            int[][] keymatrix = Utils.keySchedule(key);
            
            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 4; j++) {
                    initvector[j][i] = Integer.parseInt(iv.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
                }
            }
            
            String line = fileReader.readLine();
            while(line !=null){
                
                if (line.matches("[0-9A-F]+")){
                    
                    if (line.length() < 32) {
                        line = String.format("%032x",Integer.parseInt(line, 16));
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
                        Utils.TransportSubBytes(state,Utils.subKey(keymatrix, i));
                        Utils.shiftRows(state); 
                        Utils.mixColumns(state);
                        Utils.addRoundKey(state, Utils.subKey(keymatrix, i));
                    }

                    Utils.TransportSubBytes(state,Utils.subKey(keymatrix, numRounds)); 
                    Utils.shiftRows(state); 
                    Utils.addRoundKey(state, Utils.subKey(keymatrix, numRounds));
                    
                    initvector = state;

                    out.write(Utils.MatrixToString(state) + "\n"); 
                    line = fileReader.readLine();
                } 
                else{
                    line = fileReader.readLine();
                }
            }
            out.close();
        }
        else if (mode == "decrypt"){
            
            FileWriter out = new FileWriter(filename.replaceFirst("[.][^.]+$", "")+ ".dec");

            String iv = fileReader.readLine();
            
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

            String line = fileReader.readLine();
            while(line != null){
                
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
                    Utils.InvTransportSubBytes(state,Utils.subKey(keymatrix, i));
                    Utils.addRoundKey(state, Utils.subKey(keymatrix, i));
                    Utils.invMixColumns(state);
                }

                Utils.invShiftRows(state);
                Utils.InvTransportSubBytes(state,Utils.subKey(keymatrix, 0));
                Utils.addRoundKey(state, Utils.subKey(keymatrix, 0));
                
                Utils.addRoundKey(state, initvector);
                Utils.deepCopy2DArray(initvector,nextvector);

                out.write(Utils.MatrixToString(state) + "\n");
                line = fileReader.readLine();
            }
            out.close();
        }
        fileReader.close();
    }
}

