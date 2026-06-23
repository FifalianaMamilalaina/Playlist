package com.mp3.web.controller;

import com.mp3.web.model.Chanson;
import com.mp3.web.model.Playlist;
import com.mp3.web.service.ChansonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controleur pour les pages web Thymeleaf.
 * Gère les vues HTML pour le CRUD et les playlists.
 */
@Controller
public class WebController {

    @Autowired
    private ChansonService chansonService;

    // ======= Page d'accueil =======
    @GetMapping("/")
    public String accueil(Model model) {
        model.addAttribute("totalChansons", chansonService.listerTout().size());
        model.addAttribute("totalPlaylists", chansonService.listerPlaylists().size());
        return "accueil";
    }

    // ======= Pages Chansons =======

    @GetMapping("/chansons")
    public String listeChansons(@RequestParam(required = false) String recherche, Model model) {
        List<Chanson> chansons;
        if (recherche != null && !recherche.trim().isEmpty()) {
            chansons = chansonService.rechercherParTitre(recherche);
            model.addAttribute("recherche", recherche);
        } else {
            chansons = chansonService.listerTout();
        }
        model.addAttribute("chansons", chansons);
        return "chansons";
    }

    @GetMapping("/chansons/ajouter")
    public String formulaireAjouter(Model model) {
        model.addAttribute("chanson", new Chanson());
        return "chanson-form";
    }

    @PostMapping("/chansons/sauvegarder")
    public String sauvegarderChanson(@ModelAttribute Chanson chanson) {
        chansonService.sauvegarder(chanson);
        return "redirect:/chansons";
    }

    @GetMapping("/chansons/modifier/{id}")
    public String formulaireModifier(@PathVariable Long id, Model model) {
        return chansonService.trouverParId(id)
                .map(chanson -> {
                    model.addAttribute("chanson", chanson);
                    return "chanson-form";
                })
                .orElse("redirect:/chansons");
    }

    @GetMapping("/chansons/supprimer/{id}")
    public String suppressionChanson(@PathVariable Long id) {
        chansonService.supprimer(id);
        return "redirect:/chansons";
    }

    // ======= Pages Playlists =======

    @GetMapping("/playlists")
    public String listePlaylists(Model model) {
        model.addAttribute("playlists", chansonService.listerPlaylists());
        return "playlists";
    }

    @GetMapping("/playlists/generer")
    public String formulaireGenerer(Model model) {
        model.addAttribute("genres", chansonService.listerGenres());
        model.addAttribute("langues", chansonService.listerLangues());
        model.addAttribute("artistes", chansonService.listerArtistes());
        return "playlist-generer";
    }

    @GetMapping("/playlists/{id}")
    public String detailPlaylist(@PathVariable Long id, Model model) {
        return chansonService.trouverPlaylistParId(id)
                .map(playlist -> {
                    model.addAttribute("playlist", playlist);
                    // Charger toutes les chansons pour permettre le remplacement
                    model.addAttribute("toutesChansons", chansonService.listerTout());
                    return "playlist-detail";
                })
                .orElse("redirect:/playlists");
    }

    @GetMapping("/playlists/supprimer/{id}")
    public String suppressionPlaylist(@PathVariable Long id) {
        chansonService.supprimerPlaylist(id);
        return "redirect:/playlists";
    }
}
