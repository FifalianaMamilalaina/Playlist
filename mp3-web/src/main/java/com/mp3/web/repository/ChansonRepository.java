package com.mp3.web.repository;

import com.mp3.web.model.Chanson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChansonRepository extends JpaRepository<Chanson, Long> {

    // Trouver par hash (pour verification doublons)
    Optional<Chanson> findByHashFichier(String hashFichier);

    // Verifier si un hash existe deja
    boolean existsByHashFichier(String hashFichier);

    // Rechercher par titre (contient, insensible a la casse)
    List<Chanson> findByTitreContainingIgnoreCase(String titre);

    // Rechercher par artiste
    List<Chanson> findByArtisteContainingIgnoreCase(String artiste);

    // Rechercher par genre
    List<Chanson> findByGenreIgnoreCase(String genre);

    // Rechercher par langue
    List<Chanson> findByLangueIgnoreCase(String langue);

    // Lister tous les genres distincts
    @Query("SELECT DISTINCT c.genre FROM Chanson c WHERE c.genre IS NOT NULL ORDER BY c.genre")
    List<String> findDistinctGenres();

    // Lister toutes les langues distinctes
    @Query("SELECT DISTINCT c.langue FROM Chanson c WHERE c.langue IS NOT NULL ORDER BY c.langue")
    List<String> findDistinctLangues();

    // Lister tous les artistes distincts
    @Query("SELECT DISTINCT c.artiste FROM Chanson c WHERE c.artiste IS NOT NULL ORDER BY c.artiste")
    List<String> findDistinctArtistes();

    // Recherche multi-criteres pour generation de playlist
    @Query("SELECT c FROM Chanson c WHERE " +
           "(CAST(:genre AS String) IS NULL OR LOWER(c.genre) = LOWER(CAST(:genre AS String))) AND " +
           "(CAST(:langue AS String) IS NULL OR LOWER(c.langue) = LOWER(CAST(:langue AS String))) AND " +
           "(:hasArtistes = false OR c.artiste IN :artistes) AND " +
           "(CAST(:anneeMin AS Integer) IS NULL OR c.annee >= :anneeMin) AND " +
           "(CAST(:anneeMax AS Integer) IS NULL OR c.annee <= :anneeMax) " +
           "ORDER BY c.titre")
    List<Chanson> rechercherParCriteres(
            @Param("genre") String genre,
            @Param("langue") String langue,
            @Param("hasArtistes") boolean hasArtistes,
            @Param("artistes") List<String> artistes,
            @Param("anneeMin") Integer anneeMin,
            @Param("anneeMax") Integer anneeMax
    );
}
