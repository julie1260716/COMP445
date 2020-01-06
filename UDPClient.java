import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

public class UDPClient {

	private static String command = "";

	public static void main(String[] args) throws Exception {

		// ports and hosts specified in assignment
		String routerHost = "localhost";
		int routerPort = 3000;

		String serverHost = "localhost";
		int serverPort = 8008;

		SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
		InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);

		runClient(routerAddress, serverAddress);
	}

	//getting the client request + parsing user input
	private static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
		try (DatagramChannel channel = DatagramChannel.open()) {
		
			Packet hello1 = new Packet.Builder().setType(2).setSequenceNumber(0).setPortNumber(serverAddr.getPort())
					.setPeerAddress(serverAddr.getAddress()).setPayload("".getBytes()).create();
			channel.send(hello1.toBuffer(), routerAddr);
			System.out.println("SYN sent to server");
			
			//getting user input
			input();
			System.out.println("Starting to split packets");
			selective(serverAddr, channel, routerAddr);
			System.out.println("Finished selective method");
	
			channel.configureBlocking(false);
			Selector selector = Selector.open();
			channel.register(selector, OP_READ);
			System.out.println("Waiting for the response ");
			selector.select(5000);

			Set<SelectionKey> keys = selector.selectedKeys();
			if (keys.isEmpty()) {
				System.out.println("No response after timeout");
				return;
			}

			receive(channel);
			keys.clear();
		}
	}
	
	//testing 3 - way handshake 
	public static void receive(DatagramChannel channel ) throws IOException {
		ArrayList<String> response = new ArrayList<String>();
		String payload ="";
		ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);
		
		//do {
		for(;;){
			
			buf.clear();
			SocketAddress router = channel.receive(buf);

			// Parse a packet from the received raw data.
			buf.flip();
			Packet packet = Packet.fromBuffer(buf);
			
			buf.flip();
			
			if(packet.getType() == 3){
				System.out.println("part 2 of handshake is success!");
				Packet p = packet.toBuilder().setType(1).setSequenceNumber(0).setPayload("".getBytes()).create();
				channel.send(p.toBuffer(), router);
			}

			// first part of handshake
			System.out.println("part 1 of handshake is success!");
			
			 payload = new String(packet.getPayload(), UTF_8);
			 
			 response.add(payload);
			 //checking for last packet
			 if(payload.contains("*")){
				 for(int i = 0; i < response.size(); i++) {
					 System.out.print(response.get(i));
				 }
			 }
	
		}
	}
	

	// This method takes in the user input in order to know what to send to the
	// router which will send to the server
	public static void input() {
		Scanner scan = new Scanner(System.in);
		String input = "";
		System.out.println("Please input your command:");
		input = scan.nextLine();
		setCommand(input);
	}

	private static void setCommand(String c) {
		command = c;
		System.out.println("Command has been created: " + c);

	}

	static String getCommand() {
		return command;
	}
	//add selective to fileserver + getting packet info
	//
	public static void selective(InetSocketAddress serverAddr,DatagramChannel channel,SocketAddress routerAddr) throws IOException{
		
	//get command from client 
		byte arr[] = getCommand().getBytes();
		
		int packetsize = 10;
		//getting number of packets from packet size
		int numofpackets = (arr.length/packetsize) + 1;
		Packet packetarr[] = new Packet[numofpackets];
		boolean packets[] = new boolean[numofpackets];

		
		int from = 0;
		int to = packetsize;
		
		//create that number of packets by splitting the contents 
		for(int i = 0; i< numofpackets; i++){
			System.out.println("in the loop");
	
		byte[] array = Arrays.copyOfRange(arr, from, to);
		System.out.println("Creating packet " + i);
			Packet p = new Packet.Builder().setType(0).setSequenceNumber(i).setPortNumber(serverAddr.getPort())
					.setPeerAddress(serverAddr.getAddress()).setPayload(array).create();
			packetarr[i] = p;
			packets[i] = false;
			from = to;
			to = to + packetsize;
			System.out.println("Sending packet " + i );
			channel.send(packetarr[i].toBuffer(), routerAddr);	
			
		}
		
		//send empty packet to indicate end of packet
		Packet p = new Packet.Builder().setType(0).setSequenceNumber(numofpackets).setPortNumber(serverAddr.getPort())
				.setPeerAddress(serverAddr.getAddress()).setPayload("*".getBytes()).create();
		channel.send(p.toBuffer(), routerAddr);
		

		
	}

}
