# Projet Gestion MP3 avec RabbitMQ et Spring Boot

Un système complet composé de deux applications (Desktop et Web) permettant d'automatiser l'extraction des métadonnées de musiques `.mp3`, leur sauvegarde dans une base de données, et la génération de listes de lecture intelligentes via une interface graphique web.

## 📋 Architecture Globale
Le projet tourne grâce à plusieurs composants distincts :
1. **Base de données** : PostgreSQL.
2. **Message Broker** : RabbitMQ (qui fait le pont entre les mini-programmes de l'application Desktop).
3. **Application Desktop (`mp3-desktop`)** : Scrute un dossier `chanson`, extrait les métadonnées (Titre, Album, Hash MD5 pour éviter les doublons), et pousse à l'API. Supporte les retry en cas d'échecs.
4. **Application Web (`mp3-web`)** : Enregistre en base de données les musiques, expose une page web pour visionner la bibliothèque de chansons, générer des Playlists automatisées selon différents filtres (durée max, langue, genre, artiste) et les télécharger en fichiers `.zip`.

---

## 🚀 Prérequis Système Exigés

Avant de procéder au lancement, assurez-vous de disposer des éléments suivants sur votre machine :

1. **Java 17** (Minimum requis)
2. **Maven 3.x**
3. **PostgreSQL** qui tourne avec :
   *   Base de données : `mp3_db`
   *   Port : `5432`
   *   Utilisateur : `postgres`
   *   Mot de passe : `Fifaliana!` *(à adapter dans mp3-web/src/main/resources/application.properties si besoin)*
4. **RabbitMQ** (avec *Erlang*), fonctionnel et en cours d'exécution sur votre ordinateur.

---

## 🏃 Comment démarrer l'application ?

L'environnement est divisé en deux répertoires de code (qui doivent idéalement être gérés par **deux terminaux séparés** ou en arrière-plan).

### 1. Démarrer l'Application Web (Spring Boot)
Ouvrez un terminal ou invite de commande (PowerShell), et placez-vous dans le répertoire du composant web :
```bash
cd mp3-web
mvn spring-boot:run
```
> Le serveur web démarrera sur le port **8080** : `http://localhost:8080/`.

### 2. Démarrer l'Application Desktop (Scanner MP3 & RabbitMQ)
Dans un nouveau terminal séparé, placez-vous dans le dossier desktop :
```bash
cd mp3-desktop
mvn clean compile exec:java
```

> **IMPORTANT :** Dès l'instant où l'application Desktop est lancée, un répertoire nommé `chanson` est automatiquement créé à la racine de `mp3-desktop` si celui-ci n'existait pas encore (`mp3-desktop/chanson/`).

---

## 🎮 Comment utiliser le système ?

Maintenant que tout est démarré :

### A. Alimenter la base de données (Processus Automatique)
1. Récupérez vos fichiers personnels `.mp3`.
2. Ouvrez ou glissez vos mp3 à l'intérieur de **`mp3-desktop/chanson/`**.
3. Attendez **quelques secondes**. (Le script les scanne automatiquement, par défaut pour le développement testé toutes les 10 secondes. Pour remettre le délai à *5 minutes*, modifiez la variable `DELAI_SCAN_MS` dans `Programme1.java`).
4. Remarquez que les chansons **disparaissent** du dossier `chanson/`.
5. Si un fichier `.mp3` possède un hachage identique (doublon parfait), il ne sera pas importé deux fois.

### B. Contrôler via l'Interface Web
1. Ouvrez votre navigateur web et visitez [http://localhost:8080](http://localhost:8080).
2. Cliquez sur l'onglet **Chansons** pour percevoir la musique que vous venez de glisser dans le dossier (ses métadonnées tel l'artiste, album et genre auront été autorenseignées).
3. Rendez-vous sur l'onglet **Playlists**. 
4. Cliquez sur **Générer une Playlist**.
5. Remplissez les critères souhaités (Exemple : *Durée maximum de 30 minutes*, *Genre : Rock*, etc.). Le système choisira le meilleur combo sans dépasser 30 minutes.
6. Une fois satisfait, cliquez sur **Télécharger ZIP**. Un fichier archive contenant purement vos MPs sera téléchargé, préservant la logique locale!

---

## 🗒️ Logs et Surveillance (Debug)

Si besoin, l'application desktop intègre un suivi rigoureux pour diagnostiquer les traitements séparément :
Chaque programme écrit son avancement dans les rapports positionnés dans : `mp3-desktop/logs/`
* `programme1.log` (Détection de dossier)
* `programme2.log` (Extraction MD5 & Tags MP3agic)
* `programme3.log` (Envoi API Rest & Anti-doublons POST)
