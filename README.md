# Projet Gestion MP3 avec RabbitMQ et Spring Boot

Un système complet composé de deux applications (Desktop et Web) permettant d'automatiser l'extraction des métadonnées de musiques `.mp3`, leur sauvegarde dans une base de données, et la génération de listes de lecture intelligentes via une interface graphique web.

## 📋 Architecture Globale
Le projet tourne grâce à plusieurs composants distincts :
1. **Base de données** : PostgreSQL.
2. **Message Broker** : RabbitMQ (qui fait le pont entre les mini-programmes de l'application Desktop).
3. **Application Desktop (`mp3-desktop`)** : Scrute un dossier `chanson`, extrait les métadonnées (Titre, Album, Hash MD5 pour éviter les doublons), vérifie la **blacklist** (genres/artistes bloqués), et uploade les fichiers MP3 + métadonnées vers l'API. Supporte les retry en cas d'échecs.
4. **Application Web (`mp3-web`)** : Enregistre en base de données les musiques et les fichiers MP3, expose une page web pour visionner la bibliothèque de chansons avec **lecture audio en ligne**, générer des Playlists automatisées selon différents filtres multiples (durée max, langues, genres, artistes) et les télécharger en fichiers `.zip` contenant les vrais fichiers MP3.

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
6. Les fichiers MP3 sont **uploadés physiquement** vers le serveur web (dans `mp3-web/uploads/`) pour permettre la lecture en ligne et le téléchargement ZIP.

### B. Contrôler via l'Interface Web
1. Ouvrez votre navigateur web et visitez [http://localhost:8080](http://localhost:8080).
2. Cliquez sur l'onglet **Chansons** pour percevoir la musique que vous venez de glisser dans le dossier (ses métadonnées tel l'artiste, album et genre auront été autorenseignées).
3. **Écoutez** vos chansons directement depuis le navigateur grâce au **lecteur audio intégré** (bouton Play sur chaque chanson).
4. Rendez-vous sur l'onglet **Playlists**.
5. Cliquez sur **Générer une Playlist**.
6. Remplissez les critères souhaités :
   - **Genres musicaux** : sélection multiple (maintenir `Ctrl` pour plusieurs choix)
   - **Langues** : sélection multiple (maintenir `Ctrl` pour plusieurs choix)
   - **Artistes** : sélection multiple (maintenir `Ctrl` pour plusieurs choix)
   - **Durée maximale** : en minutes
7. Le système choisira le meilleur combo de chansons sans dépasser la durée maximale.
8. Une fois satisfait, cliquez sur **Télécharger ZIP**. Un fichier archive contenant vos **vrais fichiers MP3** sera téléchargé !
9. Vous pouvez aussi **écouter** chaque chanson directement depuis la vue détaillée de la playlist.

---

## 🚫 Blacklist (Filtrage Automatique)

Le fichier `mp3-desktop/blacklist.txt` permet de bloquer automatiquement certaines chansons **avant** qu'elles ne soient envoyées à l'application web.

### Format du fichier
```
[GENRES]
# Un genre par ligne (insensible à la casse)
Metal
Hardcore

[ARTISTES]
# Un artiste par ligne (insensible à la casse)
Unknown Artist
Artiste Inconnu
```

### Fonctionnement
- Les lignes commençant par `#` sont des **commentaires** (ignorées)
- La vérification est **insensible à la casse** ("metal" = "Metal" = "METAL")
- Si le **genre** OU l'**artiste** d'une chanson correspond à une entrée de la blacklist, le fichier est **supprimé du répertoire** sans être envoyé à l'API
- Le fichier est lu au **démarrage** du Programme 3. Pour appliquer des modifications, relancez l'application Desktop
- Le log affiche clairement les chansons bloquées : `BLACKLIST - Chanson bloquee : 'xxx'`

---

## 🔀 Fusion de Playlists

Depuis la page **Playlists** (`http://localhost:8080/playlists`), vous pouvez fusionner plusieurs playlists en une seule :

1. Cochez les cases en haut à droite de chaque playlist à fusionner (minimum 2).
2. Un bouton jaune **"Fusionner la sélection"** apparaît automatiquement en haut de la page.
3. Cliquez dessus, entrez le nom de la nouvelle playlist.
4. La nouvelle playlist contiendra toutes les chansons des playlists sélectionnées, **sans doublon**.

---

## ⏱️ Limite de Durée (Filtrage Automatique)

Le fichier `mp3-desktop/limite_duree.txt` permet de définir une durée maximale (en secondes) pour les chansons acceptées.

### Format du fichier
```
# Durée maximale en secondes
3600
```

### Fonctionnement
- Les lignes commençant par `#` sont des **commentaires** (ignorées)
- Si la durée d'une chanson **dépasse** la limite, elle n'est **ni envoyée à l'API, ni supprimée** du répertoire `chanson/`
- Le fichier est lu au **démarrage** du Programme 3
- Le log affiche : `LIMITE DUREE - Chanson bloquee : 'xxx' (300 s > 120 s)`

---

## 🗒️ Logs et Surveillance (Debug)

Si besoin, l'application desktop intègre un suivi rigoureux pour diagnostiquer les traitements séparément :
Chaque programme écrit son avancement dans les rapports positionnés dans : `mp3-desktop/logs/`
* `programme1.log` (Détection de dossier)
* `programme2.log` (Extraction MD5 & Tags MP3agic)
* `programme3.log` (Envoi API Rest & Anti-doublons POST & Blacklist & Limite durée)
* `programme4.log` (Suppression des fichiers du disque)
