package com.mp3.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "playlist")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "duree_max_secondes")
    private Integer dureeMaxSecondes;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @ManyToMany
    @JoinTable(
        name = "playlist_chanson",
        joinColumns = @JoinColumn(name = "playlist_id"),
        inverseJoinColumns = @JoinColumn(name = "chanson_id")
    )
    @OrderColumn(name = "ordre")
    private List<Chanson> chansons = new ArrayList<>();

    // Constructeur par defaut
    public Playlist() {
        this.dateCreation = LocalDateTime.now();
    }

    public Playlist(String nom, Integer dureeMaxSecondes) {
        this.nom = nom;
        this.dureeMaxSecondes = dureeMaxSecondes;
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Integer getDureeMaxSecondes() { return dureeMaxSecondes; }
    public void setDureeMaxSecondes(Integer dureeMaxSecondes) { this.dureeMaxSecondes = dureeMaxSecondes; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public List<Chanson> getChansons() { return chansons; }
    public void setChansons(List<Chanson> chansons) { this.chansons = chansons; }

    // Duree totale de la playlist en secondes
    public int getDureeTotaleSecondes() {
        return chansons.stream()
                .filter(c -> c.getDureeSecondes() != null)
                .mapToInt(Chanson::getDureeSecondes)
                .sum();
    }

    // Duree totale formatee
    public String getDureeTotaleFormatee() {
        int total = getDureeTotaleSecondes();
        int heures = total / 3600;
        int minutes = (total % 3600) / 60;
        int secondes = total % 60;
        if (heures > 0) {
            return String.format("%dh %02dmin %02ds", heures, minutes, secondes);
        }
        return String.format("%dmin %02ds", minutes, secondes);
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", nbChansons=" + chansons.size() +
                ", dureeTotale=" + getDureeTotaleFormatee() +
                '}';
    }
}
