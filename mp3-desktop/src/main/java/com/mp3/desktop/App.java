package com.mp3.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("====================================");
        logger.info("Lancement de l'application MP3 Desktop");
        logger.info("====================================");

        // Initialisation de la connexion et des queues sur RabbitMQ
        RabbitMQConfig.initQueues();

        // Lancement du Programme 1 (Scanner le repertoire)
        Thread threadP1 = new Thread(new Programme1(), "Thread-P1");
        threadP1.start();

        // Lancement du Programme 2 (Extracteur metadata)
        Thread threadP2 = new Thread(new Programme2(), "Thread-P2");
        threadP2.start();

        // Lancement du Programme 3 (Envoi API + Anti-doublons)
        Thread threadP3 = new Thread(new Programme3(), "Thread-P3");
        threadP3.start();

        // Lancement du Programme 4 (Suppression fichiers)
        Thread threadP4 = new Thread(new Programme4(), "Thread-P4");
        threadP4.start();

        logger.info("Les 4 programmes tournent en parallele.");
        
        // Ajouter un hook pour stopper proprement
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Arret de l'application...");
            threadP1.interrupt();
            threadP2.interrupt();
            threadP3.interrupt();
            threadP4.interrupt();
        }));
    }
}
