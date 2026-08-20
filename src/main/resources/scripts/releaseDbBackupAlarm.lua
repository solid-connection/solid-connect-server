-- 알림 억제와 연속 발생 횟수를 하나의 원자 연산으로 되돌린다.
-- 두 명령을 나누어 실행하면 그 사이에 다른 서버가 게이트를 얻어 증가시킨 횟수를 잘못 줄일 수 있다.
-- KEYS[1]: 억제 키, KEYS[2]: 연속 발생 횟수 키
redis.call('DEL', KEYS[1])

local count = tonumber(redis.call('GET', KEYS[2]))
-- 증가가 반영되지 않은 상태에서 줄이면 음수가 되므로, 1 이하이면 키를 지워 다음 알림이 1회차부터 시작하게 한다.
if count == nil or count <= 1 then
    redis.call('DEL', KEYS[2])
    return 0
end

return redis.call('DECR', KEYS[2])
