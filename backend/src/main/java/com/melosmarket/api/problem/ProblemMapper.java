package com.melosmarket.api.problem;

import com.melosmarket.api.generated.model.CreateProblemRequest;
import com.melosmarket.api.generated.model.County;
import com.melosmarket.api.generated.model.Problem;
import com.melosmarket.api.generated.model.ProblemImage;
import com.melosmarket.api.generated.model.ProblemStatus;
import com.melosmarket.api.generated.model.Trade;
import com.melosmarket.api.generated.model.UpdateProblemRequest;
import com.melosmarket.api.problem.domain.ProblemStatusEntity;
import com.melosmarket.api.problem.domain.TradeType;
import com.melosmarket.api.problem.persistence.ProblemEntity;
import com.melosmarket.api.problem.persistence.ProblemImageEntity;

import org.springframework.stereotype.Component;

@Component
class ProblemMapper {

    ProblemEntity toEntity(CreateProblemRequest request) {
        ProblemEntity entity = new ProblemEntity();
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setPhone(blankToNull(request.getPhone()));
        entity.setTrade(toEntityTrade(request.getTrade()));
        entity.setLocation(blankToNull(request.getLocation()));
        entity.setCounty(toEntityCounty(request.getCounty()));
        entity.setStatus(ProblemStatusEntity.OPEN);
        return entity;
    }

    void updateEntity(ProblemEntity entity, UpdateProblemRequest request) {
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setPhone(blankToNull(request.getPhone()));
        entity.setTrade(toEntityTrade(request.getTrade()));
        entity.setLocation(blankToNull(request.getLocation()));
        entity.setCounty(toEntityCounty(request.getCounty()));
    }

    Problem toApi(ProblemEntity entity) {
        return new Problem(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                toApiStatus(entity.getStatus()),
                entity.getCreatedAt())
                .customerId(entity.getCustomerId())
                .phone(entity.getPhone())
                .trade(toApiTrade(entity.getTrade()))
                .location(entity.getLocation())
                .county(toApiCounty(entity.getCounty()))
                .problemImages(entity.getProblemImages().stream()
                        .map(this::toApiProblemImage)
                        .toList());
    }

    TradeType toEntityTrade(Trade trade) {
        return trade == null ? null : TradeType.valueOf(trade.name());
    }

    String toEntityCounty(County county) {
        return county == null ? null : county.getValue();
    }

    ProblemStatusEntity toEntityStatus(ProblemStatus status) {
        return status == null ? null : ProblemStatusEntity.valueOf(status.name());
    }

    private Trade toApiTrade(TradeType trade) {
        return trade == null ? null : Trade.valueOf(trade.name());
    }

    private County toApiCounty(String county) {
        return county == null ? null : County.fromValue(county);
    }

    private ProblemStatus toApiStatus(ProblemStatusEntity status) {
        return ProblemStatus.valueOf(status.name());
    }

    ProblemImage toApiProblemImage(ProblemImageEntity entity) {
        return new ProblemImage(
                entity.getId(),
                entity.getTitle(),
                entity.getImageUrl(),
                entity.getCreatedAt());
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
