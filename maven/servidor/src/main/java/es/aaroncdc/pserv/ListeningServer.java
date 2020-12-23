package es.aaroncdc.pserv;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;
import es.aaroncdc.pserv.ConnectionHandler;

public class ListeningServer extends Thread {
	// Dirección a la que escucha el servidor (Puede ser "127.0.0.1", "::0", o lo que corresponda)
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
	
	// Ésta función reemplaza Thread.run(), y será ejecutada en un nuevo hilo.
	public void run() {
		try {
			// Crea un nuevo ServerSocket y lo pone a escuchar en la dirección:puerto asignados.
			ServerSocket ss = new ServerSocket();
			ss.bind(new InetSocketAddress(this.getIP(), this.getPort()));
			
			// El servidor escucha durante 100 ms. Si no hay nuevas conexiones en ese periodo, se lanza
			// una excepción SocketTimeoutException.
			ss.setSoTimeout(100);
			
			// Bucle principal del programa
			while(this.keepAlive)
			{
				// Esperar para ver si hay nuevas conexiones (Durante 100ms)
				try {
					Socket in = ss.accept();
					
					// Si hay nuevas conexiones:
					// Crear una nueva conexión en un hilo paralelo.
					ConnectionHandler conn = new ConnectionHandler(in);
					conn.run();
					
					// Añadir ésta conexión a la lísta de conexiones activas.
					this.connections.add(conn);
					
				} catch(SocketTimeoutException e) {
					// Si no hay nuevas conexiones después de 100ms, se lanza una
					// excepción. En éste caso, se aprovecha para limpiar la lísta
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
			
			// Cerrar la conexión y finalizar el hilo.
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
