package Pruebas;

import java_video_stream.JavaClient;
import uniandes.gload.core.Task;
import uniandes.gload.examples.clientserver.Client;

public class ClientServerTask extends Task{

	@Override
	public void fail() {
		// TODO Auto-generated method stub
		System.out.println(Task.MENSAJE_FAIL);
	}

	@Override
	public void success() {
		// TODO Auto-generated method stub
		System.out.println(Task.OK_MESSAGE);
	}

	@Override
	public void execute() {
		// TODO Auto-generated method stub
		try {
			JavaClient client= new JavaClient();
			client.consume();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail();
		}
	}

}
