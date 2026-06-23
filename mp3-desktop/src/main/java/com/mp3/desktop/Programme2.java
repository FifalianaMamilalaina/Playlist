package com.mp3.desktop;

import com.google.gson.Gson;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Programme 2 :
 * Il prend la liste (ou messages individuels de chemins) via RabbitMQ (Programme 1) 
 * et fait une extraction des metadonnees.
 * Envoie la sortie (json avec metadonnees) vers Programme 3 via RabbitMQ.
 */
public class Programme2 implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Programme2.class);
    private final Gson gson = new Gson();

    @Override
    public void run() {
        logger.info("=== Demarrage du Programme 2 ===");

        try {
            Connection connection = RabbitMQConfig.getFactory().newConnection();
            Channel channel = connection.createChannel();

            // S'assurer que les queues existent
            channel.queueDeclare(RabbitMQConfig.QUEUE_LISTE_MP3, true, false, false, null);
            channel.queueDeclare(RabbitMQConfig.QUEUE_METADATA, true, false, false, null);

            // Fetch a maximum of 1 message at a time
            channel.basicQos(1);

            logger.info("Programme 2 en attente de messages sur la queue {}", RabbitMQConfig.QUEUE_LISTE_MP3);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                try {
                    Programme1.MessageMp3 msgIn = gson.fromJson(message, Programme1.MessageMp3.class);
                    logger.info("<- Message recu (P2) : {}", msgIn.cheminFichier);

                    // Traitement : extraction metadonnees
                    Mp3Metadata metadata = extraireMetadata(msgIn.cheminFichier);

                    if (metadata != null) {
                        // Envoi au programme 3
                        String jsonOut = gson.toJson(metadata);
                        channel.basicPublish(
                                "", 
                                RabbitMQConfig.QUEUE_METADATA, 
                                MessageProperties.PERSISTENT_TEXT_PLAIN, 
                                jsonOut.getBytes("UTF-8")
                        );
                        logger.info("-> Metadata envoye (P2 -> P3) : {} - {}", metadata.artiste, metadata.titre);
                    } else {
                        logger.warn("Impossible d'extraire les metadonnees pour {}", msgIn.cheminFichier);
                    }

                } catch (Exception e) {
                    logger.error("Erreur lors du traitement du message dans P2", e);
                } finally {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };

            // Ack manuel apres traitement
            channel.basicConsume(RabbitMQConfig.QUEUE_LISTE_MP3, false, deliverCallback, consumerTag -> { });

            // Bloquer le thread pour qu'il ne se termine pas
            synchronized (this) {
                while (!Thread.currentThread().isInterrupted()) {
                    wait(1000);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Programme 2 interrompu");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Erreur inattendue dans Programme 2", e);
        }
    }

    private Mp3Metadata extraireMetadata(String cheminFichier) {
        File file = new File(cheminFichier);
        if (!file.exists()) {
            logger.error("Fichier introuvable : {}", cheminFichier);
            return null;
        }

        Mp3Metadata data = new Mp3Metadata();
        data.cheminFichier = cheminFichier;
        
        try {
            // Utilisation de mp3agic pour extraire les metadonnees
            Mp3File mp3file = new Mp3File(cheminFichier);
            data.dureeSecondes = (int) mp3file.getLengthInSeconds();
            
            if (mp3file.hasId3v2Tag()) {
                ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                data.titre = id3v2Tag.getTitle() != null ? id3v2Tag.getTitle() : file.getName().replace(".mp3", "");
                data.artiste = id3v2Tag.getArtist();
                data.album = id3v2Tag.getAlbum();
                data.genre = id3v2Tag.getGenreDescription();
                
                try {
                    if (id3v2Tag.getYear() != null && !id3v2Tag.getYear().trim().isEmpty()) {
                        data.annee = Integer.parseInt(id3v2Tag.getYear().trim().substring(0, 4));
                    }
                } catch (Exception e) { /* ignorant les erreurs de parsing d'annee */ }
                
            } else if (mp3file.hasId3v1Tag()) {
                ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                data.titre = id3v1Tag.getTitle() != null ? id3v1Tag.getTitle() : file.getName().replace(".mp3", "");
                data.artiste = id3v1Tag.getArtist();
                data.album = id3v1Tag.getAlbum();
                data.genre = id3v1Tag.getGenreDescription();
                
                try {
                    if (id3v1Tag.getYear() != null && !id3v1Tag.getYear().trim().isEmpty()) {
                        data.annee = Integer.parseInt(id3v1Tag.getYear().trim().substring(0, 4));
                    }
                } catch (Exception e) { /* ignorant les erreurs */ }
            } else {
                // Pas de tags, on utilise le nom du fichier
                data.titre = file.getName().replace(".mp3", "");
            }

            // Calcul du hash md5 pour l'anti-doublon et verification API
            data.hashFichier = calculerHashMD5(file);

            return data;
        } catch (Exception e) {
            logger.error("Erreur mp3agic pour {}", cheminFichier, e);
            return null;
        }
    }

    private String calculerHashMD5(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;
            
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.error("Erreur lors du calcul du hash MD5", e);
            // Retourner quelque chose d'unique (via chemin+taille) comme fallback
            return String.valueOf((file.getAbsolutePath() + file.length()).hashCode());
        }
    }

    public static class Mp3Metadata {
        public String titre;
        public String artiste;
        public String album;
        public String genre;
        public String langue; // Peut ne pas etre dispo dans le tag
        public Integer dureeSecondes;
        public Integer annee;
        public String cheminFichier;
        public String hashFichier;
    }
}
