package java_video_stream;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.*;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;


public class JavaServer {

	
	
	//------------------------------------------------------------------------
	// Atributos
	//------------------------------------------------------------------------
	
	public static InetAddress[] inet;
	public static int[] port;
	public static int i;
	static int count = 0;
	public static BufferedReader[] inFromClient;
	public static DataOutputStream[] outToClient;
	public Vidthread videito;
	
	public static void main(String[] args) throws Exception
	{
		JavaServer jv = new JavaServer();
	}
	
	
		public JavaServer() throws Exception {
		
		
		NativeLibrary.addSearchPath("libvlc",  System.getenv("ProgramFiles") + "\\VideoLAN\\VLC");
//		System.setProperty("jna-library.path", System.getenv("ProgramFiles") + "\\VideoLAN\\VLC");

		JavaServer.inet = new InetAddress[30];
		
		port = new int[30];

		ServerSocket welcomeSocket = new ServerSocket(8082); // AQUI SE CAMBIO
		System.out.println(welcomeSocket.isClosed());
		Socket connectionSocket[] = new Socket[30];
		inFromClient = new BufferedReader[30];
		outToClient = new DataOutputStream[30];

		DatagramSocket serv = new DatagramSocket(4321);

		byte[] buf = new byte[62000];
		
		DatagramPacket dp = new DatagramPacket(buf, buf.length);
		
		dp.setPort(8082);

		Canvas_Demo canv = new Canvas_Demo();
		System.out.println("Gotcha");

		i = 0;
		
		SThread[] st = new SThread[30];
		

		while (true) {

			System.out.println(serv.getPort());
			serv.receive(dp);
			System.out.println(new String(dp.getData()));
			buf = "starts".getBytes();

			inet[i] = dp.getAddress();
			port[i] = dp.getPort();

			DatagramPacket dsend = new DatagramPacket(buf, buf.length, inet[i], port[i]);
			serv.send(dsend);

			Vidthread sendvid = new Vidthread(serv);

			System.out.println("waiting\n ");
			connectionSocket[i] = welcomeSocket.accept();
			System.out.println("connected " + i);

			inFromClient[i] = new BufferedReader(new InputStreamReader(connectionSocket[i].getInputStream()));
			outToClient[i] = new DataOutputStream(connectionSocket[i].getOutputStream());
			outToClient[i].writeBytes("Connected: from Server\n");

			
			st[i] = new SThread(i);
			st[i].start();
			
			if(count == 0)
			{
				Sentencefromserver sen = new Sentencefromserver();
				sen.start();
				count++;
			}

			System.out.println(inet[i]);
			sendvid.start();

			i++;

			if (i == 30) {
				break;
			}
		}
	}
}

class Vidthread extends Thread {

	int clientno;
	public Canvas_Demo canvitas;
	JFrame jf = new JFrame("scrnshots before sending");
	JLabel jleb = new JLabel();
	Robot rb = new Robot();
	DatagramSocket soc;

	

	byte[] outbuff = new byte[62000];

	BufferedImage mybuf;
	ImageIcon img;
	Rectangle rc;
	
	int bord = Canvas_Demo.panel.getY() - Canvas_Demo.frame.getY();
	
	// Rectangle rv = new Rectangle(d);
	public Vidthread(DatagramSocket ds) throws Exception {
		soc = ds;

		System.out.println(soc.getPort());
		jf.setSize(500, 400);
		jf.setLocation(500, 400);
		jf.setVisible(true);
	}
	

	public void run() {
		while (true) {
			try {

				int num = JavaServer.i;

				rc = new Rectangle(new Point(Canvas_Demo.frame.getX() + 8, Canvas_Demo.frame.getY() + 27),
						new Dimension(Canvas_Demo.panel.getWidth(), Canvas_Demo.frame.getHeight() / 2));

				// System.out.println("another frame sent ");

				mybuf = rb.createScreenCapture(rc);

				img = new ImageIcon(mybuf);

				jleb.setIcon(img);
				jf.add(jleb);
				jf.repaint();
				jf.revalidate();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				ImageIO.write(mybuf, "jpg", baos);
				
				outbuff = baos.toByteArray();

				for (int j = 0; j < num; j++) {
					DatagramPacket dp = new DatagramPacket(outbuff, outbuff.length, JavaServer.inet[j],
							JavaServer.port[j]);
					//System.out.println("Frame Sent to: " + JavaServer.inet[j] + " port: " + JavaServer.port[j]
						//	+ " size: " + baos.toByteArray().length);
					soc.send(dp);
					baos.flush();
				}
				Thread.sleep(15);

				// baos.flush();
				// byte[] buffer = baos.toByteArray();
			} catch (Exception e) {

			}
		}

	}

}

class Canvas_Demo {

	// Create a media player factory
	//private MediaPlayerFactory mediaPlayerFactory;

	// Create a new media player instance for the run-time platform
	public EmbeddedMediaPlayer mediaPlayer;

	public static JPanel panel;
	public static JPanel myjp;
	private Canvas canvas;
	public static JFrame frame;
	public static JTextArea ta;
	public static JTextArea txinp;
	public static int xpos = 0, ypos = 0;
	String url = "D:\\DownLoads\\Video\\freerun.MP4";
	
	
	// Botones de pausa y play
	public Button pause;
	public Button play;

	// Constructor
	public Canvas_Demo() {

		// Creación del panel de reproducción
		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel mypanel = new JPanel();
		mypanel.setLayout(new GridLayout(2, 1));

		// Creating the canvas and adding it to the panel:
		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		
		// canvas.setSize(640, 480);
		panel.add(canvas, BorderLayout.CENTER);

		// Creation a media player :
		MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
		CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
		mediaPlayer.setVideoSurface(videoSurface);

		// Construction of the jframe :
		frame = new JFrame("Streaming Lab Redes");
		
		// frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200, 0);
		frame.setSize(640, 960);
		frame.setAlwaysOnTop(true);
		mypanel.add(panel);
		frame.add(mypanel);
		
//		myjp = new JPanel(new GridLayout(1, 1));
//		Button bna = new Button("Pausa");
//		myjp.add(bna);
		
		frame.setVisible(true);
		xpos = frame.getX();
		ypos = frame.getY();
		
		pause = new Button("Pause");
		play = new Button("Play");
		
		// Reproducción del video:
		mediaPlayer.playMedia("./Descargas/VID-20190324-WA0012.mp4");
		
		// BOTONES
		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayer.pause();
			}
		});
		
		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayer.play();	
			}
		});
	}
	
	
	public void pausarVideo() {
		pause.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayer.pause();
			}
		});
	}
	
	public void playVideo(){
		play.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayer.play();	
			}
		});
	}
	
	
//	public void ReplayVideo(){
//		play.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				mediaPlayer.release();	
//			}
//		});
//	}
}

class SThread extends Thread {

	public static String clientSentence;
	int srcid;
	BufferedReader inFromClient = JavaServer.inFromClient[srcid];
	DataOutputStream outToClient[] = JavaServer.outToClient;

	public SThread(int a) {
		srcid = a;
	}

	public void run() {
		while (true) {
			try {

				clientSentence = inFromClient.readLine();
				// clientSentence = inFromClient.readLine();

				//System.out.println("From Client " + srcid + ": " + clientSentence);
				Canvas_Demo.ta.append("From Client " + srcid + ": " + clientSentence + "\n");
				
				for(int i=0; i<JavaServer.i; i++){
                    
                    if(i!=srcid)
                        outToClient[i].writeBytes("Client "+srcid+": "+clientSentence + '\n');	//'\n' is necessary
                }
				
				Canvas_Demo.myjp.revalidate();
				Canvas_Demo.myjp.repaint();

					} catch (Exception e) {
			}

		}
	}
}

class Sentencefromserver extends Thread {

	public static String sendingSentence;

	public Sentencefromserver() {

	}

	public void run() {

		while (true) {

			try {

				if (sendingSentence.length() > 0) {
					for (int i = 0; i < JavaServer.i; i++) {
						JavaServer.outToClient[i].writeBytes("From Server: " + sendingSentence + '\n');

					}
					sendingSentence = null;
				}

			} catch (Exception e) {

			}
		}
	}
}
