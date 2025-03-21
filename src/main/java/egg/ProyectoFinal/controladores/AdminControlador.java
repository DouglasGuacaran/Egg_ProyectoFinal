package egg.ProyectoFinal.controladores;

import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.entidades.Usuario;
import egg.ProyectoFinal.enumeraciones.Rol;
import egg.ProyectoFinal.repositorios.UsuarioRepositorio;
import egg.ProyectoFinal.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @GetMapping("/dashboard")
    public String panel(Model model) {
        model.addAttribute("usuarios",usuarioServicio.listarUsuarios());
        return "panel.html"; // Retorna la vista panel.html en templates/
    }


    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable("id") UUID id, Model model) {
        Optional<Usuario> usuarioOpt = usuarioServicio.obtenerUsuarioPorId(id);
        if (usuarioOpt.isPresent()) {
            model.addAttribute("usuario", usuarioOpt.get());
            return "editarusuario.html"; // Redirige al formulario de edición
        } else {
            return "error/usuarioNoEncontrado"; // Página de error si no se encuentra el usuario
        }
    }


    @PostMapping("/editar")
    public String editarUsuario(@RequestParam("id") UUID id,
                                @RequestParam("nombre") String nombre,
                                @RequestParam("apellido") String apellido,
                                @RequestParam("email") String email,
                                @RequestParam("rol") String rol) {
        try {
            Rol rolEnum = Rol.valueOf(rol.toUpperCase()); // Convertir String a Enum
            Usuario usuario = usuarioServicio.modificarUsuario(id, nombre, apellido, email, rolEnum);
            return "redirect:../admin/dashboard"; // Redirige a la lista de usuarios
        } catch (MiExcepcion ex) {

            return "error/usuarioNoEncontrado"; // Página de error
        }
    }

    // Obtener el usuario autenticado
    private Usuario getUsuarioAutenticado() {
        // Obtener el objeto Usuario de Spring Security
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Buscar el usuario en la base de datos por su email
        return usuarioRepositorio.buscarPorEmail(principal.getUsername());
    }

    @PostMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable UUID id, RedirectAttributes redirectAttributes, Model model) {
        try {
            // Verificar si el usuario autenticado es un administrador
            Usuario usuarioAutenticado = getUsuarioAutenticado();
            if (!usuarioAutenticado.getRol().equals(Rol.ADMIN)) {
                redirectAttributes.addFlashAttribute("error", "Solo los administradores pueden eliminar usuarios.");
                return "redirect:/admin/dashboard";
            }

            // Verificar si el administrador intenta eliminarse a sí mismo
            if (usuarioAutenticado.getId().equals(id)) {
                redirectAttributes.addAttribute("error", "No puedes eliminar tu propio usuario.");
                return "redirect:/admin/dashboard";
            }

            // Eliminar usuario por su ID
            usuarioServicio.eliminarUsuario(id);
            model.addAttribute("exito", "Usuario eliminado exitosamente.");
            return "redirect:/admin/dashboard";

        } catch (Exception e) {
            // Manejo de excepciones genéricas
            model.addAttribute("error", "Ocurrió un error al intentar eliminar el usuario.");
            return "redirect:/admin/dashboard";
        }
    }

}
