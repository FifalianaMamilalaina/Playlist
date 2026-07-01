package com.mp3.desktop;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Programme 3 :
 * Parametre sortie du 2eme programme (metadonnees via RabbitMQ).
 * Verifie si doublon via API (GET).
 * Si inexistant, envoie le fichier MP3 + metadonnees via multipart.
 * Supprime le fichier d'origine. Supporte 1 retry.
 */
public class Programme3 implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Programme3.class);
    private final Gson gson = new Gson();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final String API_BASE_URL = "http://localhost:8080/api/chansons";
    private static final String BLACKLIST_FILE = "blacklist.txt";
    private static final String LIMITE_DUREE_FILE = "limite_duree.txt";

    // Listes noires chargees en memoire
    private Set<String> blacklistGenres = new HashSet<>();
    private Set<String> blacklistArtistes = new HashSet<>();
    private Integer limiteDureeSecondes = null;

    @Override
    public void run() {
        logger.info("=== Demarrage du Programme 3 ===");

        // Charger la blacklist et la limite au demarrage
        chargerBlacklist();
        chargerLimiteDuree();

        try {
            Connection connection = RabbitMQConfig.getFactory().newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(RabbitMQConfig.QUEUE_METADATA, true, false, false, null);
            channel.basicQos(1);

            logger.info("Programme 3 en attente de messages sur la queue {}", RabbitMQConfig.QUEUE_METADATA);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                Programme2.Mp3Metadata metadata = null;
                boolean success = false;
                int retryCount = 0;
                
                try {
                    metadata = gson.fromJson(message, Programme2.Mp3Metadata.class);
                    logger.info("<- Metadata recu (P3) : {}", metadata.titre);

                    while (!success && retryCount <= 1) {
                        try {
                            success = traiterMetadata(metadata, channel);
                        } catch (Exception ex) {
                            retryCount++;
                            logger.error("Erreur API - Tentative {}/2 pour {}", retryCount, metadata.titre, ex);
                            if (retryCount <= 1) {
                                Thread.sleep(2000);
                            }
                        }
                    }
                    
                    if (!success) {
                        logger.error("ECHEC definitif pour l'envoi de : {}. Le fichier NE SERA PAS supprime.", metadata.titre);
                    }

                } catch (Exception e) {
                    logger.error("Erreur inattendue dans P3 pour le message: " + message, e);
                } finally {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };

            channel.basicConsume(RabbitMQConfig.QUEUE_METADATA, false, deliverCallback, consumerTag -> { });

            synchronized (this) {
                while (!Thread.currentThread().isInterrupted()) {
                    wait(1000);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Programme 3 interrompu");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Erreur inattendue dans Programme 3", e);
        }
    }

    private boolean traiterMetadata(Programme2.Mp3Metadata metadata, Channel channel) throws Exception {
        // 0.a Verifier la blacklist AVANT tout envoi
        if (estBlackliste(metadata)) {
            logger.info("BLACKLIST - Chanson bloquee : '{}' (artiste='{}', genre='{}')."
                    + " Demande de suppression.",
                    metadata.titre, metadata.artiste, metadata.genre);
            demanderSuppression(metadata.cheminFichier, channel);
            return true;
        }

        // 0.b Verifier la limite de duree
        if (limiteDureeSecondes != null && metadata.dureeSecondes != null) {
            if (metadata.dureeSecondes > limiteDureeSecondes) {
                logger.info("LIMITE DUREE - Chanson bloquee : '{}' ({} s > {} s)."
                        + " Le fichier n'est pas envoye ni supprime.",
                        metadata.titre, metadata.dureeSecondes, limiteDureeSecondes);
                // On retourne true pour accuser reception dans la queue
                return true; 
            }
        }

        // 1. Verifier si la chanson existe deja
        boolean existe = verifierSiExiste(metadata.hashFichier);
        
        if (existe) {
            logger.info("DOUBLON detecte pour (hash={}): {}. On ne l'envoie pas.", metadata.hashFichier, metadata.titre);
            demanderSuppression(metadata.cheminFichier, channel);
            return true; 
        }

        // 2. Envoyer la chanson (POST multipart avec le fichier MP3)
        logger.info("Nouvelle chanson detectee. Upload du fichier + metadonnees vers l'API : {}", metadata.titre);
        boolean apiSuccess = uploadVersApi(metadata);

        if (apiSuccess) {
            // 3. Demander la suppression du fichier au Programme 4
            logger.info("Succes de l'API. Demande de suppression pour : {}", metadata.cheminFichier);
            demanderSuppression(metadata.cheminFichier, channel);
            return true;
        }
        
        return false;
    }

    private boolean verifierSiExiste(String hash) throws Exception {
        if (hash == null || hash.isEmpty()) return false;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/existe?hash=" + hash))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            Map result = gson.fromJson(response.body(), Map.class);
            return (Boolean) result.getOrDefault("existe", false);
        } else {
            throw new RuntimeException("L'API a repondu avec un code non 200 lors de la verification : " + response.statusCode());
        }
    }

    /**
     * Envoie le fichier MP3 + metadonnees en multipart/form-data
     */
    private boolean uploadVersApi(Programme2.Mp3Metadata metadata) throws Exception {
        File fichierMp3 = new File(metadata.cheminFichier);
        if (!fichierMp3.exists()) {
            logger.error("Fichier MP3 introuvable pour upload : {}", metadata.cheminFichier);
            return false;
        }

        String boundary = UUID.randomUUID().toString();

        // Construire le body multipart manuellement
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Champ: fichier (le binaire MP3)
        writeMultipartField(baos, boundary, "fichier", fichierMp3.getName(), 
                Files.readAllBytes(Path.of(metadata.cheminFichier)), "audio/mpeg");

        // Champs texte
        writeMultipartText(baos, boundary, "titre", metadata.titre != null ? metadata.titre : "");
        writeMultipartText(baos, boundary, "artiste", metadata.artiste != null ? metadata.artiste : "");
        writeMultipartText(baos, boundary, "album", metadata.album != null ? metadata.album : "");
        writeMultipartText(baos, boundary, "genre", metadata.genre != null ? metadata.genre : "");
        writeMultipartText(baos, boundary, "langue", metadata.langue != null ? metadata.langue : "");
        if (metadata.dureeSecondes != null) {
            writeMultipartText(baos, boundary, "dureeSecondes", String.valueOf(metadata.dureeSecondes));
        }
        if (metadata.annee != null) {
            writeMultipartText(baos, boundary, "annee", String.valueOf(metadata.annee));
        }
        writeMultipartText(baos, boundary, "hashFichier", metadata.hashFichier != null ? metadata.hashFichier : "");

        // Fin du multipart
        baos.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE_URL + "/upload"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            logger.info("Chanson '{}' uploadee avec succes", metadata.titre);
            return true;
        } else if (response.statusCode() == 409) {
            logger.info("L'API a signale un conflit (doublon) pour '{}'", metadata.titre);
            return true;
        } else {
            throw new RuntimeException("L'API a retourne un code erreur lors de l'upload : " 
                    + response.statusCode() + ". Reponse : " + response.body());
        }
    }

    private void writeMultipartText(ByteArrayOutputStream baos, String boundary, String name, String value) throws IOException {
        String part = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                value + "\r\n";
        baos.write(part.getBytes(StandardCharsets.UTF_8));
    }

    private void writeMultipartField(ByteArrayOutputStream baos, String boundary, String name, String filename, byte[] content, String contentType) throws IOException {
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n" +
                "Content-Type: " + contentType + "\r\n\r\n";
        baos.write(header.getBytes(StandardCharsets.UTF_8));
        baos.write(content);
        baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private void demanderSuppression(String chemin, Channel channel) throws Exception {
        channel.basicPublish("", RabbitMQConfig.QUEUE_SUPPRESSION, null, chemin.getBytes(StandardCharsets.UTF_8));
        logger.debug("Message de suppression envoye dans {} pour le fichier: {}", RabbitMQConfig.QUEUE_SUPPRESSION, chemin);
    }

    /**
     * Charge le fichier blacklist.txt et remplit les sets de genres/artistes bloques.
     * Le fichier est relu a chaque demarrage du programme.
     */
    private void chargerBlacklist() {
        Path blacklistPath = Path.of(BLACKLIST_FILE);
        if (!Files.exists(blacklistPath)) {
            logger.info("Aucun fichier blacklist.txt trouve. Aucun filtrage ne sera applique.");
            return;
        }

        try {
            List<String> lignes = Files.readAllLines(blacklistPath, StandardCharsets.UTF_8);
            String sectionCourante = null;

            for (String ligne : lignes) {
                String trimmed = ligne.trim();

                // Ignorer les lignes vides et les commentaires
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                // Detecter les sections
                if (trimmed.equalsIgnoreCase("[GENRES]")) {
                    sectionCourante = "GENRES";
                    continue;
                } else if (trimmed.equalsIgnoreCase("[ARTISTES]")) {
                    sectionCourante = "ARTISTES";
                    continue;
                }

                // Ajouter l'entree dans la bonne liste (en minuscules pour insensibilite a la casse)
                if ("GENRES".equals(sectionCourante)) {
                    blacklistGenres.add(trimmed.toLowerCase());
                } else if ("ARTISTES".equals(sectionCourante)) {
                    blacklistArtistes.add(trimmed.toLowerCase());
                }
            }

            logger.info("Blacklist chargee : {} genre(s) bloques, {} artiste(s) bloques.",
                    blacklistGenres.size(), blacklistArtistes.size());
            if (!blacklistGenres.isEmpty()) {
                logger.info("  Genres bloques : {}", blacklistGenres);
            }
            if (!blacklistArtistes.isEmpty()) {
                logger.info("  Artistes bloques : {}", blacklistArtistes);
            }

        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du fichier blacklist.txt", e);
        }
    }

    /**
     * Verifie si une chanson correspond a la blacklist (genre ou artiste).
     * La comparaison est insensible a la casse.
     */
    private boolean estBlackliste(Programme2.Mp3Metadata metadata) {
        // Verifier le genre
        if (metadata.genre != null && !metadata.genre.isEmpty()) {
            if (blacklistGenres.contains(metadata.genre.toLowerCase())) {
                return true;
            }
        }

        // Verifier l'artiste
        if (metadata.artiste != null && !metadata.artiste.isEmpty()) {
            if (blacklistArtistes.contains(metadata.artiste.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Charge la limite de duree depuis limite_duree.txt
     */
    private void chargerLimiteDuree() {
        Path path = Path.of(LIMITE_DUREE_FILE);
        if (!Files.exists(path)) {
            logger.info("Aucun fichier limite_duree.txt trouve. Aucune limite de duree ne sera appliquee.");
            return;
        }

        try {
            List<String> lignes = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String ligne : lignes) {
                String trimmed = ligne.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    try {
                        limiteDureeSecondes = Integer.parseInt(trimmed);
                        logger.info("Limite de duree chargee : {} secondes ({} min)", 
                                limiteDureeSecondes, limiteDureeSecondes / 60);
                        return;
                    } catch (NumberFormatException e) {
                        logger.warn("Format de limite de duree invalide : {}", trimmed);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Erreur lors de la lecture du fichier limite_duree.txt", e);
        }
    }
}
