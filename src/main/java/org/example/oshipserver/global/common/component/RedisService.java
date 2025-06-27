package org.example.oshipserver.global.common.component;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

//@Service
//@RequiredArgsConstructor
public class RedisService {
    /**
     * @Recover에서 사용할 redis 큐 적재,조회
     * 현재 단건/다건 결제에서는 사용하지 않지만, 추후 정기결제에서 사용하거나
     * 외부 api 실패 재처리를 위해 다시 사용할 수 있으므로 주석처리
     */

//    private final RedisTemplate<String, String> stringRedisTemplate;
//
//    /**
//     * Redis 리스트(큐)에 실패요청(JSON) 추가
//     * @param key Redis 리스트 키s
//     * @param jsonValue 직렬화된 요청 본문 (JSON)
//     */
//    public void pushToList(String key, String jsonValue) {
//        stringRedisTemplate.opsForList().rightPush(key, jsonValue);
//    }
//
//    /**
//     * Redis 리스트(큐)에서 가장 오래된 요청 꺼내기(재처리 시 사용)
//     * @param key Redis 리스트 키
//     * @return JSON 문자열 (직렬화된 요청), 없으면 null
//     */
//    public String popFromList(String key) {
//        return stringRedisTemplate.opsForList().leftPop(key);
//    }
//
//    /**
//     * 큐 길이 조회
//     */
//    public Long getQueueSize(String key) {
//        return stringRedisTemplate.opsForList().size(key);
//    }
}
