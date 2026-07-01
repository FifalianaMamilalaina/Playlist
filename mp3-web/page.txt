<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Miranaaaa - Gestion MP3</title>
    <!-- On utilise Bootstrap via CDN pour un design propre et rapide -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <style>
        body { padding-top: 60px; background-color: #f8f9fa; }
        .navbar { background-color: #343a40 !important; }
        .card { box-shadow: 0 4px 6px rgba(0,0,0,0.1); margin-bottom: 20px; border: none; border-radius: 10px; }
        .table { background-color: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
        .table thead { background-color: #e9ecef; }
        .btn-action { margin-right: 5px; }
    </style>
</head>
<body>

<nav class="navbar navbar-expand-md navbar-dark fixed-top bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/"><i class="bi bi-music-note-list"></i> MP3 Web</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarCollapse">
            <ul class="navbar-nav me-auto mb-2 mb-md-0">
                <li class="nav-item">
                    <a class="nav-link" href="/">Accueil</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/chansons">Chansons</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/playlists">Playlists</a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<div class="container mt-4 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-3">
        <div>
            <h2><i class="bi bi-collection-play-fill text-success"></i> <span id="titre-playlist">Miranaaaa</span></h2>
            <p class="text-muted mb-0">
                Générée le <span>01/07/2026</span> | 
                Durée: <span class="fw-bold">45min 32s</span>
                <span> / Objectif max: <span>48 min</span></span>
            </p>
        </div>
        <div class="d-flex gap-2">
            <button class="btn btn-outline-primary" onclick="renommer()">
                <i class="bi bi-pencil"></i> Renommer
            </button>
            <a href="/api/playlists/5/telecharger" class="btn btn-primary d-flex align-items-center gap-2">
                <i class="bi bi-file-earmark-zip-fill"></i> Télécharger ZIP
            </a>
        </div>
    </div>

    <!-- Interface d'édition (Vue de la playlist) -->
    <div class="row">
        <!-- Chansons dans la playlist -->
        <div class="col-md-7 mb-4">
            <div class="card border-success h-100">
                <div class="card-header bg-success text-white d-flex justify-content-between align-items-center">
                    <h5 class="mb-0"><i class="bi bi-list-check"></i> Chansons sélectionnées (<span id="compteur">10</span>)</h5>
                    <button class="btn btn-sm btn-light" onclick="sauvegarderChangements()">Sauvegarder les modifications</button>
                </div>
                <div class="card-body p-0">
                    <ul class="list-group list-group-flush" id="liste-playlist">
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="28">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">Bodo</h6>
                                <small class="text-muted"> - 3:50</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/28/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="31">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">?Louane - Maman (2015) Official Music Video</h6>
                                <small class="text-muted">?Nils - 2:42</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/31/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="32">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">Matsiro kosa ny nanaovan_i Fy Rasolofoniaina ny hiran_ny tarika _Zay teto.(480P_SD)</h6>
                                <small class="text-muted"> - 4:50</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/32/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="37">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">?Tongasoa</h6>
                                <small class="text-muted">?REKO BAND - 6:42</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/37/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="34">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">?MITONIA - REKO (Official lyrics video)</h6>
                                <small class="text-muted">?Reko Band - 5:32</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/34/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="30">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">?HATRAIZA / AZA AVELA - REKO</h6>
                                <small class="text-muted">?Reko Band - 5:46</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/30/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="35">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">?Ny Anaranao</h6>
                                <small class="text-muted">?REKO BAND - 4:50</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/35/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="38">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">Antanambao</h6>
                                <small class="text-muted">Bekoto Augustin - 2:55</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/38/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="39">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">IAO</h6>
                                <small class="text-muted">ROSSY - 3:48</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/39/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center" 
                            data-id="33">
                            <div>
                                <h6 class="mb-0 text-primary fw-bold">?Misia</h6>
                                <small class="text-muted">?REKO BAND - 4:37</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <audio controls preload="none" style="height: 35px; width: 180px;">
                                    <source src="/api/chansons/33/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item text-center text-muted py-4" id="vide-msg" style="display:none">
                            La playlist est vide.
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <!-- Catalogue disponible pour ajouter -->
        <div class="col-md-5 mb-4">
            <div class="card bg-light h-100">
                <div class="card-header border-bottom">
                    <h5 class="mb-0">Ajouter des chansons</h5>
                </div>
                <div class="card-body p-2" style="max-height: 600px; overflow-y: auto;">
                    <input type="text" id="recherche-catalogue" class="form-control form-control-sm mb-3" placeholder="Rechercher pour ajouter..." onkeyup="filtrerCatalogue()">
                    <ul class="list-group list-group-flush" id="liste-catalogue">
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="28" data-titre="bodo">
                            <div>
                                <h6 class="mb-0 fw-bold">Bodo</h6>
                                <small class="text-muted"> - 3:50</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/28/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="29" data-titre="?happier than ever">
                            <div>
                                <h6 class="mb-0 fw-bold">?Happier Than Ever</h6>
                                <small class="text-muted">?Billie Eilish - 5:15</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/29/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="30" data-titre="?hatraiza / aza avela - reko">
                            <div>
                                <h6 class="mb-0 fw-bold">?HATRAIZA / AZA AVELA - REKO</h6>
                                <small class="text-muted">?Reko Band - 5:46</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/30/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="31" data-titre="?louane - maman (2015) official music video">
                            <div>
                                <h6 class="mb-0 fw-bold">?Louane - Maman (2015) Official Music Video</h6>
                                <small class="text-muted">?Nils - 2:42</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/31/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="32" data-titre="matsiro kosa ny nanaovan_i fy rasolofoniaina ny hiran_ny tarika _zay teto.(480p_sd)">
                            <div>
                                <h6 class="mb-0 fw-bold">Matsiro kosa ny nanaovan_i Fy Rasolofoniaina ny hiran_ny tarika _Zay teto.(480P_SD)</h6>
                                <small class="text-muted"> - 4:50</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/32/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="33" data-titre="?misia">
                            <div>
                                <h6 class="mb-0 fw-bold">?Misia</h6>
                                <small class="text-muted">?REKO BAND - 4:37</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/33/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="34" data-titre="?mitonia - reko (official lyrics video)">
                            <div>
                                <h6 class="mb-0 fw-bold">?MITONIA - REKO (Official lyrics video)</h6>
                                <small class="text-muted">?Reko Band - 5:32</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/34/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="35" data-titre="?ny anaranao">
                            <div>
                                <h6 class="mb-0 fw-bold">?Ny Anaranao</h6>
                                <small class="text-muted">?REKO BAND - 4:50</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/35/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="36" data-titre="?rotoroto">
                            <div>
                                <h6 class="mb-0 fw-bold">?Rotoroto</h6>
                                <small class="text-muted">?REKO BAND - 4:18</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/36/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="37" data-titre="?tongasoa">
                            <div>
                                <h6 class="mb-0 fw-bold">?Tongasoa</h6>
                                <small class="text-muted">?REKO BAND - 6:42</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/37/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="38" data-titre="antanambao">
                            <div>
                                <h6 class="mb-0 fw-bold">Antanambao</h6>
                                <small class="text-muted">Bekoto Augustin - 2:55</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/38/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                        <li class="list-group-item d-flex justify-content-between align-items-center bg-transparent catalogue-item" 
                            data-id="39" data-titre="iao">
                            <div>
                                <h6 class="mb-0 fw-bold">IAO</h6>
                                <small class="text-muted">ROSSY - 3:48</small>
                            </div>
                            <div class="d-flex align-items-center gap-1">
                                <audio controls preload="none" style="height: 35px; width: 120px;">
                                    <source src="/api/chansons/39/stream" type="audio/mpeg">
                                </audio>
                                <button class="btn btn-sm btn-outline-success" onclick="ajouterALaPlaylist(this, [[${c.id}]], '[[${c.titre}]]', '[[${c.artiste}]]', '[[${c.dureeFormatee}]]')"><i class="bi bi-plus-lg"></i></button>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Animations simples
        document.addEventListener('DOMContentLoaded', (event) => {
            const cards = document.querySelectorAll('.card, .table-responsive');
            cards.forEach((card, index) => {
                card.style.opacity = '0';
                card.style.transform = 'translateY(20px)';
                card.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                setTimeout(() => {
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, 100 * (index + 1));
            });
        });
    </script>
</div>
<script>
    const playlistId = 5;
</script>
<script>
    // Fonction d'ajout a la playlist visuelle
    function ajouterALaPlaylist(btn, id, titre, artiste, duree) {
        document.getElementById('vide-msg').style.display = 'none';
        
        const liste = document.getElementById('liste-playlist');
        
        // Verifier si deja dans la liste
        let existe = false;
        liste.querySelectorAll('li[data-id]').forEach(li => {
            if (li.getAttribute('data-id') == id) existe = true;
        });
        
        if (existe) {
            alert('Cette chanson est déjà dans la playlist.');
            return;
        }

        const li = document.createElement('li');
        li.className = 'list-group-item d-flex justify-content-between align-items-center border-success bg-light';
        li.setAttribute('data-id', id);
        li.innerHTML = `
            <div>
                <h6 class="mb-0 text-primary fw-bold">${titre} <span class="badge bg-success ms-1">Nouveau</span></h6>
                <small class="text-muted">${artiste} - ${duree}</small>
            </div>
            <div class="d-flex align-items-center gap-2">
                <audio controls preload="none" style="height: 35px; width: 180px;">
                    <source src="/api/chansons/${id}/stream" type="audio/mpeg">
                </audio>
                <button class="btn btn-sm btn-outline-danger" onclick="retirerDePlaylist(this)"><i class="bi bi-x-lg"></i></button>
            </div>
        `;
        liste.appendChild(li);
        majCompteur();
    }

    // Fonction de retrait de la playlist visuelle
    function retirerDePlaylist(btn) {
        btn.closest('li').remove();
        majCompteur();
        
        const liste = document.getElementById('liste-playlist');
        if (liste.querySelectorAll('li[data-id]').length === 0) {
            document.getElementById('vide-msg').style.display = 'block';
        }
    }

    // Mise a jour compteur
    function majCompteur() {
        const count = document.getElementById('liste-playlist').querySelectorAll('li[data-id]').length;
        document.getElementById('compteur').innerText = count;
    }

    // Filtrage simple du catalogue
    function filtrerCatalogue() {
        const terme = document.getElementById('recherche-catalogue').value.toLowerCase();
        document.querySelectorAll('.catalogue-item').forEach(item => {
            const titre = item.getAttribute('data-titre');
            if (titre.includes(terme)) {
                item.style.setProperty('display', 'flex', 'important');
            } else {
                item.style.setProperty('display', 'none', 'important');
            }
        });
    }

    // Renommer
    function renommer() {
        const actuel = document.getElementById('titre-playlist').innerText;
        const nouveau = prompt('Nouveau nom pour la playlist:', actuel);
        if (nouveau && nouveau.trim() !== '' && nouveau !== actuel) {
            fetch('/api/playlists/' + playlistId, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ nom: nouveau })
            }).then(() => {
                document.getElementById('titre-playlist').innerText = nouveau;
            });
        }
    }

    // Sauvegarder dans la BDD (envoyer les IDs)
    function sauvegarderChangements() {
        const ids = [];
        document.getElementById('liste-playlist').querySelectorAll('li[data-id]').forEach(li => {
            ids.push(parseInt(li.getAttribute('data-id')));
        });

        fetch('/api/playlists/' + playlistId, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ chansonIds: ids })
        })
        .then(res => {
            if (!res.ok) throw new Error("Erreur de sauvegarde");
            alert("Playlist mise à jour avec succès !");
            window.location.reload();
        })
        .catch(err => alert(err.message));
    }
</script>
</body>
</html>

