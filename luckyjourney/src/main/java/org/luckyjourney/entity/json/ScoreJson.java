package org.luckyjourney.entity.json;

import lombok.Data;



@Data
public class ScoreJson{
    Double minTerror;
    Double maxTerror;
    Double minPolitician;
    Double maxPolitician;
    Double minPulp;
    Double maxPulp;

    // 当前审核规则
    Integer auditStatus;

}