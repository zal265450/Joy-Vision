package org.luckyjourney.entity.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 02:13
 */
@Data
//@ToString
public class BodyJson implements Serializable {
    String id;
    String status;
    ResultJson result;


    public boolean compare(Double min, Double max, Double value) {
        return value >= min && value <= max;
    }

    public boolean checkViolation(List<CutsJson> types,Double min, Double max){
        for (CutsJson cutsJson : types) {
            if (!ObjectUtils.isEmpty(cutsJson.details)){
                for (DetailsJson detail : cutsJson.details) {
                   if (compare(min,max,detail.getScore())){
                       return true;
                   }
                }
            }
        }
        return false;
    }


    public List<CutsJson> getTerror(){
        return result.getResult().getScenes().getTerror().getCuts();
    }

    public List<CutsJson> getPolitician(){
        return result.getResult().getScenes().getPolitician().getCuts();
    }

    public List<CutsJson> getPulp(){
        return result.getResult().getScenes().getPulp().getCuts();
    }





}
