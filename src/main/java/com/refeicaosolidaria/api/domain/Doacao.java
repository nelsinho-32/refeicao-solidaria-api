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
@Table(name = "doacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "doador")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doador", referencedColumnName = "id")
    private Usuario doador;

    @Column(name = "nome_alimento", nullable = false)
    private String nomeAlimento;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "quantidade_disponivel", nullable = false)
    private Integer quantidadeDisponivel;

    @Column(name = "data_hora_preparo")
    private LocalDateTime dataHoraPreparo;

    @Column(name = "validade_consumo", nullable = false)
    private LocalDateTime validadeConsumo;

    @Column(name = "armazenamento_conservacao", nullable = false, columnDefinition = "TEXT")
    private String armazenamentoConservacao;

    @Column(name = "declaracao_doador_aceite", nullable = false)
    private Boolean declaracaoDadorAceite;

    @Column(name = "local_retirada", nullable = false, columnDefinition = "TEXT")
    private String localRetirada;

    @Column(name = "horario_limite_retirada", nullable = false)
    private LocalDateTime horarioLimiteRetirada;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "status_doacao default 'DISPONIVEL'")
    private StatusDoacao status;

    @Column(name = "data_criacao", insertable = false, updatable = false)
    private LocalDateTime dataCriacao;
}
