package egg.ProyectoFinal.servicios;


import egg.ProyectoFinal.entidades.Fabrica;
import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.repositorios.FabricaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FabricaServicio {

    @Autowired
    FabricaRepositorio fabricaRepositorio;

    @Transactional(readOnly = true)
    public List<Fabrica> listarFabricas() {

        List<Fabrica> fabricas = new ArrayList<>();

        fabricas = fabricaRepositorio.findAll();
        return fabricas;
    }

    @Transactional(readOnly = true)
    public Fabrica getOne(Long id) {
        return fabricaRepositorio.findById(id).orElse(null);
    }

    @Transactional
    public void crearFabrica(String nombre){
        Fabrica fabrica = new Fabrica();

        fabrica.setNombreFabrica(nombre);

        fabricaRepositorio.save(fabrica);

    }

    @Transactional
    public void modificarFabrica (Long id, String nombre) throws MiExcepcion{
        validar(nombre);
        Optional<Fabrica> respuesta = fabricaRepositorio.findById(id);

        if (respuesta.isPresent()){
            Fabrica fabrica = respuesta.get();
            fabrica.setNombreFabrica(nombre);
            fabricaRepositorio.save(fabrica);

        }else {
            throw new MiExcepcion("No se encontró una fàbrica con el ID especificado");
        }

    }

    @Transactional
    public void eliminar(Long id) throws MiExcepcion{
        Optional<Fabrica> fabricaOpt = fabricaRepositorio.findById(id);
        if(fabricaOpt.isPresent()){
            fabricaRepositorio.delete(fabricaOpt.get());
        } else {
            throw new MiExcepcion("La fábrica con Id especificado no existe");
        }
    }


    private void validar(String nombre) throws MiExcepcion {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiExcepcion("El nombre no puede ser nulo o estar vacío");
        }
    }
}
