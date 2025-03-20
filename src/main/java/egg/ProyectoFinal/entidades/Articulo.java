package egg.ProyectoFinal.entidades;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;

import java.util.concurrent.atomic.AtomicInteger;


@Entity
@Data
@NoArgsConstructor

public class Articulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer nroArticulo;

    @Column(nullable = false)
    private String nombreArticulo;

    private String descripcionArticulo;

    private int cantidad;

    @ManyToOne
    @JoinColumn(name = "fabrica_id")
    private Fabrica fabrica;

}
