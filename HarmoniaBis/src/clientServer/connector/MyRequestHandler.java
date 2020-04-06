package clientServer.connector;

//import java.io.DataInputStream;
//import java.io.DataOutputStream;
import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.Socket;
import java.util.*;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.util.InputStreamSender;
import com.esotericsoftware.kryonet.util.TcpIdleSender;

import requests.*;
import solver.*;



public class MyRequestHandler extends Thread {
	
	
	private volatile boolean stop;

    
	
	Connection con;
	LinkedList<Request> queue;
	LinkedList<Request> queueToFactory;
	ArrayList<ProcessSolver> listSend;
	ArrayList<Connection> connections;
	boolean running;
	long startTime;
	Calendar cal = Calendar.getInstance();
	
	
	// Constructor 
	public MyRequestHandler(ArrayList<Connection> connections, LinkedList<Request> queue, long startTime) { 
		this.connections = connections; 
		//this.oos = oos;
		this.queue = queue;
		this.startTime = startTime;
		running = false;
		queueToFactory = new LinkedList<Request>();
		this.stop = true;
	} 

	@Override
	public void run() { 
		while (stop) {
			if(queue.size() >= 100 || getCurrentTime() > startTime + 100000000){
				running = true;
				System.out.println("It is working   .:.  " + queue.size() + "  " +  (getCurrentTime() - (startTime + 1000)) + " + " + this.getName());
				synchronized (queue) {
					int count = 0;
					for(int r=0; r < 100; r++){
						queueToFactory.add(queue.get(r));
					}
					for(int l=0; l < 100; l++){
						queue.remove(l);
					}
				}
				
				
				startTime = getCurrentTime();
			
				try { 
					System.out.println("---------------------------------------------  " + Thread.currentThread());
					//System.out.println("All requests received: Processing ...");
					//System.out.println(queue.size());
					ArrayList<ProcessSolver> response = Scheduler.getResponse(queue);
					//System.out.println("End process : Sending ... " + response.size() + "   " + Thread.currentThread());
					InputStreamSender streamSend = null;
					for(ProcessSolver process : response){
						this.connections.get(process.getRequest().getAppId()).sendTCP(process);
						//try { Thread.sleep(2); } catch (InterruptedException e) { e.printStackTrace(); }
					}
					//System.out.println("Response scheduler sent... " + Thread.currentThread()); 
					//System.out.println("---------------------------------------------");
	            	break;
	
				} catch (NullPointerException e) { 
					e.printStackTrace(); 
					continue;
				} 
			}
		}
	}  
	

	public boolean getRunning(){
		return this.running;
	}
	

	
	private long getCurrentTime(){
		return cal.getTimeInMillis();
	
	}
	
	public void stopThread() {
        stop = false;
    }

	
	
	
	
} 




