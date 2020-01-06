import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;

public class UDPServer {

	private static int port = 8007;
	private static String directory = "/Users/Julie/Desktop/test";
	private static boolean verbose;
	private static boolean defaultPort = true;
	private static boolean defaultDirectory = true;

	public static void main(String[] args) throws IOException {

		UDPServer server = new UDPServer();
		server.listenAndServe(port);
	}

	private void listenAndServe(int port) throws IOException {

		// Opens the channel
		serverInput();
		port = getPort();

		DatagramChannel channel = DatagramChannel.open();
		try {
			channel.bind(new InetSocketAddress(port));
			System.out.println("EchoServer is listening at " + channel.getLocalAddress());
			ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN).order(ByteOrder.BIG_ENDIAN);

			System.out.println("Directory:" + getDirectory());
			// create directory as file object
			File file = new File(directory);
			// takes in directory & connection
			new fileserver(file, channel);

		} finally {
			channel.close();
		}
	}

	public static void serverInput() {
		System.out.println("httpfs is a simple file server.\r\n" + "usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]\r\n"
				+ " -v Prints debugging messages.\r\n"
				+ " -p Specifies the port number that the server will listen and serve at.\r\n"
				+ " Default is 8080.\r\n" + " -d Specifies the directory that the server will use to read/write\r\n"
				+ "requested files. Default is the current directory when launching the\r\n" + "application.");

		Scanner scan = new Scanner(System.in);
		String input = "";
		input = scan.nextLine();

		// httpfs [-v] [-p PORT] [-d PATH-TO-DIR]
		String[] parts = input.split("\\s+");
		for (int i = 0; i < parts.length; i++) {

			// -v
			if (parts[i].equals("-v")) {
				verbose = true;
				System.out.println("Debugging message");

			} else {
				verbose = false;
			}

			// -p
			if (parts[i].equals("-p")) {
				String p1 = parts[i + 1];
				int p = Integer.parseInt(p1);
				System.out.println("port " + p);

				defaultPort = false;
				UDPServer.setPort(p);

			} else if (defaultPort) {
				UDPServer.setPort(8008);
			}
			
			// -d
			if (parts[i].equals("-d")) {
				String d = "";

				d = (parts[i + 1].toString());
				System.out.println("directory " + d);

				defaultDirectory = false;
				UDPServer.setDirectory(d);
			} else if (defaultDirectory) {
				UDPServer.setDirectory("/Users/Julie/Desktop/test");
			}

		}

	}

	private static void setPort(int port_) {
		port = port_;

	}

	private static int getPort() {
		return port;
	}

	private static void setDirectory(String directory_) {
		directory = directory_;

	}

	private static String getDirectory() {
		return directory;
	}

}
