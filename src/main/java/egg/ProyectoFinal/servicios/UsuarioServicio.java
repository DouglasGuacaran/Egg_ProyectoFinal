package egg.ProyectoFinal.servicios;

import egg.ProyectoFinal.entidades.Usuario;
import egg.ProyectoFinal.enumeraciones.Rol;
import egg.ProyectoFinal.excepciones.MiExcepcion;
import egg.ProyectoFinal.repositorios.UsuarioRepositorio;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UsuarioServicio implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Transactional
    public void registrar(String nombre, String apellido, String email, String password, String password2) throws MiExcepcion {

        validar(nombre, apellido, email, password, password2);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword(new BCryptPasswordEncoder().encode(password));
        usuario.setRol(Rol.USER);

        usuarioRepositorio.save(usuario);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepositorio.buscarPorEmail(email);

        if (usuario != null) {

            List<GrantedAuthority> permisos = new ArrayList<>();

            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_"+ usuario.getRol().toString());

            permisos.add(p);

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

            HttpSession session = attr.getRequest().getSession(true);

            session.setAttribute("usuariosession", usuario);

            return new User( usuario.getEmail(), usuario.getPassword(), permisos);
        }else{
            return null;
        }
    }

    private void validar(String nombre, String apellido, String email, String password, String password2) throws MiExcepcion {

        if (nombre.isEmpty() || nombre == null) {
            throw new MiExcepcion("el nombre no puede ser nulo o estar vacío");
        }
        if (apellido.isEmpty() || apellido == null) {
            throw new MiExcepcion("el apellido no puede ser nulo o estar vacío");
        }
        if (email.isEmpty() || email == null) {
            throw new MiExcepcion("el email no puede ser nulo o estar vacío");
        }
        if (password.isEmpty() || password == null || password.length() <= 5) {
            throw new MiExcepcion("La contraseña no puede estar vacía, y debe tener más de 5 dígitos");
        }
        if (!password.equals(password2)) {
            throw new MiExcepcion("Las contraseñas ingresadas deben ser iguales");
        }

    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios = usuarioRepositorio.findAll();
        return usuarios;
    }

    @Transactional
    public void cambiarRol(UUID id, String rol) {
        Usuario usuario = usuarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setRol(Rol.valueOf(rol.toUpperCase()));
        usuarioRepositorio.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(UUID id) {
        Usuario usuario = usuarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioRepositorio.delete(usuario); // Elimina el usuario de la base de datos
    }

    public boolean esAdmin(String email) {
        Usuario usuario = usuarioRepositorio.buscarPorEmail(email);
        return usuario != null && usuario.getRol() == Rol.ADMIN;
    }

    public boolean esElMismoUsuario(UUID id, String email) {
        Usuario usuario = usuarioRepositorio.findById(id).orElse(null);
        return usuario != null && usuario.getEmail().equals(email);
    }

}
