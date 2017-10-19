/**
 * By Adrian Cuellar And Jason
 */

import java.io.*;
import java.net.Socket;
public final class Ipv4Client {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("18.221.102.182",38003)) {
            System.out.println("Connected to server.");
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            BufferedReader brIS = new BufferedReader(new InputStreamReader(System.in));
            PrintStream out = new PrintStream((socket.getOutputStream()),true,"UTF-8");
            for(int packetN=0;packetN<12;packetN++) {
                System.out.println("Packet " + packetN);
                // .5B + .5B + 1B+ 2B + 2B + 3/8B + 1 5/8 B + 1B + 1B + 2B + 4B + 4B + DATA
                byte[] sequence = new byte[20+(int) Math.pow(2,(packetN+1))];
                sequence[0] = 0x45;
                sequence[1]=0x00;

                sequence[4]=0;
                sequence[5]=0;
                sequence[6]=0x20;
                sequence[7]=0x32;
                sequence[8]=0x06;

                byte[] checkSumBytes;

                int cSum = checksum(checkSumBytes);
                String hex = Integer.toHexString(cSum);
                if (hex.length() > 4)
                    hex = hex.substring(hex.length() - 4);
                else {
                    while (hex.length() < 4)
                        hex = "0" + hex;
                }
                sequence = new byte[2];
                sequence[0] = (byte) Integer.parseInt(hex.substring(0, 2).toUpperCase(), 16);
                sequence[1] = (byte) Integer.parseInt(hex.substring(2).toUpperCase(), 16);

                out.write(sequence);
                System.out.println(is.read());
            }
            is.close();
            isr.close();
            br.close();
            brIS.close();
            socket.close();
            System.out.println("Disconnected from server.");
        }
    }
    public static short checksum(byte[] b){
        int cSum = 0;
        for(int i=0;i<b.length;i+=2){
            short one = (short) (b[i] & 0xFF);
            try {
                short two = (short) (b[i + 1] & 0xFF);
                cSum += ((256 * one) + two);
                if (cSum >= 65535) {
                    cSum -= (65535);
                }
            }
            catch (ArrayIndexOutOfBoundsException e){
                cSum+=(256*one);
                if (cSum >= 65535) {
                    cSum -= (65535);
                }
            }
        }
        return (short) ((~(cSum))& 0xFFFF);
    }
}
