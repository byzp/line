import java.net.*;
import java.io.*;
import java.util.*;

public class c extends Thread{
   private ServerSocket serverSocket;
   
   public c(int port) throws IOException{
      serverSocket = new ServerSocket(port);
      serverSocket.setSoTimeout(1000);
   }
 
   public void run(){
      while(true){
         try{
            Socket server = serverSocket.accept();
            System.out.println(server.getRemoteSocketAddress());
            DataInputStream in = new DataInputStream(server.getInputStream());
            System.out.println(in.readUTF());
            DataOutputStream out = new DataOutputStream(server.getOutputStream());
            Random r = new Random();
            int ii = r.nextInt(40000)+10000;
            while(run("/usr/bin/lsof"," -i:"+ii).equals("")==true){
                System.out.println(ii+" in used.");
                ii = r.nextInt(40000)+10000;
            }
            out.writeUTF(""+ii);
            System.out.println("use port:"+ii);
            server.close();
         }catch(SocketTimeoutException s){
            //System.out.println("Socket timed out!");
            //break;
         }catch(IOException e){
            e.printStackTrace();
            //break;
         }
      }
   }
   private static final String NEWLINE = System.getProperty("line.separator");

    /**
     * @param command the command to run
     * @return the output of the command
     * @throws IOException if an I/O error occurs
     */
   public static String run(String... command) throws IOException{
        ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder result = new StringBuilder(80);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))){
            while (true){
                String line = in.readLine();
                if (line == null)
                    break;
                result.append(line).append(NEWLINE);
            }
        }
        return result.toString();
    }
   public static void main(String [] args){
      int port = 6568;
      try{
         Thread t = new c(port);
         t.run();
      }catch(IOException e){
         e.printStackTrace();
      }
   }
}