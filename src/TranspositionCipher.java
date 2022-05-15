import java.util.*;

public class TranspositionCipher {

    public static String encrypt(String text, String[] keyArray) {

        String key = processKeyArray(keyArray);

        int[] arrange = arrangeKey(key);

        int lenkey = arrange.length;
        int lentext = text.length();

        int row = (int) Math.ceil((double) lentext / lenkey);

        char[][] grid = new char[row][lenkey];
        int z = 0;
        for (int x = 0; x < row; x++) {
            for (int y = 0; y < lenkey; y++) {
                if (lentext == z) {
                    grid[x][y] = 'G';
                    z--;
                } else {
                    grid[x][y] = text.charAt(z);
                }

                z++;
            }
        }
        String enc = "";
        for (int x = 0; x < lenkey; x++) {
            for (int y = 0; y < lenkey; y++) {
                if (x == arrange[y]) {
                    for (int a = 0; a < row; a++) {
                        enc = enc + grid[a][y];
                    }
                }
            }
        }
        return enc;
    }

    public static String decrypt(String text, String[] keyArray) {
        
        String key = processKeyArray(keyArray);

        int[] arrange = arrangeKey(key);

        int lenkey = arrange.length;
        int lentext = text.length();

        int row = (int) Math.ceil((double) lentext / lenkey);

        String regex = "(?<=\\G.{" + row + "})";
        String[] get = text.split(regex);

        char[][] grid = new char[row][lenkey];

        for (int x = 0; x < lenkey; x++) {
            for (int y = 0; y < lenkey; y++) {
                if (arrange[x] == y) {
                    for (int z = 0; z < row; z++) {
                        grid[z][y] = get[arrange[y]].charAt(z);
                    }
                }
            }
        }

        String dec = "";
        for (int x = 0; x < row; x++) {
            for (int y = 0; y < lenkey; y++) {
                if(grid[x][y] != 'G') dec = dec + grid[x][y];
            }
        }
        return dec;
    }

    public static int[] arrangeKey(String key) {
        //arrange position of grid
        String[] keys = key.split("");
        Arrays.sort(keys);
        int[] num = new int[key.length()];
        for (int x = 0; x < keys.length; x++) {
            for (int y = 0; y < key.length(); y++) {
                if (keys[x].equals(key.charAt(y) + "")) {
                    num[y] = x;
                    break;
                }
            }
        }
        return num;
    }

    public static String processKeyArray(String[] keyArray){

        String key = xorHex(keyArray[0],keyArray[1]);

        char ch = 'G';
        HashSet<Character> hs = new HashSet<Character>();
        StringBuilder sb = new StringBuilder();
        for (Character character : key.toCharArray()) {
            if(hs.add(character)){
                sb.append(character);
            }else{
                sb.append(ch);
                ch = (char)((int)ch + 1);
            }
        }
        String res = sb.toString();
        System.out.println("transposition key : " + res);
        return res;
    }

    public static String xorHex(String a, String b) {
        char[] chars = new char[a.length()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = toHex(fromHex(a.charAt(i)) ^ fromHex(b.charAt(i)));
        }
        return new String(chars);
    }
    
    public static int fromHex(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        }
        if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        }
        if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        throw new IllegalArgumentException();
    }
    
    public static char toHex(int nybble) {
        if (nybble < 0 || nybble > 15) {
            throw new IllegalArgumentException();
        }
        return "0123456789ABCDEF".charAt(nybble);
    }

}