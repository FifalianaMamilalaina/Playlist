package com.mp3.web.service;

import com.mp3.web.model.Chanson;
import com.mp3.web.model.Playlist;
import com.mp3.web.repository.ChansonRepository;
import com.mp3.web.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ChansonService {

    @Autowired
    private ChansonRepository chansonRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    // ======= CRUD Chanson =======

    public List<Chanson> listerTout() {
        return chansonRepository.findAll();
    }

    public Optional<Chanson> trouverParId(Long id) {
        return chansonRepository.findById(id);
    }

    public Chanson sauvegarder(Chanson chanson) {
        return chansonRepository.save(chanson);
    }

    public void supprimer(Long id) {
        chansonRepository.deleteById(id);
    }

    // ======= Verification doublons =======

    public boolean existeParHash(String hashFichier) {
        return chansonRepository.existsByHashFichier(hashFichier);
    }

    public Optional<Chanson> trouverParHash(String hashFichier) {
        return chansonRepository.findByHashFichier(hashFichier);
    }

    // ======= Recherche =======

    public List<Chanson> rechercherParTitre(String titre) {
        return chansonRepository.findByTitreContainingIgnoreCase(titre);
    }

    public List<String> listerGenres() {
        return chansonRepository.findDistinctGenres();
    }

    public List<String> listerLangues() {
        return chansonRepository.findDistinctLangues();
    }

    public List<String> listerArtistes() {
        return chansonRepository.findDistinctArtistes();
    }

    // ======= Generation de Playlist =======

    /**
     * Genere une playlist selon les criteres donnes.
     * La playlist est remplie de chansons correspondant aux critères,
     * sans depasser la duree maximale.
     */
    public Playlist genererPlaylist(String nom, Integer dureeMaxSecondes,
                                    List<String> genres, List<String> langues,
                                    List<String> artistes, Integer anneeMin, Integer anneeMax) {

        boolean hasGenres = genres != null && !genres.isEmpty();
        List<String> queryGenres = hasGenres ? genres : Collections.singletonList("");

        boolean hasLangues = langues != null && !langues.isEmpty();
        List<String> queryLangues = hasLangues ? langues : Collections.singletonList("");

        boolean hasArtistes = artistes != null && !artistes.isEmpty();
        List<String> queryArtistes = hasArtistes ? artistes : Collections.singletonList("");

        // Rechercher les chansons correspondant aux criteres
        List<Chanson> chansonsCorrespondantes = chansonRepository.rechercherParCriteres(
                hasGenres, queryGenres, hasLangues, queryLangues, hasArtistes, queryArtistes, anneeMin, anneeMax
        );

        // Melanger aleatoirement les resultats
        List<Chanson> chansonsMelangees = new ArrayList<>(chansonsCorrespondantes);
        Collections.shuffle(chansonsMelangees);

        // Creer la playlist
        Playlist playlist = new Playlist(nom, dureeMaxSecondes);
        int dureeCumulee = 0;

        for (Chanson chanson : chansonsMelangees) {
            int dureeChanson = chanson.getDureeSecondes() != null ? chanson.getDureeSecondes() : 0;

            // Si on a une duree max, verifier qu'on ne la depasse pas
            if (dureeMaxSecondes != null && dureeMaxSecondes > 0) {
                if (dureeCumulee + dureeChanson > dureeMaxSecondes) {
                    continue; // Passer cette chanson, elle est trop longue
                }
            }

            playlist.getChansons().add(chanson);
            dureeCumulee += dureeChanson;
        }

        // Sauvegarder la playlist
        return playlistRepository.save(playlist);
    }

    // ======= CRUD Playlist =======

    public List<Playlist> listerPlaylists() {
        return playlistRepository.findAll();
    }

    public Optional<Playlist> trouverPlaylistParId(Long id) {
        return playlistRepository.findById(id);
    }

    public Playlist sauvegarderPlaylist(Playlist playlist) {
        return playlistRepository.save(playlist);
    }

    public void supprimerPlaylist(Long id) {
        playlistRepository.deleteById(id);
    }
}
