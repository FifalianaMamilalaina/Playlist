package com.mp3.web.controller;

import com.mp3.web.model.Chanson;
import com.mp3.web.model.Playlist;
import com.mp3.web.service.ChansonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * API REST pour les playlists.
 * Gestion de la generation, modification et telechargement.
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistApiController {

    @Autowired
    private ChansonService chansonService;

    // GET /api/playlists - Lister toutes les playlists
    @GetMapping
    public List<Playlist> listerTout() {
        return chansonService.listerPlaylists();
    }

    // GET /api/playlists/{id} - Detail d'une playlist
    @GetMapping("/{id}")
    public ResponseEntity<Playlist> trouverParId(@PathVariable Long id) {
        return chansonService.trouverPlaylistParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Helper method pour eviter la duplication
    private List<String> extraireListe(Object obj) {
        if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) obj;
            return list.isEmpty() ? null : list;
        }
        return null;
    }

    // POST /api/playlists/generer - Generer une playlist selon criteres
    @PostMapping("/generer")
    public ResponseEntity<Playlist> generer(@RequestBody Map<String, Object> criteres) {
        String nom = (String) criteres.getOrDefault("nom", "Ma Playlist");
        Integer dureeMax = criteres.get("dureeMaxSecondes") != null
                ? Integer.parseInt(criteres.get("dureeMaxSecondes").toString()) : null;

        List<String> genres = extraireListe(criteres.get("genres"));
        List<String> langues = extraireListe(criteres.get("langues"));
        List<String> artistes = extraireListe(criteres.get("artistes"));

        Integer anneeMin = criteres.get("anneeMin") != null
                ? Integer.parseInt(criteres.get("anneeMin").toString()) : null;
        Integer anneeMax = criteres.get("anneeMax") != null
                ? Integer.parseInt(criteres.get("anneeMax").toString()) : null;

        Playlist playlist = chansonService.genererPlaylist(
                nom, dureeMax, genres, langues, artistes, anneeMin, anneeMax
        );

        return ResponseEntity.status(201).body(playlist);
    }

    // POST /api/playlists/fusionner - Fusionner plusieurs playlists
    @PostMapping("/fusionner")
    public ResponseEntity<Playlist> fusionner(@RequestBody Map<String, Object> body) {
        String nom = (String) body.getOrDefault("nom", "Playlist Fusionnée");
        if (body.containsKey("playlistIds")) {
            @SuppressWarnings("unchecked")
            List<Integer> idsRaw = (List<Integer>) body.get("playlistIds");
            List<Long> ids = idsRaw.stream().map(Long::valueOf).toList();
            
            Playlist playlist = chansonService.fusionnerPlaylists(ids, nom);
            return ResponseEntity.status(201).body(playlist);
        }
        return ResponseEntity.badRequest().build();
    }

    // PUT /api/playlists/{id} - Modifier une playlist (changer les chansons)
    @PutMapping("/{id}")
    public ResponseEntity<Playlist> modifier(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return chansonService.trouverPlaylistParId(id)
                .map(playlist -> {
                    // Mettre a jour le nom si fourni
                    if (body.containsKey("nom")) {
                        playlist.setNom((String) body.get("nom"));
                    }

                    // Mettre a jour les chansons si une liste d'IDs est fournie
                    if (body.containsKey("chansonIds")) {
                        @SuppressWarnings("unchecked")
                        List<Integer> ids = (List<Integer>) body.get("chansonIds");
                        List<Chanson> nouvelles = ids.stream()
                                .map(chansonId -> chansonService.trouverParId(Long.valueOf(chansonId)))
                                .filter(java.util.Optional::isPresent)
                                .map(java.util.Optional::get)
                                .toList();
                        playlist.getChansons().clear();
                        playlist.getChansons().addAll(nouvelles);
                    }

                    return ResponseEntity.ok(chansonService.sauvegarderPlaylist(playlist));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/playlists/{id} - Supprimer une playlist
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        if (chansonService.trouverPlaylistParId(id).isPresent()) {
            chansonService.supprimerPlaylist(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/playlists/{id}/telecharger - Telecharger la playlist en ZIP
    @GetMapping("/{id}/telecharger")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        return chansonService.trouverPlaylistParId(id)
                .map(playlist -> {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ZipOutputStream zos = new ZipOutputStream(baos);

                        int index = 1;
                        for (Chanson chanson : playlist.getChansons()) {
                            if (chanson.getCheminFichier() != null) {
                                Path fichierMp3 = Paths.get(chanson.getCheminFichier());
                                if (Files.exists(fichierMp3)) {
                                    String nomFichier = String.format("%02d - %s - %s.mp3",
                                            index, chanson.getArtiste(), chanson.getTitre());
                                    zos.putNextEntry(new ZipEntry(nomFichier));
                                    Files.copy(fichierMp3, zos);
                                    zos.closeEntry();
                                    index++;
                                }
                            }
                        }

                        zos.close();
                        byte[] contenu = baos.toByteArray();

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment",
                                playlist.getNom().replaceAll("[^a-zA-Z0-9]", "_") + ".zip");

                        return ResponseEntity.ok()
                                .headers(headers)
                                .body(contenu);

                    } catch (IOException e) {
                        return ResponseEntity.internalServerError().<byte[]>build();
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
