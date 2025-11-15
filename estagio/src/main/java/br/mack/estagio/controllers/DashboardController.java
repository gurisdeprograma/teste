// Controller para dashboards personalizados.
// Fornece estatísticas para admin, vagas para estudantes e inscrições para empresas.
package br.mack.estagio.controllers;

import br.mack.estagio.entities.*;
import br.mack.estagio.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private EstudanteRepository estudanteRepository;

    @Autowired
    private VagaEstagioRepository vagaEstagioRepository;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    // Retorna estatísticas gerais do portal para administradores. Requer role ADMIN.
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmpresas", empresaRepository.count());
        stats.put("totalEstudantes", estudanteRepository.count());
        stats.put("vagasAbertas", vagaEstagioRepository.countByAbertaTrue());
        stats.put("vagasEncerradas", vagaEstagioRepository.countByAbertaFalse());
        stats.put("vagasPorArea", vagaEstagioRepository.countVagasByArea());
        return stats;
    }

    // Retorna vagas abertas relacionadas às áreas de interesse do estudante.
    @GetMapping("/estudante/{estudanteId}")
    public List<VagaEstagio> getEstudanteDashboard(@PathVariable Long estudanteId) {
        return estudanteRepository.findById(estudanteId)
                .map(estudante -> vagaEstagioRepository.findByAreaInAndAbertaTrueOrderByIdDesc(List.copyOf(estudante.getAreasInteresse())))
                .orElse(List.of());
    }

    // Retorna vagas da empresa e inscrições por vaga para a empresa logada.
    @GetMapping("/empresa/{empresaId}")
    public Map<String, Object> getEmpresaDashboard(@PathVariable Long empresaId) {
        Map<String, Object> dashboard = new HashMap<>();
        List<VagaEstagio> vagas = vagaEstagioRepository.findByEmpresa_Id(empresaId);
        dashboard.put("vagas", vagas);
        Map<Long, List<Inscricao>> inscricoesPorVaga = new HashMap<>();
        for (VagaEstagio vaga : vagas) {
            List<Inscricao> inscricoes = inscricaoRepository.findByVagaId(vaga.getId());
            inscricoesPorVaga.put(vaga.getId(), inscricoes);
        }
        dashboard.put("inscricoesPorVaga", inscricoesPorVaga);
        return dashboard;
    }
}
