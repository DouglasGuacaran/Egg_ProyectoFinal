package egg.ProyectoFinal.repositorios;

import egg.ProyectoFinal.entidades.Fabrica;
import egg.ProyectoFinal.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FabricaRepositorio extends JpaRepository<Fabrica,Long> {



}
