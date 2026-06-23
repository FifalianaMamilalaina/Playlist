package com.mp3.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chanson")
public class Chanson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    private String artiste;

    private String album;

    private String genre;

    private String langue;

    @Column(name = "duree_secondes")
    private Integer dureeSecondes;

    private Integer annee;

    @Column(name = "chemin_fichier")
    private String cheminFichier;

    @Column(name = "hash_fichier", unique = true)
    private String hashFichier;

    @Column(name = "date_ajout")
    private LocalDateTime dateAjout;

    // Constructeur par defaut
    public Chanson() {
        this.dateAjout = LocalDateTime.now();
    }

    // Constructeur complet
    public Chanson(String titre, String artiste, String album, String genre,
                   String langue, Integer dureeSecondes, Integer annee,
                   String cheminFichier, String hashFichier) {
        this.titre = titre;
        this.artiste = artiste;
        this.album = album;
        this.genre = genre;
        this.langue = langue;
        this.dureeSecondes = dureeSecondes;
        this.annee = annee;
        this.cheminFichier = cheminFichier;
        this.hashFichier = hashFichier;
        this.dateAjout = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getArtiste() { return artiste; }
    public void setArtiste(String artiste) { this.artiste = artiste; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getLangue() { return langue; }
    public void setLangue(String langue) { this.langue = langue; }

    public Integer getDureeSecondes() { return dureeSecondes; }
    public void setDureeSecondes(Integer dureeSecondes) { this.dureeSecondes = dureeSecondes; }

    public Integer getAnnee() { return annee; }
    public void setAnnee(Integer annee) { this.annee = annee; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }

    public String getHashFichier() { return hashFichier; }
    public void setHashFichier(String hashFichier) { this.hashFichier = hashFichier; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }

    // Methode utilitaire pour afficher la duree en format mm:ss
    public String getDureeFormatee() {
        if (dureeSecondes == null) return "N/A";
        int minutes = dureeSecondes / 60;
        int secondes = dureeSecondes % 60;
        return String.format("%d:%02d", minutes, secondes);
    }

    @Override
    public String toString() {
        return "Chanson{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", artiste='" + artiste + '\'' +
                ", album='" + album + '\'' +
                ", duree=" + getDureeFormatee() +
                '}';
    }
}
