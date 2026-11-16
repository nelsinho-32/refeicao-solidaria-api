package com.refeicaosolidaria.api.service;

import com.refeicaosolidaria.api.domain.Doacao;
import com.refeicaosolidaria.api.domain.Solicitacao;
import com.refeicaosolidaria.api.domain.StatusDoacao;
import com.refeicaosolidaria.api.domain.StatusSolicitacao;
import com.refeicaosolidaria.api.repository.DoacaoRepository;
import com.refeicaosolidaria.api.repository.SolicitacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final SolicitacaoRepository solicitacaoRepository;

    @Transactional
    public Doacao salvar(Doacao doacao) {
        if (doacao.getValidadeConsumo() == null || doacao.getValidadeConsumo().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data de validade de consumo deve ser futura.");
        }

        if (doacao.getHorarioLimiteRetirada() != null && doacao.getHorarioLimiteRetirada().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("O horário limite para retirada deve ser posterior ao momento atual.");
        }

        if (doacao.getStatus() == null) {
            doacao.setStatus(StatusDoacao.DISPONIVEL);
        }

        return doacaoRepository.save(doacao);
    }

    @Transactional(readOnly = true)
    public List<Doacao> listarTodas() {
        return doacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Doacao> listarPorStatus(StatusDoacao status) {
        return doacaoRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Doacao> listarPorDoador(Long idDoador) {
        return doacaoRepository.findByDoadorId(idDoador);
    }

    @Transactional(readOnly = true)
    public Doacao buscarPorId(Long id) {
        return doacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada com o ID: " + id));
    }

    @Transactional
    public Doacao cancelarDoacao(Long idDoacao, String motivoCancelamento) {
        if (motivoCancelamento == null || motivoCancelamento.trim().isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar o motivo do cancelamento.");
        }

        Doacao doacao = buscarPorId(idDoacao);

        // Regra: O doador só pode cancelar se status for diferente de AGUARDANDO_RETIRADA ou CONCLUIDA
        if (doacao.getStatus() == StatusDoacao.AGUARDANDO_RETIRADA || doacao.getStatus() == StatusDoacao.CONCLUIDA) {
            throw new IllegalStateException("Não é possível cancelar uma doação com status " + doacao.getStatus() + ".");
        }

        doacao.setStatus(StatusDoacao.CANCELADA);

        // Atualizar e registrar o motivo do cancelamento em todas as solicitações associadas ainda abertas/pendentes/aprovadas
        List<Solicitacao> solicitacoes = solicitacaoRepository.findByDoacaoId(idDoacao);
        for (Solicitacao solicitacao : solicitacoes) {
            if (solicitacao.getStatus() == StatusSolicitacao.PENDENTE || solicitacao.getStatus() == StatusSolicitacao.APROVADA) {
                solicitacao.setStatus(StatusSolicitacao.CANCELADA);
                solicitacao.setMotivoCancelamento(motivoCancelamento);
                solicitacaoRepository.save(solicitacao);
            }
        }

        return doacaoRepository.save(doacao);
    }
}

