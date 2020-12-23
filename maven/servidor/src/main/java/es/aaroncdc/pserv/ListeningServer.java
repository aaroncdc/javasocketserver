package es.aaroncdc.pserv;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;
import es.aaroncdc.pserv.ConnectionHandler;

public class ListeningServer extends Thread {
	// Direcci�n a la que escucha el servidor (Puede ser "127.0.0.1", "::0", o lo que corresponda)
	private String bindIP;
	// Puerto a escuchar (Por ejemplo, 8150 o lo que proceda)
	private int bindPort;
	// Mantener el hilo activo (Establecer como 'false' para finalizar el servidor)
	private boolean keepAlive = true;
	// Lista de conexiones activas
	private List<ConnectionHandler> connections;
	
	// Constructor principal
	public ListeningServer(String bIP, int bPORT)
	{
		this.setIP(bIP);
		this.setPort(bPORT);
		this.connections = new ArrayList<ConnectionHandler>();
	}
	
	// �sta funci�n reemplaza Thread.run(), y ser� ejecutada en un nuevo hilo.
	public void run() {
		try {
			// Crea un nuevo ServerSocket y lo pone a escuchar en la direcci�n:puerto asignados.
			ServerSocket ss = new ServerSocket();
			ss.bind(new InetSocketAddress(this.getIP(), this.getPort()));
			
			// El servidor escucha durante 100 ms. Si no hay nuevas conexiones en ese periodo, se lanza
			// una excepci�n SocketTimeoutException.
			ss.setSoTimeout(100);
			
			// Bucle principal del programa
			while(this.keepAlive)
			{
				// Esperar para ver si hay nuevas conexiones (Durante 100ms)
				try {
					Socket in = ss.accept();
					
					// Si hay nuevas conexiones:
					// Crear una nueva conexi�n en un hilo paralelo.
					ConnectionHandler conn = new ConnectionHandler(in);
					conn.run();
					
					// A�adir �sta conexi�n a la l�sta de conexiones activas.
					this.connections.add(conn);
					
				} catch(SocketTimeoutException e) {
					// Si no hay nuevas conexiones despu�s de 100ms, se lanza una
					// excepci�n. En �ste caso, se aprovecha para limpiar la l�sta
					// de conexiones activas.
					
					ArrayList<ConnectionHandler> toRemove = new ArrayList<ConnectionHandler>();
					for(ConnectionHandler conn: this.connections)
					{
						if(conn == null)
						{
							toRemove.add(conn);
							continue;
						}
						if(conn.isRunning() == false || conn.isAlive() == false)
						{
							toRemove.add(conn);
						}
					}
					
					this.connections.removeAll(toRemove);
				}
			}
			
			// Finalizar todas las conexiones activas.
			ArrayList<ConnectionHandler> toRemove = new ArrayList<ConnectionHandler>();
			for(ConnectionHandler conn: this.connections)
			{
				if(conn == null)
				{
					toRemove.add(conn);
					continue;
				}
				if(conn.isRunning())
				{
					conn.kill();
					toRemove.add(conn);
				}
			}
			this.connections.removeAll(toRemove);
			
			// Cerrar la conexi�n y finalizar el hilo.
			ss.close();
			
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean kill()
	{
		return this.keepAlive = false;
	}
	
	public boolean isRunning()
	{
		return this.keepAlive;
	}

	public String getIP() {
		return bindIP;
	}

	public void setIP(String bindIP) {
		this.bindIP = bindIP;
	}

	public int getPort() {
		return bindPort;
	}

	public void setPort(int bindPort) {
		this.bindPort = bindPort;
	}
}
