package com.mp3.desktop;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQConfig {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    // Noms des files d'attente (queues)
    public static final String QUEUE_LISTE_MP3 = "queue_liste_mp3";
    public static final String QUEUE_METADATA = "queue_metadata";

    private static ConnectionFactory factory;

    public static ConnectionFactory getFactory() {
        if (factory == null) {
            factory = new ConnectionFactory();
            factory.setHost("localhost");
            // Factory defaults: port 5672, user guest, pass guest
        }
        return factory;
    }

    /**
     * Initialise les queues au demarrage pour s'assurer qu'elles existent
     */
    public static void initQueues() {
        try (Connection connection = getFactory().newConnection();
             Channel channel = connection.createChannel()) {

            // true = durable (survit a un redemarrage de RabbitMQ)
            channel.queueDeclare(QUEUE_LISTE_MP3, true, false, false, null);
            channel.queueDeclare(QUEUE_METADATA, true, false, false, null);

            logger.info("Queues RabbitMQ initiees avec succes : {}, {}", QUEUE_LISTE_MP3, QUEUE_METADATA);
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation des queues RabbitMQ", e);
        }
    }
}
