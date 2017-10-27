/**
 * By Adrian Cuellar And Jason Kwok
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
            
            for(int packetN=0; packetN<12; packetN++) {
                System.out.println("Packet " + (packetN+1));
                // .5B + .5B + 1B+ 2B + 2B + 3/8B + 1 5/8 B + 1B + 1B + 2B + 4B + 4B + DATA
                byte[] sequence = new byte[20+(int) Math.pow(2,(packetN+1))];
                //Version + HLen
                sequence[0] = 0x45;
                //TOS
                sequence[1] = 0x00;
                //TOTAL LENGTH in sequence[2] and sequence[3] - Should end up being 20 + 2^Packet Number+1
                int size = (int) (20 + Math.pow(2,(packetN+1)));
                String hexSize = Integer.toHexString(size);
                if (hexSize.length() > 4)
                	hexSize = hexSize.substring(hexSize.length() - 4);
                else {
                    while (hexSize.length() < 4)
                    	hexSize = "0" + hexSize;
                }
                sequence[2] = (byte) Integer.parseInt(hexSize.substring(0, 2).toUpperCase(), 16);
                sequence[3] = (byte) Integer.parseInt(hexSize.substring(2).toUpperCase(), 16);
                //Identification
                sequence[4] = 0;
                sequence[5] = 0;
                //Flag assuming no fragmentation
                sequence[6] = 0x40;
                //Offset
                sequence[7] = 0;
                //TTL
                sequence[8] = 0x32;
                //Protocol
                sequence[9] = 0x06;
                //Source IP address
                sequence[12] = 0x11;
                sequence[13] = 0x11;
                sequence[14] = 0x11;
                sequence[15] = 0x11;
                //Server IP address
                sequence[16] = 0x12;
                sequence[17] = (byte) 0xDD;
                sequence[18] = (byte) 0x66;
                sequence[19] = (byte) 0xB6;
                //Rest is DATA, assuming it will be default byte value 0.

                // Copies all of the bytes in the packet except for the checksum and data to calculate the checksum.
                byte[] checkSumBytes=new byte[18];
                for(int i=0;i<18;i++){
                        if(i<10){
                            checkSumBytes[i]=sequence[i];
                        }
                        else{
                            checkSumBytes[i]=sequence[i+2];
                        }
                }
                int cSum = checksum(checkSumBytes);
                String hex = Integer.toHexString(cSum);
                if (hex.length() > 4)
                    hex = hex.substring(hex.length() - 4);
                else {
                    while (hex.length() < 4)
                        hex = "0" + hex;
                }
                sequence[10] = (byte) Integer.parseInt(hex.substring(0, 2).toUpperCase(), 16);
                sequence[11] = (byte) Integer.parseInt(hex.substring(2).toUpperCase(), 16);
                out.write(sequence);
                System.out.println(br.readLine());
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
