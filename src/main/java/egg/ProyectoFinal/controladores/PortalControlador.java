package egg.ProyectoFinal.controladores;

import egg.ProyectoFinal.entidades.Usuario;
import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.servicios.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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

}
