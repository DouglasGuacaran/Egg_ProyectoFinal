package egg.ProyectoFinal.controladores;

import egg.ProyectoFinal.entidades.Articulo;
import egg.ProyectoFinal.entidades.Fabrica;
import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.repositorios.ArticuloRepositorio;
import egg.ProyectoFinal.servicios.ArticuloServicio;
import egg.ProyectoFinal.servicios.FabricaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/articulo")
public class ArticuloControlador {
    @Autowired
    private ArticuloRepositorio articuloRepositorio;

    @Autowired
    FabricaServicio fabricaServicio;

    @Autowired
    ArticuloServicio articuloServicio;

    @GetMapping("/registrar")
    public String registrar (Model model){
        List<Fabrica> fabricas = fabricaServicio.listarFabricas();
        List<Articulo> articulos = articuloServicio.listarArticulos();

        model.addAttribute("fabricas", fabricas);
        model.addAttribute("articulos", articulos);

        return "articulo_form.html";
    }

    @PostMapping("/registro")
    public String registro(
                           @RequestParam String nombreArticulo,
                           @RequestParam String descripcionArticulo,
                           @RequestParam Long fabricaId,
                           @RequestParam int cantidad,Model modelo
                           ) throws MiExcepcion{
        articuloServicio.crearArticulo( nombreArticulo,descripcionArticulo,fabricaId,cantidad);
        modelo.addAttribute("exito","El artículo se ha creado correctamente");
        return "redirect:../articulo/lista";
    }



    @GetMapping ("/lista")
    public String lista( Model model){

        model.addAttribute("fabricas",fabricaServicio.listarFabricas());
        model.addAttribute("articulos",articuloServicio.listarArticulos());

        return "articulo_lista.html";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable Long id, Model model) {

        Articulo articulo = articuloServicio.getOne(id);
        if (articulo == null) {
            throw new RuntimeException("El artículo con ID " + id + " no existe.");
        }

        Fabrica fabrica = articulo.getFabrica();

        model.addAttribute("articulo",articuloServicio.getOne(id));
        model.addAttribute("fabricas", fabricaServicio.listarFabricas());

        if (fabrica != null) {
            model.addAttribute("fabricaSeleccionada", fabrica.getId());
        } else {
            model.addAttribute("fabricaSeleccionada", null);
        }
        return "articulo_modificar.html";
    }

    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable Long id, @RequestParam String nombreArticulo, @RequestParam Integer nroArticulo, @RequestParam String descripcionArticulo, @RequestParam int cantidad, @RequestParam Long fabricaId, Model model) {
        try {
            articuloServicio.modificarArticulo(id,nombreArticulo,nroArticulo,descripcionArticulo,cantidad,fabricaId);
            model.addAttribute("exito","El artículo ha sido modificado correctamente");
            return "redirect:../articulo/lista";
        }catch (MiExcepcion ex){
            model.addAttribute("error",ex.getMessage());
            return "articulo_modificar.html";
        }
    }
}
