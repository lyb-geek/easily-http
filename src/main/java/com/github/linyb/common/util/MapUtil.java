package com.github.linyb.common.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapUtil{
    private static Logger logger = LoggerFactory.getLogger(MapUtil.class);

    /**
     * key首字母转大写
     * @param map
     * @return
     */
    public static Map<String,String> getCapwordsKeyMap(Map<String,String> map){
          Map<String,String> capWordsKeyMap = new HashMap<>();
          if(!map.isEmpty()){
              map.forEach((key,value)->{
                  String capwordsKey = StringUtil.capitalize(key);
                  if(StringUtil.isNotBlank(value)){
                      capWordsKeyMap.put(capwordsKey,value);
                  }



              });
          }

          return capWordsKeyMap;

    }


    public static <T> Map<String,String> getCapwordsKeyMap(T t){
        Map<String,String> params = new HashMap<>();

        try {
           params = BeanUtils.describe(t);
           params.remove("class");
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        return getCapwordsKeyMap(params);

    }


    public static <T> Map<String,String> convertBean2Map(T t){
        Map<String,String> newParams = new HashMap<>();

        try {
            Map<String,String>  params = BeanUtils.describe(t);
            params.remove("class");
            params.forEach((key,value)->{
                if(StringUtil.isNotBlank(value)){
                    newParams.put(key,value);
                }
            });

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

        return newParams;

    }
}
