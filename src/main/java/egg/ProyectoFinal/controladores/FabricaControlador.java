package egg.ProyectoFinal.controladores;

import egg.ProyectoFinal.entidades.Fabrica;
import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.servicios.FabricaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/fabrica")
public class FabricaControlador {

    @Autowired
    FabricaServicio fabricaServicio;

    @GetMapping("/registrar")
    public String registrar (Model model) {
        List<Fabrica> fabricas = fabricaServicio.listarFabricas();
        model.addAttribute("fabricas", fabricas);
        return "fabrica_form.html";
    }

    @GetMapping ("/lista")
    public String listar( Model model){

        model.addAttribute("fabricas",fabricaServicio.listarFabricas());
        return "fabrica_lista.html";
    }

    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable Long id, Model model) {
        Fabrica fabrica = fabricaServicio.getOne(id);

        model.addAttribute("fabrica", fabrica);

        return "fabrica_modificar.html";
    }

    @PostMapping("/registro")
    public String registro(@RequestParam String nombreFabrica, Model modelo) throws Exception  {
        try {
            fabricaServicio.crearFabrica(nombreFabrica);
            modelo.addAttribute("exito","La fábrica fue creada correctamente");

        } catch (Exception ex) {
            modelo.addAttribute("error", ex.getMessage());
            return "fabrica_form.html";
        }
        return "fabrica_form.html";
    }

    @PostMapping("/modificar/{id}")
    public String modificar(@PathVariable Long id, @RequestParam String nombreFabrica, Model model) {
        try {
            fabricaServicio.modificarFabrica( id, nombreFabrica);
            model.addAttribute("exito","La fábrica se ha editado correctamente");

            return "redirect:../lista";
        }catch (MiExcepcion ex){
            model.addAttribute("error",ex.getMessage());
            return "fabrica_modificar.html";
        }
    }
}
