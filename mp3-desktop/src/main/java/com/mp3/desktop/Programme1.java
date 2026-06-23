package com.mp3.desktop;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Programme 1 :
 * A chaque 5 minutes, prend la liste des mp3 dans le repertoire.
 * Envoie la liste (ou chaque chemin vers RabbitMQ).
 */
public class Programme1 implements Runnable {

    // Logger specifique qui ecrira dans logs/programme1.log (via logback.xml)
    private static final Logger logger = LoggerFactory.getLogger(Programme1.class);
    
    private static final String REPERTOIRE_MP3 = "chanson";
    // Delai de 5 minutes : 5 * 60 * 1000 = 300000 ms.
    // Pour les tests, on met 10 secondes (10000 ms). A CHANGER APRES TEST.
    private static final long DELAI_SCAN_MS = 10000; 

    private final Gson gson = new Gson();

    @Override
    public void run() {
        logger.info("=== Demarrage du Programme 1 ===");
        
        // S'assurer que le repertoire existe
        File r = new File(REPERTOIRE_MP3);
        if (!r.exists()) {
            boolean created = r.mkdirs();
            logger.info("Repertoire {} cree : {}", REPERTOIRE_MP3, created);
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {
                scannerRepertoire();
                
                logger.info("Attente de {} ms avant le prochain scan...", DELAI_SCAN_MS);
                Thread.sleep(DELAI_SCAN_MS);
            } catch (InterruptedException e) {
                logger.warn("Programme 1 interrompu");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("Erreur inattendue dans Programme 1", e);
            }
        }
    }

    private void scannerRepertoire() {
        Path path = Paths.get(REPERTOIRE_MP3);
        logger.info("Scan du repertoire : {}", path.toAbsolutePath());

        try (Connection connection = RabbitMQConfig.getFactory().newConnection();
             Channel channel = connection.createChannel();
             Stream<Path> paths = Files.list(path)) {

            // Filtrer uniquement les fichiers .mp3
            paths.filter(p -> p.toString().toLowerCase().endsWith(".mp3"))
                 .filter(Files::isRegularFile)
                 .forEach(p -> envoyerMessage(channel, p.toAbsolutePath().toString()));

        } catch (Exception e) {
            logger.error("Erreur lors du scan du repertoire ou connexion RabbitMQ", e);
        }
    }

    private void envoyerMessage(Channel channel, String cheminComplet) {
        try {
            // Envoyer le chemin as String ou objet JSON
            MessageMp3 msg = new MessageMp3(cheminComplet);
            String json = gson.toJson(msg);

            // Publish message
            channel.basicPublish(
                    "", 
                    RabbitMQConfig.QUEUE_LISTE_MP3, 
                    MessageProperties.PERSISTENT_TEXT_PLAIN, 
                    json.getBytes("UTF-8")
            );
            
            logger.info("-> Message envoye a RabbitMQ [queue_liste_mp3] : {}", cheminComplet);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi du message pour " + cheminComplet, e);
        }
    }

    /**
     * Classe interne simple pour structurer le message JSON
     */
    public static class MessageMp3 {
        public String cheminFichier;
        public MessageMp3(String c) { this.cheminFichier = c; }
    }
}
