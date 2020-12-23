package es.aaroncdc.pserv;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.lang.Thread;

public class ConnectionHandler extends Thread {
	// Socket con la conexión
	private Socket inSocket;
	// Mantener el hilo activo (Establecer como 'false' para finalizar el servidor)
	private boolean keepAlive = true;
	// Flujo de entrada de datos
	private DataInputStream istream;
	// Flujo de salida de datos
	private DataOutputStream ostream;
	// Buffer de entrada
	private byte[] iBuffer;
	// Tamaño de los buffer
	private int bufSz = 8192;
	// Determina si los buffer están en uso
	private boolean bufInUse = false;
	
	// Constructor principal
	public ConnectionHandler(Socket conn)
	{
		try {
			this.inSocket = conn;
			this.istream = new DataInputStream(this.inSocket.getInputStream());
			this.ostream = new DataOutputStream(this.inSocket.getOutputStream());
			this.iBuffer = new byte[this.bufSz];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Ésta función reemplaza Thread.run(), y será ejecutada en un nuevo hilo.
	public void run()
	{
		// Asegurarse de que hay un socket
		if(inSocket == null)
		{
			return;
		}
			// Bucle principal del programa
			while(this.keepAlive == true)
			{
				// Lee bytes del flujo de entrada hasta que ocurra una excepción EOFException o IOException,
				// o se agote el buffer.
				try {
					this.bufInUse = true;
					
					int ses = 0;
					while(this.istream.available() > 0)
					{
						// Ésta función se ejecuta hasta que alguna de las condiciones mencionadas anteriormente
						// se cumplan.
						this.istream.readFully(this.iBuffer, this.bufSz * ses, this.bufSz);
						
						// Si hay más datos, incrementar el tamaño del buffer.
						if(this.istream.available() > 0)
						{
							// Incrementar la sesión
							ses++;
							// Crear un array temporal más grande.
							byte[] newArray = new byte[this.bufSz * (ses+1)];
							// Copiar los elementos actuales al array temporal.
							System.arraycopy(this.iBuffer, 0, newArray, 0, iBuffer.length);
							// Clona el array temporal al buffer de entrada.
							this.iBuffer = newArray.clone();
							// Asegurarse de que el array ya no está en uso.
							newArray = null;
							
							// Siguiente iteración.
							continue;
						}
					}
					this.bufInUse = false;
				}
				catch (EOFException e) {
					// Cuando ya no haya más datos que leer, se lanza una excepción EOFException.
					// A partir de éste momento, se procede a procesar el buffer de entrada.

					// Transforma el contenido en un string con formato UTF-8
					String data = new String(this.iBuffer, Charset.forName("UTF8"));
					
					// Muestra el texto en la salida
					System.out.println(data);
					
					this.bufInUse = false;
					break;
					
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					this.bufInUse = false;
					break;
				}
			}
			
			// Finaliza la conexión
			this.keepAlive = false;
			
			try {
				this.istream.close();
				this.ostream.close();
				this.inSocket.close();
			} catch (IOException ef) {
				ef.printStackTrace();
			}
			
			return;
		}
	
	public boolean kill()
	{
		return this.keepAlive = false;
	}
	
	public boolean isRunning()
	{
		return this.keepAlive;
	}
	
	public int getBufferSize()
	{
		return this.bufSz;
	}
	
	public int setBufferSize(int newSize)
	{
		if(!this.bufInUse)
		{
			return this.bufSz = newSize;
		}else {
			return this.bufSz;
		}
	}
}
