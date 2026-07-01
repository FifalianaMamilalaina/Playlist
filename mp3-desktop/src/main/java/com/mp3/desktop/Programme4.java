package com.mp3.desktop;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Programme 4 :
 * Écoute la queue de suppression (alimentée par Programme 3)
 * et s'occupe de supprimer physiquement les fichiers traités.
 */
public class Programme4 implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Programme4.class);

    @Override
    public void run() {
        logger.info("=== Demarrage du Programme 4 (Gestion de la suppression) ===");

        try {
            Connection connection = RabbitMQConfig.getFactory().newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(RabbitMQConfig.QUEUE_SUPPRESSION, true, false, false, null);
            channel.basicQos(1);

            logger.info("Programme 4 en attente de messages sur la queue {}", RabbitMQConfig.QUEUE_SUPPRESSION);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String cheminFichier = new String(delivery.getBody(), "UTF-8");
                logger.info("<- Demande suppression recu (P4) : {}", cheminFichier);

                try {
                    supprimerFichier(cheminFichier);
                } catch (Exception e) {
                    logger.error("Erreur lors de la suppression de : " + cheminFichier, e);
                } finally {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };

            channel.basicConsume(RabbitMQConfig.QUEUE_SUPPRESSION, false, deliverCallback, consumerTag -> { });

            synchronized (this) {
                while (!Thread.currentThread().isInterrupted()) {
                    wait(1000);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Programme 4 interrompu");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Erreur inattendue dans Programme 4", e);
        }
    }

    private void supprimerFichier(String chemin) {
        File file = new File(chemin);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                logger.info("Fichier supprime physiquement avec succes : {}", chemin);
            } else {
                logger.warn("Impossible de supprimer physiquement le fichier : {}", chemin);
            }
        } else {
            logger.warn("Le fichier n'existe deja plus : {}", chemin);
        }
    }
}
