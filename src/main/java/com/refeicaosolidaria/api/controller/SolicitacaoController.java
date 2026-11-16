package com.refeicaosolidaria.api.controller;

import com.refeicaosolidaria.api.domain.Solicitacao;
import com.refeicaosolidaria.api.service.SolicitacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solicitacoes")
@RequiredArgsConstructor
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Solicitacao solicitacao, UriComponentsBuilder uriBuilder) {
        try {
            Solicitacao novaSolicitacao = solicitacaoService.salvar(solicitacao);
            URI uri = uriBuilder.path("/api/solicitacoes/{id}").buildAndExpand(novaSolicitacao.getId()).toUri();
            return ResponseEntity.created(uri).body(novaSolicitacao);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Solicitacao>> listar(
            @RequestParam(required = false) Long idInstituicao,
            @RequestParam(required = false) Long idDoacao) {
        if (idInstituicao != null) {
            return ResponseEntity.ok(solicitacaoService.listarPorInstituicao(idInstituicao));
        }
        if (idDoacao != null) {
            return ResponseEntity.ok(solicitacaoService.listarPorDoacao(idDoacao));
        }
        return ResponseEntity.ok(solicitacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Solicitacao solicitacao = solicitacaoService.buscarPorId(id);
            return ResponseEntity.ok(solicitacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<?> aprovar(@PathVariable Long id) {
        try {
            Solicitacao solicitacaoAprovada = solicitacaoService.aprovarSolicitacao(id);
            return ResponseEntity.ok(solicitacaoAprovada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<?> recusar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String motivo = body != null ? body.get("motivo") : null;
            Solicitacao solicitacaoRecusada = solicitacaoService.recusarSolicitacao(id, motivo);
            return ResponseEntity.ok(solicitacaoRecusada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelar(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        try {
            String motivo = body != null ? body.get("motivo") : null;
            if (motivo == null || motivo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "O campo 'motivo' é obrigatório para cancelamento."));
            }
            Solicitacao solicitacaoCancelada = solicitacaoService.cancelarSolicitacao(id, motivo);
            return ResponseEntity.ok(solicitacaoCancelada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}

