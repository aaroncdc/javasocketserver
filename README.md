# javasocketserver
Ejemplo de servidor TCP en Java asíncrono usando la clase ServerSocket. Funciona en Java 8 y siguientes versiones.

El programa pone un servidor TCP a la escucha en la dirección y puertos asignados al instanciar la
clase ListeningServer. Tanto el servidor, como las nuevas conexiones, se gestionan en hilos separados
del proceso principal. Para ello, las clases que se vayan a ejecutar en hilos separados extienden
la clase Thread.

Cada nueva conexión se gestiona mediante una nueva instancia de la clase ConnectionHandler. Para conseguir
que la función accept() del ServerSocket no bloquee el hilo del servidor, se establece un tiempo de espera
de 100ms para la escucha mediante la función setSoTimeout(int). Al finalizar el tiempo de escucha, se
lanza una excepción SocketTimeoutException en caso de no haber nuevas conexiones. Así se consigue
que la función accept() sólo bloquee durante el tiempo especificado de escucha.