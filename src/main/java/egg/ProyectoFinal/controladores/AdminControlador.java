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

    @PostMapping("/cambiarRol")
    public String cambiarRol(@RequestParam UUID id, @RequestParam String nuevoRol, RedirectAttributes redirectAttributes) {
        String loggedInUserEmail = ((Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail();

        if (usuarioServicio.esAdmin(loggedInUserEmail)) {

            if (usuarioServicio.esElMismoUsuario(id, loggedInUserEmail)) {
                redirectAttributes.addFlashAttribute("error", "No puedes modificar tu propio rol.");
                return "redirect:/admin/dashboard";
            }
            usuarioServicio.cambiarRol(id, nuevoRol); // Cambiar el rol si es admin y no intenta cambiar su propio rol
            redirectAttributes.addFlashAttribute("exito", "Rol cambiado exitosamente.");
            return "redirect:/admin/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
            return "redirect:/acceso-denegado";
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
