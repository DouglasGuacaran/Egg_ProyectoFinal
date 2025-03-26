package egg.ProyectoFinal.controladores;

import egg.ProyectoFinal.entidades.Usuario;
import egg.ProyectoFinal.enumeraciones.Rol;
import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.servicios.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/")

public class PortalControlador {

    @Autowired
    UsuarioServicio usuarioServicio;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/")
    public String index(Model model, HttpSession session){
        Usuario logueado = (Usuario) session.getAttribute("usuariosession");
        model.addAttribute("usuario", logueado);

        return "index.html";
    }

    @GetMapping("/registrar")
    public String registrar(){
        return "registro.html";
    }

    @PostMapping("/registrar")
    public String registro(@RequestParam String nombre, @RequestParam String apellido, @RequestParam String email, @RequestParam String password, @RequestParam String password2, @RequestParam MultipartFile archivo, ModelMap model) throws MiExcepcion {
        try {
            usuarioServicio.registrar(archivo, nombre, apellido, email, password, password2);
            model.put("exito","Usuario registrado correctamente");
            return "registro.html";
        }catch (MiExcepcion ex){
            model.put("error", ex.getMessage());
            model.put("nombre",nombre);
            model.put("apellido",apellido);
            model.put("email",email);
            return "registro.html";
        }

    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, ModelMap modelo ) {
        if (error != null) {
            modelo.put("error", "Usuario o Contraseña inválidos!");        }
        return "login.html";
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/inicio")
    public String inicio(HttpSession session) {
        Usuario logueado = (Usuario) session.getAttribute("usuariosession");
        if (logueado.getRol().toString().equals("ADMIN")) {
            return "inicio.html";
        }
        logueado.getRol().toString().equals("USER");
        return "inicio.html";

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
                    return "redirect:../inicio";
                }
            }

            // Si no intenta cambiar el rol, se permite modificar otros datos
            usuarioServicio.modificarUsuario(archivo, id, nombre, apellido, email, nuevoRol);
            redirectAttributes.addFlashAttribute("exito", "Usuario editado correctamente.");
            return "redirect:../inicio";

        } catch (MiExcepcion ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:../inicio";
        }
    }

}
