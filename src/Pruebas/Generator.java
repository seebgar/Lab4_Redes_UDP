package Pruebas;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import java_video_stream.JavaClient;
import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;


public class Generator {
	private LoadGenerator loadG;
	private Task work;
	public static int numberOfTasks;
	public static  int gapBetweenGap;
	private PrintWriter writer;
	public static int nThreads;
	long[] tiempoRespuestaConsulta = new long[numberOfTasks]; //10 =Numero de itraciones
	long[] transaccionesFallidas = new long[numberOfTasks];
	long[] tiempoVerificacion = new long[numberOfTasks];
	public Generator(){
		nThreads=60;
		work=crearTask();
		numberOfTasks=480; //400,20,80
		gapBetweenGap=20;//20,40,100
        try {
			writer = new PrintWriter("./docs/datosTransaccionesPerdidas"+nThreads +"-" + numberOfTasks + "-" + gapBetweenGap+ "prueba");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		loadG=new LoadGenerator("Client-server Load Test",numberOfTasks,work,gapBetweenGap);
		loadG.generate();
		writer.println("Numero de transacciones fallidas: ");
		writer.close();
	}

	private Task crearTask() {
		return new ClientServerTask();
	}

	public static void main(String[] args){
		

			@SuppressWarnings("unused")
			Generator gen=new Generator();
		
	}
}
