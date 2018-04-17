import javax.swing.JFrame;
import org.omg.PortableServer.THREAD_POLICY_ID;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.net.*;
import java.io.*;
import java.security.*;
import java.nio.charset.*;

public class Main {
	
	public static void main(String[] args) throws Throwable{

		//Creating the window with all its awesome snaky features
		String HID = "";
		try {
			 HID = Functions.getHID();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
        String workingDir; 
        if(System.getProperty("os.name").contains("Windows"))
        	workingDir = Functions.readFile("C:/ProgramData/AntiP/info.txt").get(0);
        else
        	workingDir = Functions.readFile("/home/"+System.getProperty("user.name")+"/Documents/AntiP/info.txt").get(0);
        String files[] = {workingDir+"/user.ini"};//, workingDir+"/SnakeGame.exe"};
        String FID = Functions.getFileHash(files);
        String tHID, tFID;
        HttpService httpService = new HttpService("https://ropsten.infura.io/n5AaYOM9oh6VYyME5yV3");
        Web3j web3 = Web3j.build(httpService);
        Web3ClientVersion web3ClientVersion = web3.web3ClientVersion().sendAsync().get();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        System.out.println(clientVersion);
        //txAddress is user's address... if you use vendor's address here, the functions won't be called upon...
        String txAddress = Functions.readFile(workingDir+"/user.ini").get(0);//"0x6040B6E5a5306f705358D07b79487908b57001A6";//"0x158386314f1dbf4760e2c8bc604ac2c3feea5a2e";//readFile("F:/address.txt").get(0);
        String contractAddress = Functions.readFile(workingDir+"/user.ini").get(1);;
        List<org.web3j.abi.datatypes.Type> inputParametersHID = new ArrayList<>();
        Function getHID = new Function(
                "getHID",            // the name of function to call on
                inputParametersHID,  // Solidity Types in smart contract functions
                Arrays.asList(new TypeReference<Utf8String>() {})); // the return type of function..
        String encodedFunction = FunctionEncoder.encode(getHID);
        org.web3j.protocol.core.methods.response.EthCall responseH = web3.ethCall(
                Transaction.createEthCallTransaction(txAddress,
                        contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();
        List<org.web3j.abi.datatypes.Type> someType = FunctionReturnDecoder.decode(responseH.getValue(),getHID.getOutputParameters());
        Iterator<org.web3j.abi.datatypes.Type> it = someType.iterator();
        org.web3j.abi.datatypes.Type result = someType.get(0);
        tHID = result.getValue().toString();
        List<org.web3j.abi.datatypes.Type> inputParametersFID = new ArrayList<>();
        Function getFID = new Function(
                "getSoftwareHash",
                inputParametersFID,  // Solidity Types in smart contract functions
                Arrays.asList(new TypeReference<Utf8String>() {}));
        encodedFunction = FunctionEncoder.encode(getFID);
        org.web3j.protocol.core.methods.response.EthCall responseF = web3.ethCall(
                Transaction.createEthCallTransaction(txAddress,
                        contractAddress, encodedFunction),
                DefaultBlockParameterName.LATEST)
                .sendAsync().get();
        someType = FunctionReturnDecoder.decode(responseF.getValue(),getFID.getOutputParameters());
        it = someType.iterator();
        result = someType.get(0);
        tFID = result.getValue().toString();
        System.out.println("Verifying File Hash...");
        System.out.println("DEBUG - calculated FID is "+FID);
        System.out.println("DEBUG - FID from Smart Contract is: "+tFID);
        if(tFID.equals(FID)) {
        	System.out.println("File hash verified.");
        	System.out.println("Verifying Hardware ID...");
            System.out.println("DEBUG - calculated HID is "+HID);
            System.out.println("DEBUG - HID from Smart Contract is: "+tHID);
            if(tHID.equals(HID)) {
            	System.out.println("Hardware ID verified.");
            }
            else {
            	System.out.println("Can't run on an unregistered device.");
            }
        }
        else {
        	System.out.println("Files have been tampered with.");
        }
        if(FID.equals(tFID)&&HID.equals(tHID)){
            System.out.println("Verification Successful!");
        }
        else{
            System.out.println("Verification Failed... Quiting");
            return;
        }

		Window f1= new Window();
		
		//Setting up the window settings
		f1.setTitle("Snake");
		f1.setSize(300,300);
		f1.setVisible(true);
		f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);             

	}
}
