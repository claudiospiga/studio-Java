package studioMedico;
import com.sun.net.httpserver.HttpServer;//creazione server http
import com.sun.net.httpserver.HttpHandler;//gestire le richieste http
import com.sun.net.httpserver.HttpExchange;//gestire scambio di richiesta e risposta su http
import java.io.*;//importa le classi per  l I/O (input e output)
import java.net.InetSocketAddress;//per gestire indirizzo ip e porta del server (software)
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class main {

	public static void main(String[] args) throws IOException {
		//creazione del server HTTP sull aporta 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new home());
        server.createContext("/riepilogoPazienti", new RiepilogoPazientiHandler());
        server.createContext("/aggiuntaPazienti", new AggiuntaPazientiHandler());
        server.createContext("/eliminaPaziente", new EliminaPazienteHandler());

        server.start();
        System.out.println("il server è connesso"); //messaggio di controllo 
        
	}
	
	static class AggiuntaPazientiHandler implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException{
			if(exchange.getRequestMethod().equalsIgnoreCase("GET")) {
				String htmlAggiuntaPazienti ="<html>"+
										     "<head>" +
										     "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css'>" +
										     "<style>"+
										   
							                 "body {background-image: url('https://www.gruppoacademy.com/wp-content/uploads/2022/05/ia-scaled.jpg'); background-size: cover;background-color: rgba(0, 0, 0, 0.2);}" + // Aggiunto sfondo
							                 "#footer {position: fixed; bottom: 0; width: 100%; background-color: #333; color: white; text-align: center; padding: 10px;}" + // Stile footer
							
										     "</style>"+
										     "<body>"+
										     "<nav class='navbar bg-body-tertiary'>"+
						                     "<div class='container-fluid'>" +
						                    "<a class='navbar-brand' href='/home'>" +
						                     "Home"+
						                    
						                   " </a>" +
						                  "</div>" +
						                  " </nav>" +
										     "<div class='container'>"+
										     "<form id='aggiuntaPazientiForm' method='post' action='/aggiuntaPazienti'>"+
										     " <div class='mb-3'>"+
										     " <label for='nome' class='form-label'>Nome:</label>"+
										     " <input type='text' class='form-control' id='nome' name='nome' aria-describedby='nome' required>"+
										     "</div>"+
										     "<div class='mb-3'>"+
										     " <label for='cognome' class='form-label'>Cognome:</label>"+
										     " <input type='text' class='form-control' name='cognome' id='cognome' required>"+
										     "</div>"+
										     "<div class='mb-3'>"+
										     " <label for='telefono' class='form-label'>Telefono:</label>"+
										     " <input type='tel' class='form-control' name='telefono' id='telefono'required>"+
										     " </div>"+
										     " <div class='mb-3'>"+
										     " <label for='indirizzo' class='form-label'>Indirizzo:</label>"+
										     "<input type='text' class='form-control' name='indirizzo' id='indirizzo'required>"+
										     "</div>"+
										     "<div class='mb-3'>"+
										     "<label for='email' class='form-label'>Email:</label>"+
										     "<input type='email' class='form-control' name='email' id='email' required>"+
										     "</div>"+
										     " <div class='mb-3'>"+
										     "<label for='dataNascita' class='form-label'>Data Di Nascita:</label>"+
										     "<input type='date' class='form-control' id='dataNascita' name='dataNascita' required>"+
										     "</div>"+
										     "<div class='mb-3 form-check'>"+
										     " <input type='checkbox' class='form-check-input' id='privacy' required>"+
										     " <label class='form-check-label' for='privacy'>Dichiaro di aver letto ed accettato tutti i consensi sulle Condizioni d'uso e Norme sulla privacy di Studio Medico.</label>"+
										     " </div>"+
										     " <button type='submit' class='btn btn-primary'>Continua</button>"+
										     " <div id='emailHelp' class='form-text'>Non condivideremo le vostre informazioni con nessuno</div>"+
										     " </form>"+
										     " </div>"+
										     "</body>"+
										     "</html>";
				// Imposta l'intestazione della risposta
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                // Imposta lo status code e la lunghezza della risposta
                exchange.sendResponseHeaders(200, htmlAggiuntaPazienti.length());

                // Scrive la risposta al client
                OutputStream os = exchange.getResponseBody();
                os.write(htmlAggiuntaPazienti.getBytes());
                os.close();
                
			 } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
	                // Recupera i dati inviati dal form
	            
	            	//InputStreamerReade prend i dati dal web in formato Posto(non visibili dall'utente) al server
	            	//vengono convertite le parole in codice binario
	                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
	                System.out.println("input");
	                //Libreria che trasforma i dati di input in parole
	                BufferedReader br = new BufferedReader(isr);
	                System.out.println("buffer");
	                //La stringa abbiamo usato il metodo .readLine() per trasformare la stringa in scritta leggibile
	                String formPazienti = br.readLine();
	                //.split('&') prende la parte di testo primaa del segno
	                //se metto .spli('=')[1] mi prendde i vlori dopo il segno
	                String[] formPazientiArray = formPazienti.split("&");
	                String nome = formPazientiArray[0].split("=")[1];
	                String cognome = formPazientiArray[1].split("=")[1];
	                String telefono = formPazientiArray[2].split("=")[1];
	                String indirizzo = formPazientiArray[3].split("=")[1];
	                String email = formPazientiArray[4].split("=")[1];
	                String dataNascita = formPazientiArray[5].split("=")[1];
	              

	                // Connessione al database e inserimento del biglietto
	                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/studio", "root", "marina97!")) {
	                    String query = "INSERT INTO paziente (nome, cognome, telefono, indirizzo, email, dataNascita) VALUES (?, ?, ?, ?, ?, ?)";
	                    try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                        pstmt.setString(1, nome);
	                        pstmt.setString(2, cognome);
	                        pstmt.setString(3, telefono);
	                        pstmt.setString(4, indirizzo);
	                        pstmt.setString(5, email);
	                        pstmt.setString(6, dataNascita);
	                        pstmt.executeUpdate();
	                    }
	                } catch (SQLException e) {
	                    e.printStackTrace();
	                }

	                
	                exchange.getResponseHeaders().set("Location", "/"); 
	                exchange.sendResponseHeaders(302, -1); 
	            } else {
	             
	                exchange.sendResponseHeaders(405, -1);
	            }
	        }
	    }
	
	static class RiepilogoPazientiHandler implements HttpHandler {
	    @Override
	    public void handle(HttpExchange exchange) throws IOException {
	        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
	            // Connessione al database e recupero dei pazienti
	            List<String> pazienti = new ArrayList<>();
	            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/studio", "root", "marina97!")) {
	                String query = "SELECT * FROM paziente";
	                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                    ResultSet rs = pstmt.executeQuery();
	                    while (rs.next()) {
	                        String id = rs.getString("id");
	                        String nome = rs.getString("nome");
	                        String cognome = rs.getString("cognome");
	                        String telefono = rs.getString("telefono");
	                        String indirizzo = rs.getString("indirizzo");
	                        String email = rs.getString("email");
	                        Date data = rs.getDate("DataNascita");
	                        
	                        pazienti.add("<tr><td>" + nome + "</td><td>" + cognome + "</td><td>" + telefono + "</td><td>" + indirizzo + "</td><td>" + email + "</td><td>" + data + "</td><td><form method='post' action='/eliminaPaziente'><input type='hidden' name='id' value='" + id + "'><input type='submit' value='Elimina'></form></td></tr>");
	                    }
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }

	          // HTML
	            StringBuilder response = new StringBuilder();
	            response.append("<html>");
	            response.append("<head>");
	            response.append("<title>Riepilogo pazienti</title>");
	            response.append("<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css'>");
	            response.append("<style>");
	            response.append("body {background-image: url('https://www.gruppoacademy.com/wp-content/uploads/2022/05/ia-scaled.jpg'); background-size: cover;background-color: rgba(0, 0, 0, 0.2);}");
	            response.append("#footer {position: fixed; bottom: 0; width: 100%; background-color: #333; color: white; text-align: center; padding: 10px;}");
	            response.append("</style>");
	            response.append("</head>");
	            response.append("<body>");
	            response.append("<nav class='navbar bg-body-tertiary'>");
	            response.append("<div class='container-fluid'>");
	            response.append("<a class='navbar-brand' href='#'>Home</a>");
	            response.append("</div>");
	            response.append("<div class='container'>");
	            response.append("<h1 class='mt-5'>Riepilogo pazienti</h1>");
	            response.append("<table class='table'><thead class='thead-dark'><tr><th scope='col'>Nome</th><th scope='col'>Cognome</th><th scope='col'>Telefono</th><th scope='col'>Indirizzo</th><th scope='col'>Email</th><th scope='col'>Data di nascita</th><th scope='col'>Azioni</th></tr></thead><tbody>");

	            // Aggiungi le righe della tabella con i pazienti e i pulsanti "Elimina"
	            for (String paziente : pazienti) {
	                response.append(paziente);
	            }

	            response.append("</tbody></table>");
	            response.append("</div>");
	            response.append("</body>");
	            response.append("</html>");

	            // Invia la risposta al client
	            exchange.getResponseHeaders().set("Content-Type", "text/html");
	            exchange.sendResponseHeaders(200, response.length());
	            OutputStream os = exchange.getResponseBody();
	            os.write(response.toString().getBytes());
	            os.close();
	        } else {
	            
	            exchange.sendResponseHeaders(405, -1);
	        }
	    }
	}

	// Gestore per la richiesta POST per eliminare un paziente
	static class EliminaPazienteHandler implements HttpHandler {
	    @Override
	    public void handle(HttpExchange exchange) throws IOException {
	        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
	            // Recupera l'ID del paziente dalla richiesta
	            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8"); //conversione stringa in codice binario
	            BufferedReader br = new BufferedReader(isr); //conversione in codice binario in stringa
	            String formData = br.readLine(); //la stringa che contiene tuti i dati
	            String id = formData.split("=")[1];//split dei dati

	            // Elimina il paziente dal database utilizzando l'ID
	            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/studio", "root", "marina97!")) {
	                String query = "DELETE FROM paziente WHERE id = ?";
	                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
	                    pstmt.setString(1, id);
	                    pstmt.executeUpdate();
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }

	            // Reindirizza l'utente alla pagina di riepilogo pazienti dopo l'eliminazione
	            exchange.getResponseHeaders().set("Location", "/riepilogoPazienti");
	            exchange.sendResponseHeaders(302, -1);
	        } else {
	            // Se la richiesta non è di tipo POST, restituisci errore 405 Method Not Allowed
	            exchange.sendResponseHeaders(405, -1);
	        }
	    }
	}

	 
	 
	 //home
	
	static class home implements HttpHandler{
		@Override
		public void handle(HttpExchange exchange) throws IOException{
			String htmlHome ="<html>" +
					     "<head>"  +
	                    "<meta name='viewport' content='width=device-width, initial-scale=1'>" + // Meta tag per la visualizzazione responsive
	                    "<link rel='stylesheet' href='https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css'>" +
	                    "<title>Home</title>"+
	                    "<style>"+
	                    "body {background-image: url('https://www.gruppoacademy.com/wp-content/uploads/2022/05/ia-scaled.jpg'); background-size: cover;background-color: rgba(0, 0, 0, 0.2);}" + // Aggiunto sfondo
	                    "#footer {position: fixed; bottom: 0; width: 100%; background-color: #333; color: white; text-align: center; padding: 10px;}" + // Stile footer
	                    "</style>" +
	                    "</head>" +
	                    "<body>"+
	                    "<nav class='navbar bg-body-tertiary'>"+
	                     "<div class='container-fluid'>" +
	                    "<a class='navbar-brand' href='#'>" +
	                    "Home" +
	                   " </a>" +
	                  "</div>" +
	                  " </nav>" +
	                  "<br>"  +
	                  "<br>"  +
	                  "<br>"  +
	                  "<br>"  +
	                  "<div class='container text-center mt-5'> "+
	                  "<button type='button' class='btn btn-danger btn-lg mr-2'><a href='/riepilogoPazienti' style='color: black; text-decoration: none;'>Riepilogo pazienti</a></button> "+
	                  "<button type='button' class='btn btn-warning btn-lg'><a href='/aggiuntaPazienti' style='color: black; text-decoration: none;'>Aggiunta pazienti</a></button>"+
	                  "</div>"+
	                  "<br>"  +
	                  "<br>"  +
	                  "<div class='row'>" +
	                  "<div class='col'>" +
	                  "<ul class='list-group'>" +
	                  "<li class='list-group-item active' aria-current='true'>Dottor Gennaro</li>" +
	                  "<li class='list-group-item'>Lunedi 9:18</li>" +
	                  "<li class='list-group-item'>Martedi 9:18</li>" +
	                  "<li class='list-group-item'>Mercoledi off</li>" +
	                  "<li class='list-group-item'>Giovedi 18:20</li>" +
	                  "</ul>" +
	                  "</div>" +
	                  "<div class='col'>" +
	                  "<ul class='list-group'>" +
	                  "<li class='list-group-item active' aria-current='true'>Dottor Mario</li>" +
	                  "<li class='list-group-item'>Lunedi 10:00</li>" +
	                  "<li class='list-group-item'>Martedi 10:00</li>" +
	                  "<li class='list-group-item'>Mercoledi 9:30</li>" +
	                  "<li class='list-group-item'>Giovedi off</li>" +
	                  "</ul>" +
	                  "</div>" +
	                  "</div>"+
	             

	                  "<div id='footer'>"+
	                  "<p class='text-light'>Dott. Mario Rossi | Dott.ssa Anna Bianchi | Via Roma, 123 - Città Imaginaria | Studio Medico Immaginario</p>" +
	                  "</div>"+
	                  "</body>"+
	                  "</html>";			
		    // Imposta l'intestazione della risposta
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            int lunghezza1 =htmlHome.toString().getBytes("UTF-8").length;
			exchange.sendResponseHeaders(200, lunghezza1);
          

            // Scrive la risposta al client
            OutputStream os = exchange.getResponseBody();
            os.write(htmlHome.getBytes());
            os.close();
		                    
		}
		
	}
	
	

}
