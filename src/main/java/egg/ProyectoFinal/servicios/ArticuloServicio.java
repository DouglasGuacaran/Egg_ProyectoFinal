package egg.ProyectoFinal.servicios;

import egg.ProyectoFinal.entidades.Articulo;
import egg.ProyectoFinal.entidades.Fabrica;
import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.repositorios.ArticuloRepositorio;
import egg.ProyectoFinal.repositorios.FabricaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ArticuloServicio {

    @Autowired
    private ArticuloRepositorio articuloRepositorio;

    @Autowired
    private FabricaRepositorio fabricaRepositorio;

    @Transactional(readOnly = true)
    public List<Articulo> listarArticulos(){

        List<Articulo> articulos = new ArrayList<>();

        articulos = articuloRepositorio.findAll();
        return articulos;
    }

    @Transactional(readOnly = true)
    public Articulo getOne(Long id) {
        return articuloRepositorio.findById(id).orElse(null);
    }


    @Transactional
    public void crearArticulo(String nombreArticulo, String descripcionArticulo, Long fabricaId,int cantidad) throws MiExcepcion{

        Optional<Fabrica> respuestaFabrica = fabricaRepositorio.findById(fabricaId);
        if (respuestaFabrica.isEmpty()) {
            throw new MiExcepcion("La fábrica no existe");
        }

        validar(nombreArticulo, descripcionArticulo, cantidad, fabricaId);

        //Obtener el último número de artículo registrado en la base de datos
        int ultimoNroArticulo = articuloRepositorio.findMaxNroArticulo();

        Articulo articulo = new Articulo();
        articulo.setNombreArticulo(nombreArticulo);
        articulo.setNroArticulo(ultimoNroArticulo + 1);
        articulo.setDescripcionArticulo(descripcionArticulo);
        articulo.setFabrica(respuestaFabrica.get());
        articulo.setCantidad(cantidad);

        articuloRepositorio.save(articulo);

    }

    @Transactional
    public void modificarArticulo (Long id, String nombreArticulo, Integer nroArticulo, String descripcionArticulo, int cantidad, Long fabricaId) throws MiExcepcion {
        validar(nombreArticulo,descripcionArticulo,cantidad,fabricaId);
        Optional<Articulo> respuesta = articuloRepositorio.findById(id);
        Optional<Fabrica> respuestaFabrica = fabricaRepositorio.findById(fabricaId);

        if (respuesta.isPresent()){
            Articulo articulo = respuesta.get();
            articulo.setNroArticulo(nroArticulo);
            articulo.setNombreArticulo(nombreArticulo);
            articulo.setDescripcionArticulo(descripcionArticulo);
            articulo.setCantidad(cantidad);

            articulo.setFabrica(respuestaFabrica.get());
            articuloRepositorio.save(articulo);

        } else {
            throw new MiExcepcion("No se encontró un Artículo con el ID especificado");
        }

    }

    @Transactional
    public void eliminar(Long id) throws MiExcepcion{
        Optional<Articulo> articuloOpt = articuloRepositorio.findById(id);
        if(articuloOpt.isPresent()){
            articuloRepositorio.delete(articuloOpt.get());
        } else {
            throw new MiExcepcion("El artículo con Id especificado no existe");
        }
    }


    private void validar(String nombre,String descripcionArticulo,int cantidad, Long fabricaId) throws MiExcepcion {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new MiExcepcion("El nombre no puede ser nulo o estar vacío");
        }

        if ( cantidad == 0|| cantidad < 0){
            throw new MiExcepcion("La cantidad de artículos no puede ser menor o igual a cero");
        }

        if (descripcionArticulo.isEmpty() || descripcionArticulo == null){
            throw new MiExcepcion("La descripcion del artículo no puede estar vacía");
        }

        if ( fabricaId == null){
            throw new MiExcepcion("El id de la fábrica no puede ser nulo o estar vacío");
        }

    }


}
