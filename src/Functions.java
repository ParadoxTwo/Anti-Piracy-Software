import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;
import java.util.Scanner;


public class Functions {
	public static String bytesToHex(byte[] hash) throws Exception{
        StringBuilder hexString = new StringBuilder(100);
        String hex = "";
        int i;
        for (i = 0; i < hash.length; i++) {
            hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    private static String GetLinuxMotherBoard_serialNumber() {
        String command = "dmidecode -s baseboard-serial-number";
        String sNum = null;
        try {
            Process SerNumProcess = Runtime.getRuntime().exec(command);
            BufferedReader sNumReader = new BufferedReader(new InputStreamReader(SerNumProcess.getInputStream()));
            sNum = sNumReader.readLine().trim();
            SerNumProcess.waitFor();
            sNumReader.close();
        }
        catch (Exception ex) {
            System.err.println("Linux Motherboard Exp : "+ex.getMessage());
            sNum =null;
        }
        return sNum;
    }
    private static String getDriveSerialNumber(String drive) throws Throwable {
        String result = "";
        try {
        	if(!System.getProperty("os.name").contains("Windows")){
        		String sc = "/sbin/udevadm info --query=property --name=sda"; // get HDD parameters as non root user
                String[] scargs = {"/bin/sh", "-c", sc};

                Process p = Runtime.getRuntime().exec(scargs);
                p.waitFor();

                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream())); 
                String line;
                StringBuilder sb  = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    if (line.indexOf("ID_SERIAL_SHORT") != -1) { // look for ID_SERIAL_SHORT or ID_SERIAL
                        sb.append(line);
                    }    
                }

                return (sb.toString().substring(sb.toString().indexOf("=") + 1));
        	}
        else {
            
            File file = File.createTempFile("realhowto",".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    +"Set colDrives = objFSO.Drives\n"
                    +"Set objDrive = colDrives.item(\"" + drive + "\")\n"
                    +"Wscript.Echo objDrive.SerialNumber";  // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input =
                    new BufferedReader
                            (new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return result.trim();
    }
    private static String getBiosSerialNumber() throws Throwable{
        // wmic command for diskdrive id: wmic DISKDRIVE GET SerialNumber
        // wmic command for cpu id : wmic cpu get ProcessorId
        if(System.getProperty("os.name").contains("Windows")){
            Process process = Runtime.getRuntime().exec(new String[] { "wmic", "bios", "get", "serialnumber" });
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            sc.next();
            String biosSerial = sc.next();
            sc.close();
            return biosSerial;
        }
        else{
            return GetLinuxMotherBoard_serialNumber();
        }
    }
    public static String getFileHash(String[] files) throws Throwable{
        String FID = "";
        for (String s : files){
            File file = new File(s);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st, f = "";
            while ((st = br.readLine()) != null){
                f = f+st;
            }
            br.close();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(f.getBytes(StandardCharsets.UTF_8));
            FID = FID + bytesToHex(hash);
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(FID.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }
    private static String getMacAddress() throws Throwable{
    	if(System.getProperty("os.name").contains("Windows")){
	        InetAddress address = InetAddress.getLocalHost();
	        NetworkInterface nwi = NetworkInterface.getByInetAddress(address);
	        return bytesToHex(nwi.getHardwareAddress());
    	}
    	else {
    		NetworkInterface nwi = NetworkInterface.getByName("wlan0");
    	    if (nwi == null) {
    	    	nwi = NetworkInterface.getByName("eth0");
    	    }
    	    return bytesToHex(nwi.getHardwareAddress());
    	}
    }
    public static List<String> readFile(String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }
    public static String getHID() throws Throwable{
    	String biosSerial = getBiosSerialNumber();
        String mac = getMacAddress();
        String driveSerial = getDriveSerialNumber("C");
        String tmp = driveSerial+ mac +biosSerial;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(tmp.getBytes(StandardCharsets.UTF_8));
		return bytesToHex(hash);
    }
}
