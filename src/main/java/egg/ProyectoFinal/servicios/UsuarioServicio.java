package egg.ProyectoFinal.servicios;

import egg.ProyectoFinal.entidades.Imagen;
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
import org.springframework.web.multipart.MultipartFile;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioServicio implements UserDetailsService {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private  ImagenServicio imagenServicio;

    @Transactional
    public void registrar(MultipartFile archivo, String nombre, String apellido, String email, String password, String password2) throws MiExcepcion {

        validar(nombre, apellido, email, password, password2);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setPassword(new BCryptPasswordEncoder().encode(password));
        usuario.setRol(Rol.USER);

        Imagen imagen = imagenServicio.guardar(archivo);

        usuario.setImagen(imagen);
        usuarioRepositorio.save(usuario);
    }

    public Usuario buscarPorEmail(String email) throws MiExcepcion {
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new MiExcepcion("Usuario no encontrado con email: " + email));
    }

    public Usuario buscarPorId(UUID id) throws MiExcepcion {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new MiExcepcion("Usuario no encontrado con id: " + id));
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
    public Usuario modificarUsuario(MultipartFile archivo, UUID id, String nombre, String apellido, String email, Rol rol) throws MiExcepcion {

        Optional<Usuario> usuarioOpt = Optional.ofNullable(usuarioRepositorio.buscarPorEmail(email));
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setEmail(email);
            usuario.setRol(rol);
            String idImagen = null;

            if (usuario.getImagen()!= null){
                idImagen = usuario.getImagen().getId();
            }

            Imagen imagen = imagenServicio.actualizar(archivo,idImagen);
            usuario.setImagen(imagen);
            return usuarioRepositorio.save(usuario); // Guarda los cambios
        } else {
            throw new MiExcepcion("Usuario no encontrado con ID: " + id);
        }
    }

    @Transactional
    public void eliminarUsuario(UUID id) {
        Usuario usuario = usuarioRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioRepositorio.delete(usuario); // Elimina el usuario de la base de datos
    }


    public Optional<Usuario> obtenerUsuarioPorId(UUID id) {
        return usuarioRepositorio.findById(id); // Cambié el tipo de id a UUID
    }
    @Transactional(readOnly = true)
    public Usuario getOne(UUID id) {
        return usuarioRepositorio.findById(id).orElse(null);
    }
}
