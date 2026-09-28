package com.datapilot.server.ai.executor;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class QueryResultProcessor {
    /**
     * 单个字符串字段最多保留的字符数&整体长度
     */
    private static final int MAX_TEXT_LENGTH=2000;
    private static final int MAX_TOTAL_LENGTH=100_000;


    public List<Map<String,Object>> process(List<Map<String,Object>> rows){
        if(rows==null || rows.isEmpty()){
            return List.of();
        }

        List<Map<String,Object>> result=new ArrayList<>();

        int totalChar=0;

        for(Map<String,Object> row:rows){
            Map<String,Object> newRow=new LinkedHashMap<>();

            int rowChar=0;

            for(Map.Entry<String,Object> entry:row.entrySet()){
                Object value=entry.getValue();

                if(value instanceof String text){
                    if(text.length()>MAX_TEXT_LENGTH){
                        text=text.substring(0,MAX_TEXT_LENGTH)+"...";
                    }
                    value=text;
                }

                /*
                 *转成字符串后估算当前值的大小
                 */
                if(value!=null){
                    rowChar+=value.toString().length();
                }
                newRow.put(
                        entry.getKey(), value
                );
                }

            /*
            *加上当前行后超过整体行数限制，就不再继续加入结果集
            */
            if(rowChar+totalChar>MAX_TOTAL_LENGTH){
                break;
            }

            result.add(newRow);

            totalChar+=rowChar;
        }
        return result;
    }
}
