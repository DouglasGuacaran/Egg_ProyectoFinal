package egg.ProyectoFinal.controladores;

import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.entidades.Usuario;
import egg.ProyectoFinal.enumeraciones.Rol;
import egg.ProyectoFinal.repositorios.UsuarioRepositorio;
import egg.ProyectoFinal.servicios.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @GetMapping("/panel")
    public String panel(Model model) {
        model.addAttribute("usuarios",usuarioServicio.listarUsuarios());
        return "panel.html"; // Retorna la vista panel.html en templates/
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable("id") UUID id, Model model) {
        Optional<Usuario> usuarioOpt = usuarioServicio.obtenerUsuarioPorId(id);
        if (usuarioOpt.isPresent()) {
            model.addAttribute("usuario", usuarioOpt.get());
            return "editar_usuario.html"; // Redirige al formulario de edición
        } else {
            return "error/usuarioNoEncontrado"; // Página de error si no se encuentra el usuario
        }
    }


    @PostMapping("/editar")
    public String editarUsuario(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("id") UUID id,
            @RequestParam("nombre") String nombre,
            @RequestParam("apellido") String apellido,
            @RequestParam("email") String email,
            @RequestParam("rol") String rol,
            Principal principal, // Obtiene el usuario autenticado
            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuarioAutenticado = usuarioServicio.buscarPorEmail(principal.getName());
            Usuario usuarioAEditar = usuarioServicio.buscarPorId(id); // Obtener el usuario que se va a editar

            Rol nuevoRol = Rol.valueOf(rol.toUpperCase());

            // Verificar si el usuario autenticado es ADMIN y está tratando de cambiar su propio rol
            if (usuarioAutenticado.getId().equals(usuarioAEditar.getId()) && usuarioAutenticado.getRol() == Rol.ADMIN) {
                if (!usuarioAutenticado.getRol().equals(nuevoRol)) { // Solo bloquear si intenta cambiar el rol
                    redirectAttributes.addFlashAttribute("error", "No puedes cambiar tu propio rol.");
                    return "redirect:/admin/panel";
                }
            }

            // Si no intenta cambiar el rol, se permite modificar otros datos
            usuarioServicio.modificarUsuario(archivo, id, nombre, apellido, email, nuevoRol);
            redirectAttributes.addFlashAttribute("exito", "Usuario editado correctamente.");
            return "redirect:/admin/panel";

        } catch (MiExcepcion ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/panel";
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
                return "redirect:/admin/panel";
            }

            // Verificar si el administrador intenta eliminarse a sí mismo
            if (usuarioAutenticado.getId().equals(id)) {
                redirectAttributes.addAttribute("error", "No puedes eliminar tu propio usuario.");
                return "redirect:/admin/panel";
            }

            // Eliminar usuario por su ID
            usuarioServicio.eliminarUsuario(id);
            model.addAttribute("exito", "Usuario eliminado exitosamente.");
            return "redirect:/admin/panel";

        } catch (Exception e) {
            // Manejo de excepciones genéricas
            model.addAttribute("error", "Ocurrió un error al intentar eliminar el usuario.");
            return "redirect:/admin/panel";
        }
    }

}
