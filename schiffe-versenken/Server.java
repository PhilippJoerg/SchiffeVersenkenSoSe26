import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class Server {
    // Server-Seite eines sehr einfachen Chat-Programms mit Sockets.
    // (Anstelle von "throws IOException" sollte man Ausnahmen besser
    // gezielt mit try-catch auffangen.)
    public static void main (String [] args) throws IOException {
	// Verwendete Portnummer.
	final int port = 50000;

	// Server-Socket erzeugen und an diesen Port binden.
	ServerSocket ss = new ServerSocket(port);

	// Auf eine Client-Verbindung warten und diese akzeptieren.
	// Als Resultat erhält man ein "normales" Socket.
	System.out.println("Waiting for client connection ...");
	Socket s = ss.accept();
	System.out.println("Connection established.");

	// Ein- und Ausgabestrom des Sockets ermitteln
	// und als BufferedReader bzw. Writer verpacken
	// (damit man zeilen- bzw. zeichenweise statt byteweise arbeiten kann).
	BufferedReader in =
		new BufferedReader(new InputStreamReader(s.getInputStream()));
	Writer out = new OutputStreamWriter(s.getOutputStream());

	// Standardeingabestrom ebenfalls als BufferedReader verpacken.
	BufferedReader usr = 
			new BufferedReader(new InputStreamReader(System.in));

    //Spiel beginn
    /*Idee (Münz wurf (festgelegt)): 
    1. Peron A wirft eine Wünze
    2. "Münze dreht sich in der Luft" (zufall wird generiert)
    3. Münze fällt auf...
        - Kopf, dann beginnt Person A
        - Zahl, dann bgeinnt Person B
    */
    
    //zufalls "zahl" wirs generiert
    Random random = new Random();
    
    //Zufalls - boolean wird in result gespeichert
    boolean result = random.nextBoolean();
    
    //Beginner wird entschieden: wenn true -> Client sonst Server
    if (result == true){
        System.out.println("Client is starting");

        //Nachricht wird dem Client übermittelt
        out.write(String.format("You can start\n"));
	    out.flush();

    } else {
        System.out.println("Server is starting");

        //Nachricht wird dem Client übermittelt
        out.write(String.format("I am starting\n"));
	    out.flush();
    }

	// Abwechselnd vom Socket lesen und auf den Bildschirm schreiben
	// bzw. vom Benutzer lesen und ins Socket schreiben.
	// Abbruch bei EOF vom Socket bzw. bei EOF oder Leerzeile vom Benutzer.
	while (true) {
	    String line = in.readLine();
	    if (line == null) break;
	    System.out.println("<<< " + line);

	    System.out.print(">>> ");
	    line = usr.readLine();
	    if (line == null || line.equals("")) break;
	    out.write(String.format("%s%n", line));
	    out.flush();
	    // flush sorgt dafür, dass der Writer garantiert alle Zeichen
	    // in den unterliegenden Ausgabestrom schreibt.
	}

	// EOF ins Socket "schreiben".
	s.shutdownOutput();
	System.out.println("Connection closed.");
    }
}
