package clientServer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.FrameworkMessage;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.kryonet.util.InputStreamSender;
import com.esotericsoftware.minlog.Log;

import clientServer.connector.MyRequestHandler;
import requests.RR;
import requests.Request;
import solver.ProcessSolver;

public class Connector2 extends Listener {
	
	
	static Server server;
	static int tcpPort = 33278;
	
	int id = 0;
	static long startTime;
	static long currentTime;
	static Calendar cal;
	LinkedList<Request> queueToFactory;
	MyRequestHandler threadQueue;
	
	public static LinkedList<Request> queue;
	ExecutorService pool = Executors.newFixedThreadPool(4);
	ArrayList<Connection> connections = new ArrayList<Connection>();
	ArrayList<MyRequestHandler> listThreads = new ArrayList<MyRequestHandler>();
	
	
	public static void main(String[] args) throws Exception {
		
		queue = new LinkedList<Request>() ;
		cal =  Calendar.getInstance();
		
		System.out.println("Creating the server");
		server = new Server(10000000,9000000);
		
		server.getKryo().register(RR.class);
		server.getKryo().register(Request.class);
		server.getKryo().register(ArrayList.class);
		server.getKryo().register(ProcessSolver.class);
		Log.ERROR();
		Log.WARN();
		Log.INFO();
		Log.DEBUG();
		Log.TRACE();
		
		server.bind(tcpPort);
		
		server.start();
		
		
		
		server.addListener(new Connector2());
		startTime = cal.getTimeInMillis();
		
		System.out.println("Server started");
		
	}
	
	
	
	

	public void connected(Connection c) {
		System.out.println("___________Connection received from " + c.getRemoteAddressTCP().getHostString() + "____________________");
		this.connections.add(c);
		System.out.println("____________List of connections : " + server.getConnections().length + "____________________");
		startTime = cal.getTimeInMillis();
		listThreads.add(new MyRequestHandler(connections, queue, startTime));
		pool.execute(listThreads.get(listThreads.size()-1));
		
	}
	
	
	public void received(Connection c, Object o) {
		try{
			
			if(o instanceof Request) {
				int appID = 10000;
				for(int con=0; con < this.connections.size(); con++){
					if(c.getID() == this.connections.get(con).getID()){
						appID = con;
					}
				}
				Request request = (Request) o;
				request.setAppId(appID);
				synchronized (queue) {
					
					queue.add(request);
					
					for(int index=0; index < listThreads.size(); index++){
						if(!listThreads.get(index).getRunning()){
							listThreads.get(index).stopThread();
							listThreads.remove(index);
							listThreads.add(new MyRequestHandler(connections, queue, startTime));
							pool.execute(listThreads.get(listThreads.size()-1));
							break;
						} else if(index == listThreads.size()-1){
							listThreads.add(new MyRequestHandler(connections, queue, startTime));
							pool.execute(listThreads.get(listThreads.size()-1));
						}
					}
				}
				
				
				
			} else if(o instanceof FrameworkMessage){
				System.out.println("FrameworkMessage : " + o.toString());
			} else if(o instanceof String){
				System.out.println(o);
			} else {
				System.out.println("not request type : " + o.getClass());
			}
			id++;
		} catch (NullPointerException e){
			e.printStackTrace();
		}
	}
	
	public void disconnected(Connection c) {
		System.out.println("Disconnected");
	}
	
	

	
	private long getCurrentTime(){
		return cal.getTimeInMillis();
	
	}
	
	
	
	
	
	
	
}










