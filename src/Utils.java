import java.nio.ByteBuffer;
import java.util.Random;

public class Utils{

    // AES Utils

    public static String generateIV(){
        
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < 32){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 32).toUpperCase();
    }

    public static void deepCopy2DArray(int[][] destination, int[][] source) {
        assert destination.length == source.length && destination[0].length == source[0].length;
        for(int i = 0; i < destination.length;i++)
        {
            System.arraycopy(source[i], 0, destination[i], 0, destination[0].length);
        }
    }

    public static int[][] subKey(int[][] km, int begin) {
        int[][] arr = new int[4][4];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length; j++) {
                arr[i][j] = km[i][4 * begin + j];
            }
        }
        return arr;
    }

    public static void subBytes(int[][] arr) {
        for (int i = 0; i < arr.length; i++) 
        {
            for (int j = 0; j < arr[0].length; j++) {
                int hex = arr[j][i];
                arr[j][i] = Constants.sbox[hex / 16][hex % 16];
            }
        }
    }

    public static void invSubBytes(int[][] arr) {
        for (int i = 0; i < arr.length; i++) 
        {
            for (int j = 0; j < arr[0].length; j++) {
                int hex = arr[j][i];
                arr[j][i] = Constants.invsbox[hex / 16][hex % 16];
            }
        }
    }

    // start of modified SubBytes

        // for(int i=0;i<4;i++){
        //     int xorKey = roundkey[i][0] ^ roundkey[i][1] ^ roundkey[i][2] ^ roundkey[i][3];
        //     for(int j=0;j<4;j++) arr[i][j] ^= xorKey;
        // }

                // for(int i=0;i<4;i++){
        //     int xorKey = roundkey[i][0] ^ roundkey[i][1] ^ roundkey[i][2] ^ roundkey[i][3];
        //     for(int j=0;j<4;j++) arr[i][j] ^= xorKey;
        // }


    public static int Transport(int state ) {
        int temp = state ;
        return ((temp & 0x0F)<<4 | (temp & 0xF0)>>4); 
    }
        
    public static void TransportSubBytes(int[][] arr, int[][] roundkey){ 
        for (int row = 0; row < 4; row++) 
            for (int col = 0; col < 4; col++) 
                arr[row][col] = Transport(arr[row][col]); 
        for (int row = 0; row < 4; row++){ 
            for (int col = 0; col < 4; col++){
                int hex = arr[col][row];
                arr[col][row] = Constants.sbox[hex / 16][hex % 16];
            }
        }
    }

    public static void InvTransportSubBytes(int[][] arr, int[][] roundkey) {
        for (int row = 0; row < 4; row++){
            for (int col = 0; col < 4; col++){
                int hex = arr[col][row];
                arr[col][row] = Constants.invsbox[hex / 16][hex % 16]; 
            }
        }
        for (int row = 0; row < 4; row++) 
            for (int col = 0; col < 4; col++) 
                arr[row][col] = Transport(arr[row][col]); 
    }

    // end of modified SubBytes

    public static void shiftRows(int[][] arr) {
        for (int i = 1; i < arr.length; i++) {
            arr[i] = leftrotate(arr[i], i);
        }
    }

    public static int[] leftrotate(int[] arr, int times)
    {
        assert(arr.length == 4);
        if (times % 4 == 0) {
            return arr;
        }
        while (times > 0) {
            int temp = arr[0];
            for (int i = 0; i < arr.length - 1; i++) {
                arr[i] = arr[i + 1];
            }
            arr[arr.length - 1] = temp;
            --times;
        }
        return arr;
    }

    public static void invShiftRows(int[][] arr) {
        for (int i = 1; i < arr.length; i++) {
            arr[i] = rightrotate(arr[i], i);
        }
    }

    public static int[] rightrotate(int[] arr, int times) {
        if (arr.length == 0 || arr.length == 1 || times % 4 == 0) {
            return arr;
        }
        while (times > 0) {
            int temp = arr[arr.length - 1];
            for (int i = arr.length - 1; i > 0; i--) {
                arr[i] = arr[i - 1];
            }
            arr[0] = temp;
            --times;
        }
        return arr;
    }

      // start of modified ShiftRows

      public static boolean contains(int[] arr, int value){
          for(int i=0;i<arr.length;i++){
              if(arr[i] == value) return true;
          }
          return false;
      }

      public static int[] getRankNumber(int[][] arr, int[][] roundkey){
        
        int[] RVal = new int[4];
        for(int i=0;i<4;i++){
            int[] SKeyVector = new int[4];
            for(int j=0;j<4;j++) SKeyVector[j] = arr[i][j] ^ roundkey[i][j];  
            RVal[i] = SKeyVector[0] ^ SKeyVector[1] ^ SKeyVector[2] ^ SKeyVector[3];
        }

        int[] sort_idx = {-1,-1,-1,-1};
        for(int i=0;i<4;i++){
            int smallest = 1000, idx = 100;
            for(int j=0;j<4;j++){
                if(RVal[j]<smallest && !contains(sort_idx,j)){
                    smallest = RVal[j];
                    idx = j; 
                }
            }
            sort_idx[i] = idx;
        }
        int[] RNo = {-1,-1,-1,-1};
        for(int i=0;i<4;i++)
            RNo[sort_idx[i]] = i;
            
        return RNo;
    }

    public static void modifiedShiftRows(int[][] arr, int[][] roundkey){

        int[] RNo = getRankNumber(arr, roundkey);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = leftrotate(arr[i], RNo[i]);
        }
    }

    public static void modifiedInvShiftRows(int[][] arr, int[][] roundkey){
        
        int[] RNo = getRankNumber(arr, roundkey);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = rightrotate(arr[i], RNo[i]);
        }
    }

    // end of modified ShiftRows

    public static void mixColumns(int[][] arr) 
    {
        int[][] tarr = new int[4][4];
        for(int i = 0; i < 4; i++)
        {
            System.arraycopy(arr[i], 0, tarr[i], 0, 4);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                arr[i][j] = mcHelper(tarr, Constants.galois, i, j);
            }
        }
    }

    public static int mcHelper(int[][] arr, int[][] g, int i, int j)
    {
        int mcsum = 0;
        for (int k = 0; k < 4; k++) {
            int a = g[i][k];
            int b = arr[k][j];
            mcsum ^= mcCalc(a, b);
        }
        return mcsum;
    }

    public static int mcCalc(int a, int b) 
    {
        if (a == 1) {
            return b;
        } else if (a == 2) {
            return Constants.mc2[b / 16][b % 16];
        } else if (a == 3) {
            return Constants.mc3[b / 16][b % 16];
        }
        return 0;
    }

    public static void invMixColumns(int[][] arr) {
        int[][] tarr = new int[4][4];
        for(int i = 0; i < 4; i++)
        {
            System.arraycopy(arr[i], 0, tarr[i], 0, 4);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                arr[i][j] = invMcHelper(tarr, Constants.invgalois, i, j);
            }
        }
    }

    public static int invMcHelper(int[][] arr, int[][] igalois, int i, int j)
    {
        int mcsum = 0;
        for (int k = 0; k < 4; k++) {
            int a = igalois[i][k];
            int b = arr[k][j];
            mcsum ^= invMcCalc(a, b);
        }
        return mcsum;
    }

    public static int invMcCalc(int a, int b) 
    {
        if (a == 9) {
            return Constants.mc9[b / 16][b % 16];
        } else if (a == 0xb) {
            return Constants.mc11[b / 16][b % 16];
        } else if (a == 0xd) {
            return Constants.mc13[b / 16][b % 16];
        } else if (a == 0xe) {
            return Constants.mc14[b / 16][b % 16];
        }
        return 0;
    }

    public static int[] schedule_core(int[] in, int rconpointer) {
        in = leftrotate(in, 1);
        int hex;
        for (int i = 0; i < in.length; i++) {
            hex = in[i];
            in[i] = Constants.sbox[hex / 16][hex % 16];
        }
        in[0] ^= Constants.rcon[rconpointer];
        return in;
    }

    public static int[][] keySchedule(String key)
    {

        int binkeysize = key.length() * 4;
        int colsize = binkeysize + 48 - (32 * ((binkeysize / 64) - 2)); 
        int[][] keyMatrix = new int[4][colsize / 4]; 
        int rconpointer = 1;
        int[] t = new int[4];
        final int keycounter = binkeysize / 32;
        int k;

        for (int i = 0; i < keycounter; i++) 
        {
            for (int j = 0; j < 4; j++) {
                keyMatrix[j][i] = Integer.parseInt(key.substring((8 * i) + (2 * j), (8 * i) + (2 * j + 2)), 16);
            }
        }
        int keypoint = keycounter;
        while (keypoint < (colsize / 4)) {
            int temp = keypoint % keycounter;
            if (temp == 0) {
                for (k = 0; k < 4; k++) {
                    t[k] = keyMatrix[k][keypoint - 1];
                }
                t = schedule_core(t, rconpointer++);
                for (k = 0; k < 4; k++) {
                    keyMatrix[k][keypoint] = t[k] ^ keyMatrix[k][keypoint - keycounter];
                }
                keypoint++;
            } else if (temp == 4) {
                for (k = 0; k < 4; k++) {
                    int hex = keyMatrix[k][keypoint - 1];
                    keyMatrix[k][keypoint] = Constants.sbox[hex / 16][hex % 16] ^ keyMatrix[k][keypoint - keycounter];
                }
                keypoint++;
            } else {
                int ktemp = keypoint + 3;
                while (keypoint < ktemp) {
                    for (k = 0; k < 4; k++) {
                        keyMatrix[k][keypoint] = keyMatrix[k][keypoint - 1] ^ keyMatrix[k][keypoint - keycounter];
                    }
                    keypoint++;
                }
            }
        }
        return keyMatrix;
    }

    public static void addRoundKey(int[][] bytematrix, int[][] keymatrix)
    {
        for (int i = 0; i < bytematrix.length; i++) {
            for (int j = 0; j < bytematrix[0].length; j++) {
                bytematrix[j][i] ^= keymatrix[j][i];
            }
        }
    }

    public static String MatrixToString(int[][] m)
    {
        String t = "";
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                String h = Integer.toHexString(m[j][i]).toUpperCase();
                if (h.length() == 1) {
                    t += '0' + h;
                } else {
                    t += h;
                }
            }
        }
        return t;
    }

    // Blowfish Utils

    public static String hexToBin(String plainText){
		String binary = "", binary4B;
		Long num;
		int n = plainText.length();
		for (int i = 0; i < n; i++) {
            num = Long.parseUnsignedLong( plainText.charAt(i) + "", 16);
            binary4B = Long.toBinaryString(num);
			binary4B = "0000" + binary4B;
			binary4B = binary4B.substring(binary4B.length() - 4);
			binary += binary4B;
		}
		return binary;
	}
	
    public static String binToHex(String plainText){
		long num = Long.parseUnsignedLong(plainText, 2);
		String hexa = Long.toHexString(num);
		while (hexa.length() < (plainText.length() / 4))
			hexa = "0" + hexa;
		return hexa;
	}

	public static String xor(String a, String b){
		a = hexToBin(a);
		b = hexToBin(b);
		String ans = "";
		for (int i = 0; i < a.length(); i++)
			ans += (char)(((a.charAt(i) - '0') ^ (b.charAt(i) - '0')) + '0');
		ans = binToHex(ans);
		return ans;
    }

	public static String addBin(String a, String b){
		String ans = "";
		long n1 = Long.parseUnsignedLong(a, 16);
		long n2 = Long.parseUnsignedLong(b, 16);
		n1 = (n1 + n2) % (2^32);
		ans = Long.toHexString(n1);
		ans = "00000000" + ans;
		return ans.substring(ans.length() - 8);
	}

	public static String f(String plainText){
		String a[] = new String[4];
		String ans = "";
		for (int i = 0; i < 8; i += 2) {
			long col = Long.parseUnsignedLong(hexToBin(plainText.substring(i, i + 2)),2);
			a[i / 2] = Constants.S[i / 2][(int)col];
		}
		ans = addBin(a[0], a[1]);
		ans = xor(ans, a[2]);
		ans = addBin(ans, a[3]);
		return ans;
	}

	public static void keyGenerate(String key){
		int j = 0;
		for (int i = 0; i < Constants.P.length; i++) {
			Constants.P[i] = xor(Constants.P[i], key.substring(j, j + 8));
			j = (j + 8) % key.length();
		}
	}

	public static String round(int time, String plainText){
		String left, right;
		left = plainText.substring(0, 8);
		right = plainText.substring(8, 16);
		left = xor(left, Constants.P[time]);
		String fOut = f(left);
		right = xor(fOut, right);
		return right + left;
	}

    // start of modified blowfish

    public static String modified_f(String plainText, char flag){
		String a[] = new String[4];
		String ans = "";
		for (int i = 0; i < 8; i += 2) {
			long col = Long.parseUnsignedLong(hexToBin(plainText.substring(i, i + 2)),2);
			a[i / 2] = Constants.S[i / 2][(int)col];
		}
        if(flag == '1'){
		    ans = xor(a[0], a[1]);
		    ans = addBin(ans, a[2]);
		    ans = xor(ans, a[3]);
        }
        else if(flag == '0'){
            String L = addBin(a[0],a[2]);
            String R = addBin(a[1], a[3]);
            ans = xor(L, R);
        }
		return ans;
	}

    public static String modified_round(int time, String plainText, char flag){
		String left, right;
		left = plainText.substring(0, 8);
		right = plainText.substring(8, 16);
		left = xor(left, Constants.P[time]);
		String fOut = modified_f(left,flag);
		right = xor(fOut, right);
		return right + left;
	}

    // end of modified blowfish

    
    // File Utils

    public static String stringToHex(String str){
        
        byte[] barr = str.getBytes();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<barr.length;i++) sb.append(String.format("%02X", barr[i]));
        return sb.toString();
        
    }

    public static String hexToString(String hex){
        
        String[] list=hex.split("(?<=\\G.{2})");
        ByteBuffer buffer= ByteBuffer.allocate(list.length);
        for(String str: list) buffer.put(Byte.parseByte(str,16));
        return new String(buffer.array());

        // String str = "";
        // for(int i=0;i<hex.length();i+=2)
        // {
        //     String s = hex.substring(i, (i + 2));
        //     int decimal = Integer.parseInt(s, 16);
        //     str = str + (char) decimal;
        // }       
        // return str;
    }
}