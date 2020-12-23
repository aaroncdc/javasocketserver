package es.aaroncdc.pserv;

import es.aaroncdc.pserv.ListeningServer;

public class Main {
	
	private static ListeningServer ls;
	
	public static void main(String[] args) {
		try {
			// Crear un nuevo Servidor de escucha (ListeningServer)
			ls = new ListeningServer("127.0.0.1", 8150);
			
			// Crear un nuevo Shutdown Hook
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					System.out.println("Server halt!");
					ls.kill();
				}
			});
			
			// Iniciar el servidor
			ls.run();
			
			// Unir éste proceso con el del servidor.
			// Si el proceso principal debe realizar alguna tarea en paralelo,
			// entonces hay que omitir ésta línea.
			ls.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
