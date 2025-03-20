package egg.ProyectoFinal.repositorios;

import egg.ProyectoFinal.entidades.Articulo;
import egg.ProyectoFinal.entidades.Fabrica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticuloRepositorio extends JpaRepository<Articulo,Long> {
    @Query("SELECT a FROM Articulo a WHERE a.id = :id")
    public Articulo buscarPorId(@Param("id") Long Id);

    @Query("SELECT COALESCE(MAX(a.nroArticulo), 0) FROM Articulo a")
    int findMaxNroArticulo();
}
