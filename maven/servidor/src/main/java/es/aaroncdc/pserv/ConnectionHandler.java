package es.aaroncdc.pserv;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.lang.Thread;

public class ConnectionHandler extends Thread {
	// Socket con la conexi�n
	private Socket inSocket;
	// Mantener el hilo activo (Establecer como 'false' para finalizar el servidor)
	private boolean keepAlive = true;
	// Flujo de entrada de datos
	private DataInputStream istream;
	// Flujo de salida de datos
	private DataOutputStream ostream;
	// Buffer de entrada
	private byte[] iBuffer;
	// Tama�o de los buffer
	private int bufSz = 8192;
	// Determina si los buffer est�n en uso
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
	
	// �sta funci�n reemplaza Thread.run(), y ser� ejecutada en un nuevo hilo.
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
				// Lee bytes del flujo de entrada hasta que ocurra una excepci�n EOFException o IOException,
				// o se agote el buffer.
				try {
					this.bufInUse = true;
					
					int ses = 0;
					while(this.istream.available() > 0)
					{
						// �sta funci�n se ejecuta hasta que alguna de las condiciones mencionadas anteriormente
						// se cumplan.
						this.istream.readFully(this.iBuffer, this.bufSz * ses, this.bufSz);
						
						// Si hay m�s datos, incrementar el tama�o del buffer.
						if(this.istream.available() > 0)
						{
							// Incrementar la sesi�n
							ses++;
							// Crear un array temporal m�s grande.
							byte[] newArray = new byte[this.bufSz * (ses+1)];
							// Copiar los elementos actuales al array temporal.
							System.arraycopy(this.iBuffer, 0, newArray, 0, iBuffer.length);
							// Clona el array temporal al buffer de entrada.
							this.iBuffer = newArray.clone();
							// Asegurarse de que el array ya no est� en uso.
							newArray = null;
							
							// Siguiente iteraci�n.
							continue;
						}
					}
					this.bufInUse = false;
				}
				catch (EOFException e) {
					// Cuando ya no haya m�s datos que leer, se lanza una excepci�n EOFException.
					// A partir de �ste momento, se procede a procesar el buffer de entrada.

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
			
			// Finaliza la conexi�n
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
