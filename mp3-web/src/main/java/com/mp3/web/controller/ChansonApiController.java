package com.mp3.web.controller;

import com.mp3.web.model.Chanson;
import com.mp3.web.service.ChansonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API REST pour les chansons.
 * Utilisee par le Programme3 (desktop) pour envoyer les chansons
 * et verifier les doublons.
 */
@RestController
@RequestMapping("/api/chansons")
public class ChansonApiController {

    @Autowired
    private ChansonService chansonService;

    @Value("${mp3.storage.path:./uploads}")
    private String storagePath;

    // GET /api/chansons - Lister toutes les chansons
    @GetMapping
    public List<Chanson> listerTout() {
        return chansonService.listerTout();
    }

    // GET /api/chansons/{id} - Detail d'une chanson
    @GetMapping("/{id}")
    public ResponseEntity<Chanson> trouverParId(@PathVariable Long id) {
        return chansonService.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/chansons/{id}/stream - Lire le fichier MP3 en direct
    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamMp3(@PathVariable Long id) {
        java.util.Optional<Chanson> opt = chansonService.trouverParId(id);
        if (opt.isPresent()) {
            Chanson chanson = opt.get();
            try {
                Path file = Paths.get(chanson.getCheminFichier());
                Resource resource = new UrlResource(file.toUri());
                if (resource.exists() || resource.isReadable()) {
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType("audio/mpeg"))
                            .body(resource);
                }
            } catch (Exception e) {}
        }
        return ResponseEntity.notFound().build();
    }

    // POST /api/chansons - Ajouter une chanson
    @PostMapping
    public ResponseEntity<Chanson> ajouter(@RequestBody Chanson chanson) {
        // Verifier si la chanson existe deja (doublon)
        if (chanson.getHashFichier() != null && chansonService.existeParHash(chanson.getHashFichier())) {
            return ResponseEntity.status(409).build(); // 409 Conflict = doublon
        }
        Chanson sauvegardee = chansonService.sauvegarder(chanson);
        return ResponseEntity.status(201).body(sauvegardee);
    }

    // PUT /api/chansons/{id} - Modifier une chanson
    @PutMapping("/{id}")
    public ResponseEntity<Chanson> modifier(@PathVariable Long id, @RequestBody Chanson chanson) {
        return chansonService.trouverParId(id)
                .map(existante -> {
                    chanson.setId(id);
                    chanson.setDateAjout(existante.getDateAjout());
                    return ResponseEntity.ok(chansonService.sauvegarder(chanson));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/chansons/{id} - Supprimer une chanson
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        if (chansonService.trouverParId(id).isPresent()) {
            chansonService.supprimer(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // GET /api/chansons/existe?hash=xxx - Verifier si une chanson existe (anti-doublons)
    @GetMapping("/existe")
    public ResponseEntity<Map<String, Boolean>> verifierExistence(@RequestParam String hash) {
        boolean existe = chansonService.existeParHash(hash);
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    // GET /api/chansons/recherche?titre=xxx - Rechercher par titre
    @GetMapping("/recherche")
    public List<Chanson> rechercher(@RequestParam String titre) {
        return chansonService.rechercherParTitre(titre);
    }

    // POST /api/chansons/upload - Recevoir fichier MP3 + metadonnees
    @PostMapping("/upload")
    public ResponseEntity<Chanson> uploadMp3(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam("titre") String titre,
            @RequestParam(value = "artiste", required = false) String artiste,
            @RequestParam(value = "album", required = false) String album,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "langue", required = false) String langue,
            @RequestParam(value = "dureeSecondes", required = false) Integer dureeSecondes,
            @RequestParam(value = "annee", required = false) Integer annee,
            @RequestParam(value = "hashFichier", required = false) String hashFichier
    ) {
        try {
            // Verifier doublon
            if (hashFichier != null && chansonService.existeParHash(hashFichier)) {
                return ResponseEntity.status(409).build();
            }

            // Creer le dossier uploads s'il n'existe pas
            Path uploadDir = Paths.get(storagePath).toAbsolutePath();
            Files.createDirectories(uploadDir);

            // Nom de fichier securise
            String nomFichier = System.currentTimeMillis() + "_" + fichier.getOriginalFilename()
                    .replaceAll("[^a-zA-Z0-9._-]", "_");
            Path destination = uploadDir.resolve(nomFichier);
            fichier.transferTo(destination.toFile());

            // Creer l'entite Chanson
            Chanson chanson = new Chanson();
            chanson.setTitre(titre);
            chanson.setArtiste(artiste);
            chanson.setAlbum(album);
            chanson.setGenre(genre);
            chanson.setLangue(langue);
            chanson.setDureeSecondes(dureeSecondes);
            chanson.setAnnee(annee);
            chanson.setHashFichier(hashFichier);
            chanson.setCheminFichier(destination.toString());
            chanson.setDateAjout(LocalDateTime.now());

            Chanson sauvegardee = chansonService.sauvegarder(chanson);
            return ResponseEntity.status(201).body(sauvegardee);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
