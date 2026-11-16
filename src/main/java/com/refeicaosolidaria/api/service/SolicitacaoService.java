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

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final DoacaoRepository doacaoRepository;

    @Transactional
    public Solicitacao salvar(Solicitacao solicitacao) {
        Integer idDoacao = solicitacao.getDoacao() != null ? solicitacao.getDoacao().getId() : null;
        if (idDoacao == null) {
            throw new IllegalArgumentException("A solicitação deve estar vinculada a uma doação válida.");
        }

        Doacao doacao = doacaoRepository.findById(idDoacao)
                .orElseThrow(() -> new IllegalArgumentException("Doação não encontrada com o ID: " + idDoacao));

        if (solicitacao.getQuantidadeSolicitada() == null || solicitacao.getQuantidadeSolicitada() <= 0) {
            throw new IllegalArgumentException("A quantidade solicitada deve ser maior que zero.");
        }

        // Regra: Uma instituição não pode solicitar uma quantidade maior do que a quantidade_disponivel da doação
        if (solicitacao.getQuantidadeSolicitada() > doacao.getQuantidadeDisponivel()) {
            throw new IllegalArgumentException(
                    "Quantidade solicitada (" + solicitacao.getQuantidadeSolicitada()
                            + ") é maior que a quantidade disponível (" + doacao.getQuantidadeDisponivel() + ")."
            );
        }

        if (doacao.getStatus() != StatusDoacao.DISPONIVEL && doacao.getStatus() != StatusDoacao.PARCIALMENTE_RESERVADA) {
            throw new IllegalStateException("Esta doação não está disponível para receber solicitações.");
        }

        solicitacao.setDoacao(doacao);
        if (solicitacao.getStatus() == null) {
            solicitacao.setStatus(StatusSolicitacao.PENDENTE);
        }

        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional(readOnly = true)
    public List<Solicitacao> listarTodas() {
        return solicitacaoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Solicitacao buscarPorId(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Solicitacao> listarPorInstituicao(Long idInstituicao) {
        return solicitacaoRepository.findByInstituicaoId(idInstituicao);
    }

    @Transactional(readOnly = true)
    public List<Solicitacao> listarPorDoacao(Long idDoacao) {
        return solicitacaoRepository.findByDoacaoId(idDoacao);
    }

    @Transactional
    public Solicitacao aprovarSolicitacao(Long idSolicitacao) {
        Solicitacao solicitacao = buscarPorId(idSolicitacao);

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Apenas solicitações com status PENDENTE podem ser aprovadas.");
        }

        Doacao doacao = solicitacao.getDoacao();

        if (solicitacao.getQuantidadeSolicitada() > doacao.getQuantidadeDisponivel()) {
            throw new IllegalStateException(
                    "Não há estoque suficiente disponível (" + doacao.getQuantidadeDisponivel()
                            + ") para aprovar a solicitação de " + solicitacao.getQuantidadeSolicitada() + "."
            );
        }

        // Abate a quantidade disponível
        int novaQuantidadeDisponivel = doacao.getQuantidadeDisponivel() - solicitacao.getQuantidadeSolicitada();
        doacao.setQuantidadeDisponivel(novaQuantidadeDisponivel);

        // Regra de Automação de Status:
        // Se a quantidade_disponivel da doação chegar a zero, status da doação vai para TOTALMENTE_RESERVADA.
        // Se ainda sobrar estoque, atualiza para PARCIALMENTE_RESERVADA.
        if (novaQuantidadeDisponivel == 0) {
            doacao.setStatus(StatusDoacao.TOTALMENTE_RESERVADA);
        } else {
            doacao.setStatus(StatusDoacao.PARCIALMENTE_RESERVADA);
        }

        doacaoRepository.save(doacao);

        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public Solicitacao recusarSolicitacao(Long idSolicitacao, String motivoRecusa) {
        Solicitacao solicitacao = buscarPorId(idSolicitacao);

        if (solicitacao.getStatus() != StatusSolicitacao.PENDENTE) {
            throw new IllegalStateException("Apenas solicitações com status PENDENTE podem ser recusadas.");
        }

        solicitacao.setStatus(StatusSolicitacao.RECUSADA);
        solicitacao.setMotivoCancelamento(motivoRecusa);
        return solicitacaoRepository.save(solicitacao);
    }

    @Transactional
    public Solicitacao cancelarSolicitacao(Long idSolicitacao, String motivoCancelamento) {
        if (motivoCancelamento == null || motivoCancelamento.trim().isEmpty()) {
            throw new IllegalArgumentException("É obrigatório informar o motivo do cancelamento.");
        }

        Solicitacao solicitacao = buscarPorId(idSolicitacao);

        if (solicitacao.getStatus() == StatusSolicitacao.CANCELADA || solicitacao.getStatus() == StatusSolicitacao.RECUSADA) {
            throw new IllegalStateException("Esta solicitação já se encontra finalizada/cancelada.");
        }

        // Se a solicitação já estava aprovada, devolve a quantidade para a doação
        if (solicitacao.getStatus() == StatusSolicitacao.APROVADA) {
            Doacao doacao = solicitacao.getDoacao();
            int novaQuantidade = doacao.getQuantidadeDisponivel() + solicitacao.getQuantidadeSolicitada();
            doacao.setQuantidadeDisponivel(novaQuantidade);

            if (doacao.getStatus() == StatusDoacao.TOTALMENTE_RESERVADA) {
                doacao.setStatus(StatusDoacao.PARCIALMENTE_RESERVADA);
            }
            doacaoRepository.save(doacao);
        }

        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        solicitacao.setMotivoCancelamento(motivoCancelamento);

        return solicitacaoRepository.save(solicitacao);
    }
}

