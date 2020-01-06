
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class fileserver {

	// we get a server & directory
	private File serverDirectory;
	private DatagramChannel channel;
	private int windowsize = 2;
	private boolean ackpackets[];

	public fileserver(File Directory, DatagramChannel channel) throws IOException {

		serverDirectory = Directory;
		this.channel = channel;
		System.out.println(Directory.getName());
		run();
		channel.close();
	}

	public void run() throws IOException {
		ArrayList<String> command = new ArrayList<String>();
		

		ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
		
		for (;;) {
			
			buf.clear();
			SocketAddress router = channel.receive(buf);

			// Parse a packet from the received raw data.
			buf.flip();
			Packet packet = Packet.fromBuffer(buf);
			buf.flip();

			// first part of handshake
			if(packet.getType() == 2){
			System.out.println("part 1 of handshake is success!");
			Packet p = packet.toBuilder().setType(3).setSequenceNumber(0).setPayload("".getBytes()).create();
			channel.send(p.toBuffer(), router);
			}
			//third part of handshake
			if(packet.getType() == 1) {
				System.out.println("part 3 of handshake is success");
			}
	        //indicates maximum num of bytes for each packet
			String payload = new String(packet.getPayload(), UTF_8);

			System.out.println("Packet: " + packet.getSequenceNumber());
			System.out.println("Payload: " + payload);
			System.out.println("Router: " + router);
			System.out.println();

			// Send the response to the router not the client.
			// The peer address of the packet is the address of the client already.
			// We can use toBuilder to copy properties of the current packet.
			// This demonstrate how to create a new packet from an existing packet.
			if(!payload.contains("*")){
			command.add(payload);
			}
			String request = "";
			
			  request = payload;
			  
			  if (request.contains("..")) {
			  System.out.println("Error 300 - You cannot access parent directory!"); channel.close();
			  
			  }
			 
			//only runs if the last packet is empty
		if(payload.contains("*")){
		
			try {
				command(command, packet, router);
				command.clear();
			} catch (Exception e) {

				e.printStackTrace();
			
			}

		}
		}
	}

	// For Get /foo
	private void sendFile(String fileName, Packet outgoing, SocketAddress router) throws Exception {
		String response="";
		System.out.println("Printing a file");
		File file = new File(serverDirectory, fileName.trim());
	

		// check if the file is the directory & that it exists
		if ((!file.exists()) || file.isDirectory()) {
			response = "Error 404 - Requested file does not exist. " + fileName;
			Packet resp = outgoing.toBuilder().setPayload(response.getBytes()).create();
			channel.send(resp.toBuffer(), router);

		} else {
		
			System.out.println("GET File exists. Sending file to client ...");

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String s;

			while ((s = br.readLine()) != null) {
			response += s;
			}
			fr.close();
			
			selective(channel, router, response, outgoing);

			System.out.println("File transfered.");
		}
	}

	// POST
	private void getFile(String fileName, Packet outgoing, SocketAddress router, String content) throws Exception {
		System.out.println("POST Receiving file: " + fileName);
		
		// Check if file exists, then overwrite
		{
			File file = new File(serverDirectory, fileName);
			FileWriter writer = new FileWriter(file, false);
			PrintWriter output = new PrintWriter(writer);

			output.print(content);
			output.flush();
			output.close();
		}

		Packet resp = outgoing.toBuilder().setPayload("file created".getBytes()).create();
		channel.send(resp.toBuffer(), router);

		System.out.println("POST File uploaded. File name: " + fileName);

	}

	private void allfiles(PrintWriter out) {
		System.out.println("Printing list of files in directory");
		String[] listOfFiles = serverDirectory.list();

		for (int i = 0; i < listOfFiles.length; i++) {
			out.println(listOfFiles[i]);
		}

		out.println();
		out.flush();
		out.close();
	}

	// Interpret the client's command, i.e. user input
	private void command(ArrayList<String> command, Packet outgoing, SocketAddress router) throws Exception {
		StringBuilder commandString = new StringBuilder();
		for (String s : command) {
			commandString.append(s);
		}
		String command1 = commandString.toString();
		command1.replaceAll("\\s", "");
		
		System.out.println("This is the command string:" + command1);

		String response = "Response from server..";

		// parsing of the command

		String input = command1.toLowerCase();
		String fileName = "";
		String content = "";

		String[] parts = command1.split("\\s+");

		//Parsing commands
		// get method
		if (parts[0].equals("get") && parts[1].length() > 1) {
			fileName = parts[1].substring(1);

			System.out.println("GET request received. Filename is : " + fileName);
			String work = fileName.trim();
			
			sendFile(work, outgoing, router);
		}
		// post method
		if (input.startsWith("post")) {
			fileName = parts[1].substring(1);

			for (int i = 2; i < parts.length; i++) {

				content = content + parts[i] + " ";

			}

			System.out.println(content);
			System.out.println("POST request received. Filename is : " + fileName);
			String work = fileName.trim();
			getFile(work, outgoing, router, content);
		}
	}
	public static void selective(DatagramChannel channel,SocketAddress routerAddr, String response, Packet outgoing) {
		
		//get command from client 
			byte arr[] = response.getBytes();
			
			int packetsize = 10;
			int numofpackets = (arr.length/packetsize) + 1 ;
			
			Packet packetarr[] = new Packet[numofpackets];
			
			int from = 0;
			
			int to = packetsize;
			
			//create that number of packets by splitting the contents 
			for(int i = 0; i< numofpackets;i++){
				System.out.println("in the loop");
		
			byte[] array = Arrays.copyOfRange(arr, from, to);
			System.out.println("Creating packet " + i);
			
			Packet p = outgoing.toBuilder().setSequenceNumber(i).setPayload(array).create();
	
				packetarr[i] = p;
				from = to;
				to = to + packetsize;
				System.out.println("we sending packet now yo " + i );
				try {
					channel.send(packetarr[i].toBuffer(), routerAddr);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
						
			}
			
			//send empty packet to indicate the end
			Packet p = outgoing.toBuilder().setSequenceNumber(numofpackets+1).setPayload("*".getBytes()).create();
			try {
				channel.send(p.toBuffer(), routerAddr);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

			
		}
}