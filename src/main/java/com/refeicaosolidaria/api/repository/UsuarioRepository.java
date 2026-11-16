package com.refeicaosolidaria.api.repository;

import com.refeicaosolidaria.api.domain.TipoUsuario;
import com.refeicaosolidaria.api.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByDocumento(String documento);

    boolean existsByEmail(String email);

    boolean existsByDocumento(String documento);

    List<Usuario> findByTipo(TipoUsuario tipo);

    List<Usuario> findByTipoAndAprovado(TipoUsuario tipo, Boolean aprovado);
}

