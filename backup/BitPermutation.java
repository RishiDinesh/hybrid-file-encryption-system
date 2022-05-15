public class BitPermutation {
    
    public static void bitPermutation(int[][] arr){
        
        for(int i=0;i<4;i++){
            int[] column = new int[4];
            for(int j=0;j<4;j++) column[j] = arr[j][i];
            char[][] bitarr = getColumnBitArray(column);
            String binary = "";
            int k = 0;
            for(int m=0;m<8;m++){
                if(m%2==0 && m!=0){
                    arr[k][i] = Integer.parseInt(binary,2);
                    binary = "";
                    k++;
                }
                for(int n=0;n<4;n++) binary += Character.toString(bitarr[n][m]);
                if(m==7) arr[k][i] = Integer.parseInt(binary,2);
            }
        }
    }

    public static void inverseBitPermutation(int[][] arr){

        for(int i=0;i<4;i++){
            int[] row = new int[4];
            for(int j=0;j<4;j++) row[j] = arr[i][j];
            char[] bitarr = getRowBitArray(row);
            for(int m=0;m<4;m++){
                String binary = "";
                for(int n=0;n<7;n++) binary += Character.toString(bitarr[n*4 + m]);
                arr[i][m] = Integer.parseInt(binary,2);
            }
        }
    }

    public static char[] getRowBitArray(int[] row){
        
        char[] bitarr = new char[32];
        int offset = 0;
        for(int i=0;i<4;i++){
            String bin = Integer.toBinaryString(row[i]);
            bin = String.format("%8s", bin).replaceAll(" ", "0");
            char[] char_arr  = bin.toCharArray();
            for(int k=0;k<8;k++) bitarr[k + offset] = char_arr[k];
            offset += 8;
        }
        return bitarr;
    }

    public static char[][] getColumnBitArray(int[] column){

        char[][] bitarr = new char[4][8];
        for(int i=0;i<4;i++){
            String bin = Integer.toBinaryString(column[i]);
            bin = String.format("%8s", bin).replaceAll(" ", "0");
            char[] char_arr  = bin.toCharArray();
            for(int k=0;k<8;k++) bitarr[i][k] = char_arr[k];
        }
        return bitarr;
    }    
}
