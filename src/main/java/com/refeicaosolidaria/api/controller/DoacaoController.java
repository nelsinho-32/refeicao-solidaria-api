package com.refeicaosolidaria.api.controller;

import com.refeicaosolidaria.api.domain.Doacao;
import com.refeicaosolidaria.api.domain.StatusDoacao;
import com.refeicaosolidaria.api.service.DoacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doacoes")
@RequiredArgsConstructor
public class DoacaoController {

    private final DoacaoService doacaoService;

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Doacao doacao, UriComponentsBuilder uriBuilder) {
        try {
            Doacao novaDoacao = doacaoService.salvar(doacao);
            URI uri = uriBuilder.path("/api/doacoes/{id}").buildAndExpand(novaDoacao.getId()).toUri();
            return ResponseEntity.created(uri).body(novaDoacao);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Doacao>> listar(
            @RequestParam(required = false) StatusDoacao status,
            @RequestParam(required = false) Long idDoador) {
        if (status != null) {
            return ResponseEntity.ok(doacaoService.listarPorStatus(status));
        }
        if (idDoador != null) {
            return ResponseEntity.ok(doacaoService.listarPorDoador(idDoador));
        }
        return ResponseEntity.ok(doacaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            Doacao doacao = doacaoService.buscarPorId(id);
            return ResponseEntity.ok(doacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Doacao doacao) {
        try {
            doacaoService.buscarPorId(id); // Garante existência prévia
            doacao.setId(id);
            Doacao doacaoAtualizada = doacaoService.salvar(doacao);
            return ResponseEntity.ok(doacaoAtualizada);
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
            Doacao doacaoCancelada = doacaoService.cancelarDoacao(id, motivo);
            return ResponseEntity.ok(doacaoCancelada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}

