package com.refeicaosolidaria.api.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"doacao", "instituicao"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doacao", referencedColumnName = "id")
    private Doacao doacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_instituicao", referencedColumnName = "id")
    private Usuario instituicao;

    @Column(name = "quantidade_solicitada", nullable = false)
    private Integer quantidadeSolicitada;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "status_solicitacao default 'PENDENTE'")
    private StatusSolicitacao status;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "data_solicitacao", insertable = false, updatable = false)
    private LocalDateTime dataSolicitacao;
}
