package com.example.kuwalog.service;

import com.example.kuwalog.entity.Beetle;
import com.example.kuwalog.entity.enums.Species;
import com.example.kuwalog.repository.BeetleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingService {

    private final BeetleRepository beetleRepository;

    public RankingService(BeetleRepository beetleRepository) {
        this.beetleRepository = beetleRepository;
    }

    @Transactional(readOnly = true)
    public Map<Species, List<Beetle>> getRankings() {
        Map<Species, List<Beetle>> rankings = new LinkedHashMap<>();
        for (Species species : Species.rankingTargets()) {
            List<Beetle> top = beetleRepository.findRankingBySpecies(
                    species.getLabel(), PageRequest.of(0, 10));
            rankings.put(species, top);
        }
        return rankings;
    }

    @Transactional(readOnly = true)
    public List<Beetle> getTop3() {
        List<String> labels = Species.rankingTargets().stream()
                .map(Species::getLabel).toList();
        return beetleRepository.findRankingTop(labels, PageRequest.of(0, 3));
    }
}
